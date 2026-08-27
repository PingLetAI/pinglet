import SwiftUI

@MainActor final class ExploreModel: ObservableObject {
    @Published var loading = true; @Published var catalogs: [Catalog] = []; @Published var error: String?
    func load(_ env: AppEnvironment) async { loading = true; do { catalogs = try await env.session.perform("/api/v1/me/catalogs"); error = nil } catch { error = "Check your connection and try again." }; loading = false }
}
struct ExploreView: View {
    @EnvironmentObject private var env: AppEnvironment; @StateObject private var model = ExploreModel()
    var body: some View { NavigationStack { PingLetPage(eyebrow: "Explore", title: "Ideas beyond your saves.", subtitle: "Curated collections fill the quiet spaces. Your personal PingLets always come first.") {
        if model.loading { ProgressView() }
        if let error = model.error { PingLetCard { Text("Explore could not be loaded").font(.title2); Text(error); Button("TRY AGAIN") { Task { await model.load(env) } } } }
        if !model.loading && model.catalogs.isEmpty && model.error == nil { PingLetCard { Text("New collections are being prepared.").font(.title2); Text("Your own saves will continue rotating normally.") } }
        ForEach(model.catalogs) { catalog in NavigationLink { CatalogDetailView(catalogID: catalog.id) } label: { PingLetCard(dark: catalog.id == model.catalogs.first?.id) { Text(catalog.id == model.catalogs.first?.id ? "FEATURED COLLECTION" : (catalog.enabled ? "INCLUDED" : "PAUSED")).font(.caption.bold()).foregroundStyle(Color.pingletGold); Text(catalog.name).font(.title2); Text(catalog.description ?? "A curated PingLet collection."); if let preview = catalog.previewItems.first { Text("“\(preview.text)”").lineLimit(2) }; Text("\(catalog.itemCount) PINGLETS") } }.buttonStyle(.plain) }
    }.task { await model.load(env) }.navigationTitle("Explore") } }
}

struct CatalogDetailView: View {
    @EnvironmentObject private var env: AppEnvironment; let catalogID: String; @State private var catalog: CatalogDetail?; @State private var error: String?; @State private var notice: String?
    var body: some View { PingLetPage(eyebrow: "Collection", title: catalog?.name ?? "Loading collection", subtitle: catalog?.description ?? "") {
        if let error { PingLetCard { Text(error).foregroundStyle(.red); Button("TRY AGAIN") { Task { await load() } } } }; if let notice { Text(notice).foregroundStyle(.brown) }
        if let catalog { PingLetCard { Text(catalog.enabled ? "Included in your rotation" : "Paused").font(.title2); Text(catalog.enabled ? "PingLets from this collection can fill spaces after your personal saves." : "This collection will stay out of your rotation until you include it again."); Button(catalog.enabled ? "PAUSE COLLECTION" : "INCLUDE IN ROTATION") { Task { await toggle() } } }; ForEach(Array(catalog.items.enumerated()), id: \.element.id) { index, item in PingLetCard { HStack { Text(String(format: "%02d", index + 1)).foregroundStyle(.brown); Spacer(); Menu { Menu("Report this PingLet") { Button("Inappropriate or unsafe") { report(item.id, "UNSAFE") }; Button("Misleading or spam") { report(item.id, "MISLEADING_SPAM") }; Button("Privacy or rights concern") { report(item.id, "PRIVACY_RIGHTS") }; Button("Other") { report(item.id, "OTHER") } }; Button("Hide this source") { hide(item.id) } } label: { Image(systemName: "ellipsis") } }; Text(item.text).font(.title3); if let author = item.author { Text(author).foregroundStyle(.secondary) }; if let source = item.sourceUrl, let url = URL(string: source) { Link("VIEW ORIGINAL SOURCE", destination: url) } } } }
    }.task { await load() } }
    private func load() async { do { catalog = try await env.session.perform("/api/v1/me/catalogs/\(catalogID)/items"); error = nil } catch { error = "This collection could not be loaded." } }
    private func toggle() async { guard let old = catalog else { return }; catalog?.enabled.toggle(); struct Body: Encodable { let enabled: Bool }; do { let _: CatalogPreference = try await env.session.perform("/api/v1/me/catalogs/\(catalogID)", method: .patch, body: Body(enabled: !old.enabled)) } catch { catalog = old; error = "Collection preference could not be updated." } }
    private func report(_ id: String, _ reason: String) { Task { struct Body: Encodable { let reason: String }; if let result: ExploreAction = try? await env.session.perform("/api/v1/me/catalogs/items/\(id)/report", method: .post, body: Body(reason: reason)) { remove(result.hiddenContentIds); notice = "Report received. This PingLet is now hidden." } } }
    private func hide(_ id: String) { Task { if let result: ExploreAction = try? await env.session.perform("/api/v1/me/catalogs/items/\(id)/hide-source", method: .post, body: EmptyBody()) { remove(result.hiddenContentIds); notice = "This source is now hidden from Explore." } } }
    private func remove(_ ids: [String]) { catalog?.items.removeAll { ids.contains($0.id) }; catalog?.itemCount = catalog?.items.count ?? 0 }
}

