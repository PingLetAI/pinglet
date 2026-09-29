import SwiftUI

@MainActor final class ExploreModel: ObservableObject {
    @Published var loading = true; @Published var catalogs: [Catalog] = []; @Published var error: String?
    func load(_ env: AppEnvironment) async { loading = true; do { catalogs = try await env.session.perform("/api/v1/me/catalogs"); error = nil } catch { self.error = "Check your connection and try again." }; loading = false }
}
struct ExploreView: View {
    @EnvironmentObject private var env: AppEnvironment
    @StateObject private var model = ExploreModel()
    var body: some View {
        NavigationStack {
            PingLetPage(eyebrow: "", title: "Explore", subtitle: "Find your next good thought.") {
                if model.loading && model.catalogs.isEmpty { ProgressView().frame(maxWidth: .infinity) }
                if let error = model.error {
                    PingLetCard {
                        Text(error).font(.subheadline)
                        Button("Try again") { Task { await model.load(env) } }.buttonStyle(PingLetSecondaryButtonStyle())
                    }
                }
                if !model.loading && model.catalogs.isEmpty && model.error == nil {
                    ContentUnavailableView("More ideas are on the way", systemImage: "sparkles", description: Text("New collections will appear here."))
                }
                ForEach(model.catalogs) { catalog in
                    NavigationLink { CatalogDetailView(catalogID: catalog.id) } label: {
                        PingLetCard {
                            HStack(alignment: .top, spacing: 12) {
                                Text(catalog.name).font(.title2).fontDesign(.serif)
                                Spacer(minLength: 4)
                                Image(systemName: "arrow.up.right").font(.subheadline.weight(.semibold))
                                    .frame(width: 36, height: 36)
                                    .background(Color.pingletMint, in: Circle())
                            }
                            if let preview = catalog.previewItems.first {
                                Text("“\(preview.text)”").font(.subheadline).foregroundStyle(Color.pingletMutedInk).lineLimit(2)
                            } else if let description = catalog.description {
                                Text(description).font(.subheadline).foregroundStyle(Color.pingletMutedInk).lineLimit(2)
                            }
                            HStack {
                                Text("\(catalog.itemCount) PingLets").font(.caption).foregroundStyle(Color.pingletMutedInk)
                                Spacer()
                            }
                            HStack {
                                Text("View collection")
                                Spacer()
                                Image(systemName: "arrow.right")
                            }
                            .font(.subheadline.weight(.semibold))
                            .padding(.horizontal, 16).frame(minHeight: 48)
                            .foregroundStyle(Color.pingletPaper)
                            .background(Color.pingletInk, in: RoundedRectangle(cornerRadius: 14))
                        }
                        .multilineTextAlignment(.leading)
                    }.buttonStyle(.plain)
                    .accessibilityElement(children: .combine)
                }
            }
            .toolbar(.hidden, for: .navigationBar)
            .task { await model.load(env) }
            .refreshable { await model.load(env) }
        }
    }
}

