import SwiftUI
import WidgetKit

private let countSuffix = try! NSRegularExpression(pattern: #"([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$"#)
func cleanPingLetText(_ value: String) -> String {
    let range = NSRange(value.startIndex..., in: value)
    return countSuffix.stringByReplacingMatches(in: value, range: range, withTemplate: "$1").trimmingCharacters(in: .whitespacesAndNewlines)
}

struct PingLetPage<Content: View>: View {
    let eyebrow: String, title: String, subtitle: String; @ViewBuilder let content: Content
    @ScaledMetric(relativeTo: .title) private var titleSize: CGFloat = 30
    var body: some View {
        ZStack {
            PingLetCanvas()
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 20) {
                    if !eyebrow.isEmpty || !title.isEmpty || !subtitle.isEmpty {
                    VStack(alignment: .leading, spacing: 8) {
                        if !eyebrow.isEmpty {
                        Text(eyebrow.uppercased())
                            .font(.system(size: 12, weight: .bold, design: .rounded))
                            .tracking(1.8)
                            .foregroundStyle(Color.pingletClay)
                        }
                        if !title.isEmpty {
                        Text(title)
                            .font(.system(size: titleSize, weight: .regular, design: .serif))
                            .foregroundStyle(Color.pingletInk)
                            .fixedSize(horizontal: false, vertical: true)
                        }
                        if !subtitle.isEmpty {
                        Text(subtitle)
                            .font(.subheadline)
                            .foregroundStyle(Color.pingletMutedInk)
                            .lineSpacing(3)
                        }
                    }
                    }
                    content
                }
                .padding(.horizontal, 22)
                .padding(.top, 22)
                .padding(.bottom, 28)
            }
            .scrollIndicators(.hidden)
        }
        .foregroundStyle(Color.pingletInk)
    }
}
struct HomeView: View {
    @EnvironmentObject private var env: AppEnvironment
    @Environment(\.scenePhase) private var scenePhase
    let onOpen: (String) -> Void
    let onAdd: () -> Void
    let onWidgetSettings: () -> Void
    @State private var tick = Date()
    @State private var widgetInstalled: Bool?
    @AppStorage("home_widget_prompt_dismissed") private var widgetPromptDismissed = false
    @State private var poster: PosterContent?
    @State private var updatingFavorites = Set<String>()
    @State private var notice: String?
    private var profile: WidgetProfile {
        SharedWidgetSelector.resolvedProfile(feed: env.feed, stored: env.shared.widgetProfile(key: "default"),
            plus: (env.entitlement ?? env.shared.entitlement)?.plan == "PLUS", key: "default", date: tick)
    }
    private var featured: FeedItem? { env.feed.first { $0.id == profile.currentContentId } ?? env.feed.first }
    var body: some View {
        PingLetPage(eyebrow: "", title: "Today", subtitle: "") {
            if let item = featured {
                PingLetCard(dark: true) {
                    HStack {
                        Text("A LITTLE PINGLET").font(.caption.weight(.semibold)).tracking(1.2).foregroundStyle(Color.pingletGold)
                        Spacer()
                        Button { favorite(item) } label: {
                            Image(systemName: item.favorite ? "heart.fill" : "heart").frame(width: 44, height: 44)
                        }
                        .disabled(updatingFavorites.contains(item.id))
                        .accessibilityLabel(item.favorite ? "Remove from favorites" : "Add to favorites")
                        shareButton(item)
                    }
                    Button { onOpen(item.id) } label: {
                        Text(cleanPingLetText(item.text)).font(.title2).fontDesign(.serif).lineSpacing(5).lineLimit(8)
                            .frame(maxWidth: .infinity, alignment: .leading).multilineTextAlignment(.leading)
                    }.buttonStyle(.plain)
                    if let author = item.author {
                        Text(author).font(.subheadline).foregroundStyle(Color.pingletPaper.opacity(0.8))
                    }
                    Rectangle().fill(Color.pingletGold).frame(width: 32, height: 3).padding(.top, 6)
                }
            } else {
                PingLetCard {
                    Image(systemName: "text.quote").font(.title).foregroundStyle(Color.pingletClay)
                    Text("Make room for a good idea.").font(.title2).fontDesign(.serif)
                    Text("Save a thought or a post to get started.").font(.subheadline).foregroundStyle(Color.pingletMutedInk)
                    Button("Add your first PingLet", action: onAdd).buttonStyle(PingLetPrimaryButtonStyle())
                }
            }
            let more = Array(env.feed.filter { $0.id != featured?.id }.prefix(4))
            if !more.isEmpty {
                Text("Worth another look").font(.headline)
                ForEach(more) { item in
                    PingLetCard {
                        HStack {
                            Text(item.author ?? (item.source == .personal ? "Saved by you" : "From Explore"))
                                .font(.caption).foregroundStyle(Color.pingletMutedInk).lineLimit(1)
                            Spacer()
                            shareButton(item)
                        }
                        Button { onOpen(item.id) } label: {
                            Text(cleanPingLetText(item.text)).font(.body).fontDesign(.serif).lineSpacing(3).lineLimit(4)
                                .frame(maxWidth: .infinity, alignment: .leading).multilineTextAlignment(.leading)
                        }.buttonStyle(.plain)
                    }
                }
            }
            if widgetInstalled == false && !widgetPromptDismissed {
                HStack {
                    Button(action: onWidgetSettings) {
                        Label("Keep a PingLet on your Home Screen", systemImage: "rectangle.3.group")
                            .font(.subheadline).multilineTextAlignment(.leading)
                    }.frame(minHeight: 44)
                    Spacer(minLength: 8)
                    Button { widgetPromptDismissed = true } label: { Image(systemName: "xmark").frame(width: 44, height: 44) }
                        .accessibilityLabel("Dismiss widget suggestion")
                }.foregroundStyle(Color.pingletMutedInk).padding(.top, 8)
            }
        }
        .sheet(item: $poster) { PosterShareView(content: $0) }
        .alert("Favorite saved locally", isPresented: Binding(get: { notice != nil }, set: { if !$0 { notice = nil } })) {
            Button("OK") { notice = nil }
        } message: { Text(notice ?? "") }
        .refreshable { await env.syncFeed() }
        .task(id: scenePhase) {
            guard scenePhase == .active else { return }
            widgetInstalled = await withCheckedContinuation { continuation in
                WidgetCenter.shared.getCurrentConfigurations { result in
                    switch result {
                    case .success(let configurations): continuation.resume(returning: configurations.contains { $0.kind == "PingLetWidget" })
                    case .failure: continuation.resume(returning: nil)
                    }
                }
            }
        }
        .task {
            while !Task.isCancelled {
                try? await Task.sleep(for: .seconds(15))
                tick = .now
            }
        }
    }
    private func shareButton(_ item: FeedItem) -> some View {
        Button {
            poster = PosterContent(id: item.id, text: item.text, author: item.author, sourceURL: item.sourceUrl)
        } label: {
            Image(systemName: "square.and.arrow.up").frame(width: 44, height: 44)
        }.buttonStyle(.plain).accessibilityLabel("Share PingLet as image")
    }
    private func favorite(_ item: FeedItem) {
        guard !updatingFavorites.contains(item.id) else { return }
        updatingFavorites.insert(item.id)
        Task {
            defer { updatingFavorites.remove(item.id); env.feed = env.shared.feed }
            do { try await env.setFavorite(item.id, !item.favorite) }
            catch { notice = "Open PingLet online to sync your favorite." }
        }
    }
}

