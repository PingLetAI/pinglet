import SwiftUI

struct RootView: View {
    enum Tab: Hashable { case home, library, explore, settings }
    @State private var tab: Tab = .home; @State private var adding = false; @State private var contentID: String?
    var body: some View {
        ZStack(alignment: .bottom) { TabView(selection: $tab) {
            HomeView(onOpen: { contentID = $0 }).tabItem { Label("Home", systemImage: "house.fill") }.tag(Tab.home)
            LibraryView(onOpen: { contentID = $0 }, onAdd: { adding = true }).tabItem { Label("Library", systemImage: "bookmark.fill") }.tag(Tab.library)
            ExploreView().tabItem { Label("Explore", systemImage: "safari.fill") }.tag(Tab.explore)
            SettingsView().tabItem { Label("Settings", systemImage: "gearshape.fill") }.tag(Tab.settings)
        }
        .tint(Color.pingletInk)
        Button { adding = true } label: {
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
        .sheet(isPresented: $adding) { AddPingLetView() }
        .sheet(item: Binding(get: { contentID.map(ContentRoute.init) }, set: { contentID = $0?.id })) { ContentDetailView(contentID: $0.id) }
    }
}
private struct ContentRoute: Identifiable { let id: String }
private struct PendingParityView: View { let title: String; var body: some View { NavigationStack { Color.pingletBackground.ignoresSafeArea().navigationTitle(title) } } }
