import Foundation

@MainActor final class AppEnvironment: ObservableObject {
    let shared = SharedStore(); let secure = SecureStore(); let api = APIClient(); lazy var session = SessionManager(api: api, secure: secure, shared: shared)
    @Published var entitlement: Entitlement?
    @Published var feed: [FeedItem] = []
    @Published var library: [UserContent] = []
    func bootstrap() async { feed = shared.feed; library = shared.library; await refreshEntitlement(); await flushPendingFavorites(); await syncFeed() }
    func refreshEntitlement() async { entitlement = try? await session.perform("/api/v1/me/entitlements"); shared.entitlement = entitlement }
    func syncFeed() async {
        if let response: FeedResponse = try? await session.perform("/api/v1/me/feed?limit=200") { feed = response.items; shared.feed = feed }
    }
    func refreshLibrary() async throws {
        let rows: [UserContent] = try await session.perform("/api/v1/me/content")
        library = rows.filter { !$0.archived }; shared.library = library
    }
    func setFavorite(_ id: String, _ favorite: Bool) async throws {
        shared.queueFavorite(contentID: id, favorite: favorite)
        library = library.map { row in var copy = row; if copy.contentItemId == id { copy.favorite = favorite }; return copy }
        shared.library = library
        let _: BoolResponse = try await session.perform("/api/v1/me/content/\(id)/favorite", method: favorite ? .post : .delete, body: EmptyBody())
        shared.pendingFavorites.removeAll { $0.contentID == id }
    }
    func signOut() async throws {
        let _: BoolResponse = try await session.perform("/api/v1/auth/logout", method: .post, body: EmptyBody())
        try await session.resetToAnonymous(); feed = []; library = []; await bootstrap()
    }
    func flushPendingFavorites() async {
        for action in shared.pendingFavorites.sorted(by: { $0.createdAt < $1.createdAt }) {
            do {
                let _: BoolResponse = try await session.perform("/api/v1/me/content/\(action.contentID)/favorite", method: action.favorite ? .post : .delete, body: EmptyBody())
                shared.pendingFavorites.removeAll { $0.id == action.id }
            } catch { return }
        }
    }
    func track(_ type: String, metadata: String? = nil) async {
        let event = EventPayload(type: type, contentItemId: nil, surface: "APP", timestamp: ISO8601DateFormatter().string(from: Date()), metadata: metadata)
        let _: [String: Int]? = try? await session.perform("/api/v1/events/batch", method: .post, body: EventBatch(events: [event]))
    }
}
