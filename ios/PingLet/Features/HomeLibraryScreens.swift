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
    @EnvironmentObject private var env: AppEnvironment; let onOpen: (String) -> Void; let onAdd: () -> Void; @State private var tick = Date()
    let onWidgetSettings: () -> Void
    @Environment(\.scenePhase) private var scenePhase
    @State private var widgetInstalled: Bool?
    private var profile: WidgetProfile {
        SharedWidgetSelector.resolvedProfile(
            feed: env.feed,
            stored: env.shared.widgetProfile(key: "default"),
            plus: (env.entitlement ?? env.shared.entitlement)?.plan == "PLUS",
            key: "default",
            date: tick
        )
    }
    var body: some View { PingLetPage(eyebrow: "", title: "Today", subtitle: "") {
        PingLetCard(dark: true) {
            HStack {
                Text("YOUR PINGLET").font(.caption.bold()).foregroundStyle(Color.pingletGold)
                Spacer()
                if profile.nextChangeAt > 0 {
                    Text("Next around \(Date(timeIntervalSince1970: Double(profile.nextChangeAt) / 1000).formatted(date: .omitted, time: .shortened))").font(.caption)
                }
            }
            Text(cleanPingLetText(profile.currentText.isEmpty ? "Keep something worth coming back to." : profile.currentText))
                .font(.title2).fontDesign(.serif).lineLimit(7)
            if let author = profile.currentAuthor { Text(author).font(.subheadline).foregroundStyle(Color.pingletPaper.opacity(0.8)) }
            Rectangle().fill(Color.pingletGold).frame(width: 36, height: 3)
            if profile.currentContentId.isEmpty {
                Button("Save your first PingLet", action: onAdd)
                    .font(.subheadline.weight(.semibold)).foregroundStyle(Color.pingletGold)
                    .frame(minHeight: 44)
            }
        }.onTapGesture { if !profile.currentContentId.isEmpty { onOpen(profile.currentContentId) } }
        Button(action: onWidgetSettings) {
            HStack(spacing: 10) {
                Image(systemName: "rectangle.3.group")
                Text(widgetInstalled == false ? "Set up widget" : widgetInstalled == true ? "Customize widget" : "Widget settings")
                Spacer()
                Image(systemName: "arrow.right")
            }
            .font(.subheadline.weight(.semibold))
            .padding(.horizontal, 16).frame(minHeight: 50)
            .foregroundStyle(widgetInstalled == false ? Color.pingletPaper : Color.pingletInk)
            .background(widgetInstalled == false ? Color.pingletInk : Color.pingletPaper, in: RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color.pingletInk.opacity(0.2), lineWidth: 1))
            .contentShape(RoundedRectangle(cornerRadius: 16))
        }.buttonStyle(.plain)
        let upcoming = Array(env.feed.filter { $0.id != profile.currentContentId }.prefix(3))
        if !upcoming.isEmpty {
            VStack(alignment: .leading, spacing: 12) {
                Text("More to revisit").font(.headline).foregroundStyle(Color.pingletInk)
                ForEach(upcoming) { item in
                    Button { onOpen(item.id) } label: {
                        HStack(alignment: .center, spacing: 14) {
                            VStack(alignment: .leading, spacing: 9) {
                                Text(cleanPingLetText(item.text))
                                    .font(.body).fontDesign(.serif).lineLimit(3)
                                    .foregroundStyle(Color.pingletInk)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                Label(item.author ?? (item.source == .personal ? "Saved by you" : "From Explore"), systemImage: item.source == .personal ? "bookmark" : "sparkles")
                                    .font(.caption).lineLimit(1).foregroundStyle(Color.pingletMutedInk)
                            }
                            Image(systemName: "chevron.right").font(.caption.weight(.semibold)).foregroundStyle(Color.pingletMutedInk)
                        }
                        .padding(16).frame(maxWidth: .infinity, minHeight: 80)
                        .background(Color.pingletPaper, in: RoundedRectangle(cornerRadius: 18))
                        .overlay(RoundedRectangle(cornerRadius: 18).stroke(Color.pingletLine, lineWidth: 1))
                        .multilineTextAlignment(.leading).contentShape(RoundedRectangle(cornerRadius: 18))
                    }.buttonStyle(.plain)
                }
            }
        }
    }.refreshable { await env.syncFeed() }
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
        .task { while !Task.isCancelled { try? await Task.sleep(for: .seconds(15)); tick = .now } } }
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