struct ContentDetailView: View {
    @EnvironmentObject private var env: AppEnvironment; @Environment(\.dismiss) private var dismiss; let contentID: String; @State private var detail: ContentDetail?; @State private var failed = false
    private var local: FeedItem? { env.feed.first { $0.id == contentID } ?? env.library.first { $0.contentItemId == contentID }?.contentItem }
    var body: some View { NavigationStack { PingLetPage(eyebrow: local?.source == .personal ? "Saved by you" : "From PingLet", title: cleanPingLetText(detail?.content.text ?? local?.text ?? "This PingLet is unavailable."), subtitle: detail?.content.author ?? local?.author ?? "") {
        if let source = detail?.content.sourceUrl ?? local?.sourceUrl, let url = URL(string: source) { Link("OPEN ORIGINAL SOURCE", destination: url).buttonStyle(.borderedProminent) }
        if let d = detail { if let overview = d.overview, !overview.isEmpty { detailSection("Overview", overview) }; if !d.insights.isEmpty { Text("Key insights").font(.title2); ForEach(d.insights) { insight in PingLetCard { Text(insight.title).font(.headline); Text(insight.explanation); if !insight.evidence.isEmpty { Text("“\(insight.evidence)”").foregroundStyle(.secondary) } } } }; if d.access.fullDetailsUnlocked { if let summary = d.comprehensiveSummary { detailSection("Full summary", summary) }; if !d.actions.isEmpty { detailSection("Things to take forward", d.actions.map { "• \($0)" }.joined(separator: "\n")) }; ForEach(d.themes, id: \.self) { Text($0).padding(7).background(.thinMaterial, in: Capsule()) }; disclosure("Full transcript", d.transcript); disclosure("Text found in images", d.visibleText); disclosure("Original caption", d.caption) } else if d.access.hasAnalysis { PingLetCard { Text("There is more in this PingLet").font(.title2); Text("Unlock the full summary, all insights, practical takeaways, transcript, visible text, and related topics."); Text(d.access.isAnonymous ? "CREATE ACCOUNT TO TRY PLUS" : d.access.trialEligible ? "TRY PLUS FREE - 7 DAYS" : d.access.paidPlansEnabled ? "UNLOCK WITH PINGLET PLUS" : "PingLet Plus subscriptions are coming soon.").font(.headline) } } }
        else if failed { PingLetCard { Text("Details couldn't load").font(.headline); Text("Your saved PingLet and original source are still available."); Button("TRY AGAIN") { Task { await load() } } } }
    }.toolbar { Button("Close", action: dismiss.callAsFunction) }.task { await load() } } }
    @ViewBuilder private func detailSection(_ title: String, _ value: String) -> some View { Divider(); Text(title).font(.title2); Text(value) }
    @ViewBuilder private func disclosure(_ title: String, _ value: String?) -> some View { if let value, !value.isEmpty { DisclosureGroup(title) { Text(value) } } }
    private func load() async { do { detail = try await env.session.perform("/api/v1/me/content/\(contentID)/detail"); failed = false } catch { failed = true } }
}