struct LibraryView: View {
    @EnvironmentObject private var env: AppEnvironment; let onOpen: (String) -> Void; let onAdd: () -> Void
    @State private var query = ""; @State private var favoritesOnly = false; @State private var loading = true; @State private var error: String?; @State private var deleteCandidate: UserContent?
    @State private var deletingIDs = Set<String>()
    private var visible: [UserContent] { env.library.filter { (!favoritesOnly || $0.favorite) && (query.isEmpty || $0.contentItem.text.localizedCaseInsensitiveContains(query) || $0.contentItem.author?.localizedCaseInsensitiveContains(query) == true) } }
    var body: some View { PingLetPage(eyebrow: "", title: "Library", subtitle: "") {
        Picker("Library", selection: $favoritesOnly) { Text("All saves").tag(false); Text("Favorites").tag(true) }.pickerStyle(.segmented)
        if !env.library.isEmpty { TextField("Search your PingLets", text: $query).textFieldStyle(.roundedBorder) }
        if loading { ProgressView().frame(maxWidth: .infinity) }
        if let error { PingLetCard { Label(error, systemImage: "exclamationmark.circle"); Button("Dismiss") { self.error = nil }; Button("Refresh library") { Task { await refresh() } } } }
        if !loading && visible.isEmpty {
            ContentUnavailableView {
                Label(query.isEmpty ? (favoritesOnly ? "No favorites yet" : "Your ideas belong here") : "No matching PingLets", systemImage: query.isEmpty ? "bookmark" : "magnifyingglass")
            } description: {
                Text(query.isEmpty ? "Save a thought or share a public post. Use the heart to keep favorites close." : "Try different words or search for a creator.")
            } actions: {
                if !query.isEmpty { Button("Clear search") { query = "" } }
                else if !favoritesOnly { Button("Add a PingLet", action: onAdd).buttonStyle(PingLetPrimaryButtonStyle()) }
            }
        }
        ForEach(visible) { row in
            PingLetCard {
                HStack {
                    Text(row.contentItem.type.rawValue.capitalized).font(.caption).foregroundStyle(Color.pingletClay)
                    Spacer()
                    if deletingIDs.contains(row.id) { ProgressView().accessibilityLabel("Deleting") }
                    Button { toggle(row) } label: {
                        Image(systemName: row.favorite ? "heart.fill" : "heart").frame(width: 44, height: 44)
                    }.accessibilityLabel(row.favorite ? "Remove from favorites" : "Add to favorites")
                    Menu {
                        Button("Delete PingLet", role: .destructive) { deleteCandidate = row }
                    } label: { Image(systemName: "ellipsis").frame(width: 44, height: 44) }
                    .accessibilityLabel("PingLet actions")
                }
                Button { onOpen(row.contentItemId) } label: {
                    Text(cleanPingLetText(row.contentItem.text)).font(.title3).fontDesign(.serif)
                        .lineLimit(3).frame(maxWidth: .infinity, alignment: .leading).multilineTextAlignment(.leading)
                }.buttonStyle(.plain)
                if let author = row.contentItem.author { Text(author).font(.subheadline).foregroundStyle(Color.pingletMutedInk) }
            }
            .disabled(deletingIDs.contains(row.id))
            .onLongPressGesture { deleteCandidate = row }
            .accessibilityAction(named: "Delete PingLet") { deleteCandidate = row }
        }
    }.task { await refresh() }.refreshable { await refresh() }
        .confirmationDialog("Delete this PingLet?", isPresented: Binding(get: { deleteCandidate != nil }, set: { if !$0 { deleteCandidate = nil } })) {
            Button("Delete", role: .destructive) {
                guard let row = deleteCandidate else { return }
                deleteCandidate = nil
                deletingIDs.insert(row.id)
                Task {
                    defer { deletingIDs.remove(row.id) }
                    do { try await env.deleteUserContent(row.id, contentItemId: row.contentItemId) }
                    catch { self.error = "Could not delete this PingLet. Try again." }
                }
            }
            Button("Cancel", role: .cancel) { deleteCandidate = nil }
        } message: {
            Text("It will be removed from your library and widget rotation.")
        }
}
    private func refresh() async { loading = true; do { try await env.refreshLibrary(); error = nil } catch { env.library = env.shared.library; self.error = env.library.isEmpty ? "Your library could not be loaded. Check your connection and try again." : nil }; loading = false }
    private func toggle(_ row: UserContent) { let target = !row.favorite; Task { do { try await env.setFavorite(row.contentItemId, target) } catch { try? await env.setFavorite(row.contentItemId, !target) } } }
}
