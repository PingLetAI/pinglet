import SwiftUI

@main struct PingLetApp: App {
    @StateObject private var environment = AppEnvironment()
    @StateObject private var subscriptions = AppleSubscriptionManager()
    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(environment)
                .environmentObject(subscriptions)
                .task {
                    subscriptions.start(environment)
                    await environment.bootstrap()
                    await subscriptions.prepare(environment)
                }
        }
    }
}