struct CatalogDetailView: View {
    @EnvironmentObject private var env: AppEnvironment
    let catalogID: String
    @State private var catalog: CatalogDetail?
    @State private var error: String?
    @State private var notice: String?
    @State private var updating = false
    var body: some View {
        PingLetPage(eyebrow: "", title: catalog?.name ?? "Collection", subtitle: "") {
            if catalog == nil && error == nil { ProgressView("Loading collection").frame(maxWidth: .infinity) }
            if let error {
                PingLetCard {
                    Label(error, systemImage: "exclamationmark.circle").font(.subheadline)
                    Button("Try again") { Task { await load() } }.buttonStyle(PingLetSecondaryButtonStyle())
                }
            }
            if let notice { Label(notice, systemImage: "checkmark.circle").font(.subheadline).foregroundStyle(Color.pingletMutedInk) }
            if let catalog {
                if let description = catalog.description, !description.isEmpty {
                    Text(description).font(.subheadline).foregroundStyle(Color.pingletMutedInk)
                }
                if catalog.enabled {
                    Toggle(isOn: Binding(get: { self.catalog?.enabled == true }, set: { _ in Task { await toggle() } })) {
                        Label(updating ? "Updating…" : "In rotation", systemImage: "checkmark.circle.fill")
                            .font(.headline)
                    }
                    .tint(Color.pingletInk).padding(16)
                    .background(Color.pingletMint.opacity(0.5), in: RoundedRectangle(cornerRadius: 18))
                    .disabled(updating)
                } else {
                    Button { Task { await toggle() } } label: {
                        HStack {
                            if updating { ProgressView().tint(Color.pingletPaper) }
                            Text(updating ? "Adding…" : "Add to rotation")
                            if !updating { Image(systemName: "plus") }
                        }
                    }.buttonStyle(PingLetPrimaryButtonStyle()).disabled(updating)
                }
                PingLetSectionLabel(title: "\(catalog.items.count) PingLets")
                ForEach(catalog.items) { item in
                    PingLetCard {
                        Text(item.text).font(.title3).fontDesign(.serif).textSelection(.enabled)
                        HStack {
                            if let author = item.author {
                                Text(author).font(.subheadline).foregroundStyle(Color.pingletMutedInk)
                            }
                            Spacer()
                            Menu {
                                Menu("Report this PingLet") {
                                    Button("Inappropriate or unsafe") { report(item.id, "UNSAFE") }
                                    Button("Misleading or spam") { report(item.id, "MISLEADING_SPAM") }
                                    Button("Privacy or rights concern") { report(item.id, "PRIVACY_RIGHTS") }
                                    Button("Other") { report(item.id, "OTHER") }
                                }
                                Button("Hide this source") { hide(item.id) }
                            } label: { Image(systemName: "ellipsis").frame(width: 44, height: 44) }
                            .accessibilityLabel("PingLet actions")
                        }
                        if let source = item.sourceUrl, let url = URL(string: source) {
                            Link(destination: url) { Label("Original source", systemImage: "arrow.up.right") }
                                .buttonStyle(PingLetSecondaryButtonStyle())
                        }
                    }
                }
                if catalog.items.isEmpty {
                    Text("No PingLets to show in this collection yet.").foregroundStyle(Color.pingletMutedInk)
                }
            }
        }
        .toolbar(.visible, for: .navigationBar)
        .navigationBarTitleDisplayMode(.inline)
        .task { await load() }
        .refreshable { await load() }
    }
    private func load() async { do { catalog = try await env.session.perform("/api/v1/me/catalogs/\(catalogID)/items"); error = nil } catch { self.error = "This collection could not be loaded." } }
    private func toggle() async { guard !updating, let old = catalog else { return }; updating = true; defer { updating = false }; error = nil; struct Body: Encodable { let enabled: Bool }; do { let _: CatalogPreference = try await env.session.perform("/api/v1/me/catalogs/\(catalogID)", method: .patch, body: Body(enabled: !old.enabled)); catalog?.enabled = !old.enabled; await env.syncFeed() } catch { catalog = old; self.error = "Collection preference could not be updated." } }
    private func report(_ id: String, _ reason: String) { Task { struct Body: Encodable { let reason: String }; if let result: ExploreAction = try? await env.session.perform("/api/v1/me/catalogs/items/\(id)/report", method: .post, body: Body(reason: reason)) { remove(result.hiddenContentIds); notice = "Report received. This PingLet is now hidden."; await env.syncFeed() } else { error = "Your report could not be sent. Please try again." } } }
    private func hide(_ id: String) { Task { if let result: ExploreAction = try? await env.session.perform("/api/v1/me/catalogs/items/\(id)/hide-source", method: .post, body: EmptyBody()) { remove(result.hiddenContentIds); notice = "This source is now hidden from Explore."; await env.syncFeed() } else { error = "This source could not be hidden. Please try again." } } }
    private func remove(_ ids: [String]) { catalog?.items.removeAll { ids.contains($0.id) }; catalog?.itemCount = catalog?.items.count ?? 0 }
}

