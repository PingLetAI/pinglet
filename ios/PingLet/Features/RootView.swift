import SwiftUI

struct RootView: View {
    enum Tab: Hashable { case home, library, explore, settings }
    @EnvironmentObject private var env: AppEnvironment
    @Environment(\.scenePhase) private var scenePhase
    @State private var tab: Tab = .home; @State private var addRoute: AddRoute?; @State private var contentID: String?
    @State private var submittingShare = false; @State private var shareQueued = false
    @State private var processingItems: [Ingestion] = []; @State private var showingQueue = false
    var body: some View {
        ZStack(alignment: .bottom) { TabView(selection: $tab) {
            HomeView(onOpen: { contentID = $0 }).tabItem { Label("Home", systemImage: "house.fill") }.tag(Tab.home)
            LibraryView(onOpen: { contentID = $0 }, onAdd: { addRoute = AddRoute(text: "") }).tabItem { Label("Library", systemImage: "bookmark.fill") }.tag(Tab.library)
            ExploreView().tabItem { Label("Explore", systemImage: "safari.fill") }.tag(Tab.explore)
            SettingsView().tabItem { Label("Settings", systemImage: "gearshape.fill") }.tag(Tab.settings)
        }
        .tint(Color.pingletInk)
        Button { addRoute = AddRoute(text: "") } label: {
            Image(systemName: "plus")
                .font(.system(size: 22, weight: .bold, design: .rounded))
                .frame(width: 58, height: 58)
                .background(Color.pingletGold, in: Circle())
                .foregroundStyle(Color.pingletInk)
                .overlay(Circle().stroke(Color.pingletPaper.opacity(0.9), lineWidth: 5))
                .shadow(color: Color.pingletInk.opacity(0.22), radius: 14, y: 7)
        }
        .accessibilityLabel("Add a PingLet")
        .padding(.bottom, 8)
        if !activeProcessing.isEmpty {
            Button { showingQueue = true } label: {
                HStack(spacing: 9) {
                    ProgressView().tint(Color.pingletPaper)
                    Text(activeProcessing.count == 1 ? "1 LINK IN PROGRESS" : "\(activeProcessing.count) LINKS IN PROGRESS")
                        .font(.system(size: 11, weight: .bold, design: .rounded)).tracking(0.8)
                    Image(systemName: "chevron.right")
                }
                .padding(.horizontal, 15).padding(.vertical, 11)
                .foregroundStyle(Color.pingletPaper).background(Color.pingletInk, in: Capsule())
                .shadow(color: Color.pingletInk.opacity(0.22), radius: 10, y: 5)
            }
            .buttonStyle(.plain)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottomLeading)
            .padding(.leading, 14).padding(.bottom, 76)
        }
        }
        .sheet(item: $addRoute) { AddPingLetView(initialText: $0.text) }
        .sheet(item: Binding(get: { contentID.map(ContentRoute.init) }, set: { contentID = $0?.id })) { ContentDetailView(contentID: $0.id) }
        .sheet(isPresented: $showingQueue) { NavigationStack { ProcessingQueueView() } }
        .task { await submitPendingShare() }
        .task { await monitorProcessing() }
        .task { await refreshFeedPeriodically() }
        .onChange(of: scenePhase) { _, phase in
            if phase == .active {
                Task {
                    await env.resumeFromBackground()
                    await submitPendingShare()
                }
            }
        }
        .alert("Saved to PingLet", isPresented: $shareQueued) {
            Button("OK") {}
        } message: {
            Text("Your shared post is now in the processing queue. You can keep using PingLet while it is analyzed.")
        }
        .onOpenURL { url in
            guard url.scheme?.lowercased() == "pinglet", url.host?.lowercased() == "content",
                  let id = url.pathComponents.dropFirst().first, !id.isEmpty else { return }
            contentID = id
        }
    }

    private var activeProcessing: [Ingestion] { processingItems.filter { !["READY", "FAILED", "REJECTED"].contains($0.status) } }
    private func refreshFeedPeriodically() async {
        while !Task.isCancelled {
            try? await Task.sleep(for: .seconds(30 * 60))
            guard !Task.isCancelled, scenePhase == .active else { continue }
            await env.syncFeed()
        }
    }
    private func monitorProcessing() async {
        while !Task.isCancelled {
            if let rows: [Ingestion] = try? await env.session.perform("/api/v1/me/ingestions") { processingItems = rows }
            try? await Task.sleep(for: .seconds(4))
        }
    }

    private func submitPendingShare() async {
        guard !submittingShare, addRoute == nil,
              let value = env.shared.pendingShare?.trimmingCharacters(in: .whitespacesAndNewlines),
              !value.isEmpty else { return }
        guard let url = firstWebURL(in: value) else {
            env.shared.pendingShare = nil
            addRoute = AddRoute(text: value)
            return
        }

        submittingShare = true
        let context = value.replacingOccurrences(of: url.absoluteString, with: "").trimmingCharacters(in: .whitespacesAndNewlines)
        do {
            let _: Ingestion = try await env.session.perform(
                "/api/v1/me/ingestions",
                method: .post,
                body: IngestionRequest(url: url.absoluteString, contextText: context.isEmpty ? nil : context)
            )
            env.shared.pendingShare = nil
            await env.refreshEntitlement()
            shareQueued = true
        } catch let api as APIError where api.code == "TERMS_ACCEPTANCE_REQUIRED" {
            env.shared.pendingShare = nil
            addRoute = AddRoute(text: value)
        } catch {
            env.shared.pendingShare = nil
            addRoute = AddRoute(text: value)
        }
        submittingShare = false
    }

    private func firstWebURL(in value: String) -> URL? {
        guard let detector = try? NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue) else { return nil }
        let range = NSRange(value.startIndex..., in: value)
        return detector.matches(in: value, range: range).compactMap(\.url).first {
            ["http", "https"].contains($0.scheme?.lowercased() ?? "")
        }
    }
}
private struct AddRoute: Identifiable { let id = UUID(); let text: String }
private struct ContentRoute: Identifiable { let id: String }
private struct PendingParityView: View { let title: String; var body: some View { NavigationStack { Color.pingletBackground.ignoresSafeArea().navigationTitle(title) } } }
