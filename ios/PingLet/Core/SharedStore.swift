import Foundation

final class SharedStore {
    static let appGroup = "group.ai.pinglet.app"
    private let defaults: UserDefaults
    init() { defaults = UserDefaults(suiteName: Self.appGroup) ?? .standard }
    var installationID: String {
        if let value = defaults.string(forKey: "installation_id"), !value.isEmpty { return value }
        let value = UUID().uuidString; defaults.set(value, forKey: "installation_id"); return value
    }
    var entitlement: Entitlement? { get { decode("entitlement") } set { encode(newValue, "entitlement") } }
    var feed: [FeedItem] { get { decode("feed") ?? [] } set { encode(newValue, "feed") } }
    var library: [UserContent] { get { decode("library") ?? [] } set { encode(newValue, "library") } }
    var pendingFavorites: [PendingFavorite] { get { decode("pending_favorites") ?? [] } set { encode(newValue, "pending_favorites") } }
    var contentMix: String { get { defaults.string(forKey: "personal_system_mix") ?? "BALANCED" } set { defaults.set(newValue, forKey: "personal_system_mix") } }
    var pendingShare: String? { get { defaults.string(forKey: "pending_share") } set { if let newValue { defaults.set(newValue, forKey: "pending_share") } else { defaults.removeObject(forKey: "pending_share") } } }
    func savePendingShare(_ value: String) {
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        pendingShare = trimmed
        defaults.synchronize()
    }
    func widgetProfile(key: String) -> WidgetProfile { decode("widget_profile_\(key)") ?? WidgetProfile() }
    func setWidgetProfile(_ profile: WidgetProfile, key: String) { encode(profile, "widget_profile_\(key)") }
    func queueFavorite(contentID: String, favorite: Bool) {
        var feedRows = feed
        for index in feedRows.indices where feedRows[index].id == contentID {
            feedRows[index].favorite = favorite
        }
        feed = feedRows

        var libraryRows = library
        for index in libraryRows.indices where libraryRows[index].contentItemId == contentID {
            libraryRows[index].favorite = favorite
        }
        library = libraryRows

        var rows = pendingFavorites.filter { $0.contentID != contentID }
        rows.append(PendingFavorite(id: UUID(), contentID: contentID, favorite: favorite, createdAt: .now))
        pendingFavorites = rows
        defaults.synchronize()
    }
    func clearAccountData() {
        for key in defaults.dictionaryRepresentation().keys where key != "installation_id" { defaults.removeObject(forKey: key) }
    }
    private func decode<T: Decodable>(_ key: String) -> T? { defaults.data(forKey: key).flatMap { try? JSONDecoder().decode(T.self, from: $0) } }
    private func encode<T: Encodable>(_ value: T?, _ key: String) { if let value, let data = try? JSONEncoder().encode(value) { defaults.set(data, forKey: key) } else { defaults.removeObject(forKey: key) } }
}