struct ContentDetailView: View {
    @State private var poster: PosterContent?
    @EnvironmentObject private var env: AppEnvironment; @Environment(\.dismiss) private var dismiss; let contentID: String; @State private var detail: ContentDetail?; @State private var failed = false
    private var local: FeedItem? { env.feed.first { $0.id == contentID } ?? env.library.first { $0.contentItemId == contentID }?.contentItem }
    var body: some View { NavigationStack { PingLetPage(eyebrow: local?.source == .personal ? "Saved by you" : "From PingLet", title: "", subtitle: "") {
        if let text = detail?.content.text ?? local?.text {
            Text(cleanPingLetText(text)).font(.title3).fontDesign(.serif).lineSpacing(6).textSelection(.enabled)
        } else if !failed { ProgressView("Loading PingLet").frame(maxWidth: .infinity) }
        if let author = detail?.content.author ?? local?.author { Text(author).font(.subheadline).foregroundStyle(Color.pingletMutedInk) }
        if let source = detail?.content.sourceUrl ?? local?.sourceUrl, let url = URL(string: source) { Link(destination: url) { Label("Original source", systemImage: "arrow.up.right") }.buttonStyle(PingLetSecondaryButtonStyle()) }
        if let d = detail { if let overview = d.overview, !overview.isEmpty { detailSection("Overview", overview) }; if !d.insights.isEmpty { Text("Key insights").font(.title2); ForEach(d.insights) { insight in PingLetCard { Text(insight.title).font(.headline); Text(insight.explanation); if !insight.evidence.isEmpty { Text("“\(insight.evidence)”").foregroundStyle(.secondary) } } } }; if d.access.fullDetailsUnlocked { if let summary = d.comprehensiveSummary { disclosure("Full summary", summary) }; if !d.actions.isEmpty { disclosure("Things to take forward", d.actions.map { "• \($0)" }.joined(separator: "\n")) }; ForEach(d.themes, id: \.self) { Text($0).padding(7).background(.thinMaterial, in: Capsule()) }; disclosure("Full transcript", d.transcript); disclosure("Text found in images", d.visibleText); disclosure("Original caption", d.caption) } else if d.access.hasAnalysis { PingLetCard { Text("There is more in this PingLet").font(.title2); Text("Go deeper with full summaries, insights, and transcripts."); plusAction(d.access) } } }
        else if failed { PingLetCard { Text("Details couldn't load").font(.headline); Text(local == nil ? "Check your connection and try again." : "Your saved PingLet and original source are still available."); Button("TRY AGAIN") { Task { await load() } } } }
    }.toolbar {
        ToolbarItem(placement: .cancellationAction) { Button("Close", action: dismiss.callAsFunction) }
        ToolbarItem(placement: .primaryAction) {
            if let text = detail?.content.text ?? local?.text {
                PosterShareButton {
                    poster = PosterContent(id: contentID, text: text, author: detail?.content.author ?? local?.author, sourceURL: detail?.content.sourceUrl ?? local?.sourceUrl)
                }
            }
        }
    }.sheet(item: $poster) { PosterShareView(content: $0) }.task { await load() }.onChange(of: env.entitlement?.plan) { _, _ in Task { await load() } }.onChange(of: env.entitlement?.isAnonymous) { _, _ in Task { await load() } } } }
    @ViewBuilder private func detailSection(_ title: String, _ value: String) -> some View { Divider(); Text(title).font(.title2); Text(value) }
    @ViewBuilder private func disclosure(_ title: String, _ value: String?) -> some View { if let value, !value.isEmpty { DisclosureGroup(title) { Text(value) } } }
    @ViewBuilder private func plusAction(_ access: DetailAccess) -> some View {
        if access.isAnonymous {
            NavigationLink { AccountConnectionView() } label: { Text("Create account to try Plus") }
                .buttonStyle(PingLetPrimaryButtonStyle())
        } else if access.trialEligible {
            NavigationLink { TrialOfferView() } label: { Text("Try Plus · 7 days free") }
                .buttonStyle(PingLetPrimaryButtonStyle())
        } else if access.paidPlansEnabled {
            NavigationLink { PlusPlansView() } label: { Text("Unlock with Plus") }
                .buttonStyle(PingLetPrimaryButtonStyle())
        } else {
            Text("PingLet Plus subscriptions are coming soon.")
                .font(.system(size: 14, weight: .semibold, design: .rounded))
                .foregroundStyle(Color.pingletMutedInk)
        }
    }
    private func load() async { do { detail = try await env.session.perform("/api/v1/me/content/\(contentID)/detail"); failed = false } catch { failed = true } }
}
