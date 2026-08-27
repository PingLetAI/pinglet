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
        }; Button { adding = true } label: { Image(systemName: "plus").font(.title2.bold()).frame(width: 54, height: 54).background(Color.pingletGold, in: Circle()).foregroundStyle(Color.pingletInk) }.padding(.bottom, 6) }
        .sheet(isPresented: $adding) { AddPingLetView() }
        .sheet(item: Binding(get: { contentID.map(ContentRoute.init) }, set: { contentID = $0?.id })) { ContentDetailView(contentID: $0.id) }
    }
}
private struct ContentRoute: Identifiable { let id: String }
private struct PendingParityView: View { let title: String; var body: some View { NavigationStack { Color.pingletBackground.ignoresSafeArea().navigationTitle(title) } } }
