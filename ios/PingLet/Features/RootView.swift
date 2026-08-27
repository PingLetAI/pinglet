import SwiftUI

struct RootView: View {
    enum Tab: Hashable { case home, library, explore, settings }
    @EnvironmentObject private var env: AppEnvironment
    @Environment(\.scenePhase) private var scenePhase
    @State private var tab: Tab = .home; @State private var addRoute: AddRoute?; @State private var contentID: String?
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
        }
        .sheet(item: $addRoute) { AddPingLetView(initialText: $0.text) }
        .sheet(item: Binding(get: { contentID.map(ContentRoute.init) }, set: { contentID = $0?.id })) { ContentDetailView(contentID: $0.id) }
        .task { presentPendingShare() }
        .onChange(of: scenePhase) { _, phase in if phase == .active { presentPendingShare() } }
    }

    private func presentPendingShare() {
        guard addRoute == nil, let value = env.shared.pendingShare?.trimmingCharacters(in: .whitespacesAndNewlines), !value.isEmpty else { return }
        env.shared.pendingShare = nil
        addRoute = AddRoute(text: value)
    }
}
private struct AddRoute: Identifiable { let id = UUID(); let text: String }
private struct ContentRoute: Identifiable { let id: String }
private struct PendingParityView: View { let title: String; var body: some View { NavigationStack { Color.pingletBackground.ignoresSafeArea().navigationTitle(title) } } }
