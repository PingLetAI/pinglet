import SwiftUI

private let countSuffix = try! NSRegularExpression(pattern: #"([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$"#)
func cleanPingLetText(_ value: String) -> String {
    let range = NSRange(value.startIndex..., in: value)
    return countSuffix.stringByReplacingMatches(in: value, range: range, withTemplate: "$1").trimmingCharacters(in: .whitespacesAndNewlines)
}

struct PingLetPage<Content: View>: View {
    let eyebrow: String, title: String, subtitle: String; @ViewBuilder let content: Content
    private var titleSize: CGFloat { title.count > 180 ? 27 : title.count > 90 ? 33 : 42 }
    var body: some View {
        ZStack {
            PingLetCanvas()
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 20) {
                    VStack(alignment: .leading, spacing: 10) {
                        Text(eyebrow.uppercased())
                            .font(.system(size: 12, weight: .bold, design: .rounded))
                            .tracking(1.8)
                            .foregroundStyle(Color.pingletClay)
                        Text(title)
                            .font(.system(size: titleSize, weight: .regular, design: .serif))
                            .foregroundStyle(Color.pingletInk)
                            .fixedSize(horizontal: false, vertical: true)
                        Text(subtitle)
                            .font(.system(size: 17, weight: .medium, design: .rounded))
                            .foregroundStyle(Color.pingletMutedInk)
                            .lineSpacing(3)
                    }
                    .padding(.bottom, 8)
                    content
                }
                .padding(.horizontal, 22)
                .padding(.top, 22)
                .padding(.bottom, 108)
            }
            .scrollIndicators(.hidden)
        }
        .foregroundStyle(Color.pingletInk)
    }
}
struct HomeView: View {
    @EnvironmentObject private var env: AppEnvironment; let onOpen: (String) -> Void; @State private var tick = Date()
    private var profile: WidgetProfile {
        SharedWidgetSelector.resolvedProfile(
            feed: env.feed,
            stored: env.shared.widgetProfile(key: "default"),
            plus: (env.entitlement ?? env.shared.entitlement)?.plan == "PLUS",
            key: "default",
            date: tick
        )
    }
    var body: some View { PingLetPage(eyebrow: "Today", title: "One good thought, kept close.", subtitle: "Your personal saves lead. PingLet fills the gaps quietly.") {
        PingLetCard(dark: true) { HStack { Text("ON YOUR WIDGET").font(.caption.bold()).foregroundStyle(Color.pingletGold); Spacer(); Text(profile.nextChangeAt > 0 ? "CHANGING SOON" : "ABOUT 30 MIN").font(.caption) }; Text(cleanPingLetText(profile.currentText.isEmpty ? "Your next thought is finding its place." : profile.currentText)).font(.system(size: 28, design: .serif)); if let author = profile.currentAuthor { Text(author).foregroundStyle(.gray) }; Rectangle().fill(Color.pingletGold).frame(width: 36, height: 3) }.onTapGesture { if !profile.currentContentId.isEmpty { onOpen(profile.currentContentId) } }
        PingLetSectionLabel(title: "Coming up", trailing: "Ready offline")
        Text("A new PingLet returns approximately every 30 minutes.").font(.system(size: 14, weight: .medium, design: .rounded)).foregroundStyle(Color.pingletMutedInk)
        let upcoming = Array(env.feed.filter { $0.id != profile.currentContentId }.prefix(5))
        if upcoming.isEmpty { PingLetCard { Text("Share a post or tap + to build your rotation.") } }
        else { PingLetCard { ForEach(Array(upcoming.enumerated()), id: \.element.id) { index, item in Button { onOpen(item.id) } label: { HStack(spacing: 12) { Text(String(format: "%02d", index + 1)).foregroundStyle(.brown); Text(cleanPingLetText(item.text)).lineLimit(2).foregroundStyle(Color.pingletInk); Spacer(); Image(systemName: "chevron.right") } }.buttonStyle(.plain); if index < upcoming.count - 1 { Divider() } } } }
    }.task { while !Task.isCancelled { try? await Task.sleep(for: .seconds(15)); tick = .now } } }
}

struct LibraryView: View {
    @EnvironmentObject private var env: AppEnvironment; let onOpen: (String) -> Void; let onAdd: () -> Void
    @State private var query = ""; @State private var favoritesOnly = false; @State private var loading = true; @State private var error: String?
    private var visible: [UserContent] { env.library.filter { (!favoritesOnly || $0.favorite) && (query.isEmpty || $0.contentItem.text.localizedCaseInsensitiveContains(query) || $0.contentItem.author?.localizedCaseInsensitiveContains(query) == true) } }
    var body: some View { PingLetPage(eyebrow: "Library", title: "Everything you kept.", subtitle: "Personal saves live here and lead your rotation.") {
        Picker("Library", selection: $favoritesOnly) { Text("All saves").tag(false); Text("Favorites").tag(true) }.pickerStyle(.segmented)
        if !env.library.isEmpty { TextField("Search your PingLets", text: $query).textFieldStyle(.roundedBorder) }
        if loading { ProgressView().frame(maxWidth: .infinity) }
        if let error { PingLetCard { Text("Your library could not be loaded").font(.title2); Text(error); Button("TRY AGAIN") { Task { await refresh() } } } }
        if !loading && visible.isEmpty { PingLetCard { Text(favoritesOnly ? "Nothing favorited yet" : "Start your library").font(.title2); Text(favoritesOnly ? "Use the heart on any PingLet to keep it close." : "Write a thought or share a public post from another app."); if !favoritesOnly { Button("ADD A PINGLET", action: onAdd) } } }
        ForEach(visible) { row in PingLetCard { HStack { Text(row.contentItem.type.rawValue.replacingOccurrences(of: "_", with: " ")).font(.caption).foregroundStyle(.brown); Spacer(); Button { toggle(row) } label: { Image(systemName: row.favorite ? "heart.fill" : "heart").foregroundStyle(row.favorite ? Color.pingletGold : .secondary) } }; Text(cleanPingLetText(row.contentItem.text)).font(.title3); if let author = row.contentItem.author { Text(author).foregroundStyle(.secondary) } }.onTapGesture { onOpen(row.contentItemId) } }
    }.task { await refresh() } }
    private func refresh() async { loading = true; do { try await env.refreshLibrary(); error = nil } catch { env.library = env.shared.library; self.error = env.library.isEmpty ? "Your library could not be loaded. Check your connection and try again." : nil }; loading = false }
    private func toggle(_ row: UserContent) { let target = !row.favorite; Task { do { try await env.setFavorite(row.contentItemId, target) } catch { try? await env.setFavorite(row.contentItemId, !target) } } }
}
