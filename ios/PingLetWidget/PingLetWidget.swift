import AppIntents
import SwiftUI
import WidgetKit

private extension Color { static let widgetGold = Color(red: 0.90, green: 0.66, blue: 0.16) }

enum ProfileChoice: String, AppEnum {
    case defaultProfile = "default", profile2, profile3
    static var typeDisplayRepresentation = TypeDisplayRepresentation(name: "PingLet profile")
    static var caseDisplayRepresentations: [Self: DisplayRepresentation] = [.defaultProfile: "Default", .profile2: "Widget 2", .profile3: "Widget 3"]
}
struct PingLetWidgetConfiguration: WidgetConfigurationIntent {
    static var title: LocalizedStringResource = "PingLet profile"
    static var description = IntentDescription("Choose the independent profile used by this widget.")
    @Parameter(title: "Profile", default: .defaultProfile) var profile: ProfileChoice
}
struct PingLetEntry: TimelineEntry { let date: Date; let key: String; let profile: WidgetProfile; let isPlus: Bool }

struct PingLetProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> PingLetEntry { PingLetEntry(date: .now, key: "default", profile: WidgetProfile(currentText: "What you keep should find its way back."), isPlus: false) }
    func snapshot(for configuration: PingLetWidgetConfiguration, in context: Context) async -> PingLetEntry { entry(configuration.profile.rawValue, .now) }
    func timeline(for configuration: PingLetWidgetConfiguration, in context: Context) async -> Timeline<PingLetEntry> {
        let calendar = Calendar.current, now = Date()
        let first = Date(timeIntervalSince1970: (floor(now.timeIntervalSince1970 / 1800) + 1) * 1800)
        let dates = [now] + (0..<12).compactMap { calendar.date(byAdding: .minute, value: $0 * 30, to: first) }
        return Timeline(entries: dates.map { entry(configuration.profile.rawValue, $0) }, policy: .atEnd)
    }
    private func entry(_ key: String, _ date: Date) -> PingLetEntry {
        let store = SharedStore(), plus = store.entitlement?.plan == "PLUS"
        let shown = SharedWidgetSelector.resolvedProfile(feed: store.feed, stored: store.widgetProfile(key: key), plus: plus, key: key, date: date)
        return PingLetEntry(date: date, key: key, profile: shown, isPlus: plus)
    }
}

struct FavoriteIntent: AppIntent {
    static var title: LocalizedStringResource = "Favorite PingLet"
    @Parameter(title: "Content ID") var contentID: String
    @Parameter(title: "Profile") var profileKey: String
    @Parameter(title: "Favorite") var favorite: Bool
    init() {}
    init(contentID: String, profileKey: String, favorite: Bool) {
        self.contentID = contentID
        self.profileKey = profileKey
        self.favorite = favorite
    }
    func perform() async throws -> some IntentResult {
        let store = SharedStore()
        var profile = store.widgetProfile(key: profileKey)
        guard store.feed.contains(where: { $0.id == contentID }) else { return .result() }
        if profile.currentContentId == contentID {
            profile.currentFavorite = favorite
            store.setWidgetProfile(profile, key: profileKey)
        }
        store.queueFavorite(contentID: contentID, favorite: favorite)
        WidgetCenter.shared.reloadTimelines(ofKind: "PingLetWidget")
        return .result()
    }
}
struct NextIntent: AppIntent {
    static var title: LocalizedStringResource = "Show another"
    @Parameter(title: "Profile") var profileKey: String
    init() {}
    init(profileKey: String) {
        self.profileKey = profileKey
    }
    func perform() async throws -> some IntentResult { let store = SharedStore(); guard store.entitlement?.plan == "PLUS" else { return .result() }; var profile = store.widgetProfile(key: profileKey); profile.manualOffset += 1; store.setWidgetProfile(profile, key: profileKey); WidgetCenter.shared.reloadAllTimelines(); return .result() }
}

struct PingLetWidgetView: View {
    let entry: PingLetEntry
    @Environment(\.widgetFamily) private var family
    var body: some View {
        let artwork = PingLetWidgetArtwork(profile: entry.profile, large: family == .systemLarge) {
            if entry.isPlus && entry.profile.manualNext {
                Button(intent: NextIntent(profileKey: entry.key)) { Image(systemName: "arrow.right").frame(width: 44, height: 36) }
                    .buttonStyle(.plain).accessibilityLabel("Show another PingLet")
            }
            if !entry.profile.currentContentId.isEmpty {
                Button(intent: FavoriteIntent(contentID: entry.profile.currentContentId, profileKey: entry.key, favorite: !entry.profile.currentFavorite)) {
                    Image(systemName: entry.profile.currentFavorite ? "heart.fill" : "heart").frame(width: 44, height: 36)
                }.buttonStyle(.plain).accessibilityLabel(entry.profile.currentFavorite ? "Remove from favorites" : "Add to favorites")
            }
        }
        artwork.containerBackground(for: .widget) {
            artwork.surface.opacity(Double(entry.profile.opacity) / 100)
        }
    }
}
private func cleanWidgetText(_ value: String) -> String { value.replacingOccurrences(of: #"([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$"#, with: "$1", options: .regularExpression) }

struct PingLetWidget: Widget {
    var body: some WidgetConfiguration {
        AppIntentConfiguration(
            kind: "PingLetWidget",
            intent: PingLetWidgetConfiguration.self,
            provider: PingLetProvider()
        ) { entry in
            PingLetWidgetView(entry: entry)
        }
        .configurationDisplayName("PingLet")
        .description("Keep one meaningful idea within reach.")
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}

@main struct PingLetWidgetBundle: WidgetBundle {
    var body: some Widget {
        PingLetWidget()
    }
}
