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

private enum WidgetSelector {
    static func effective(_ stored: WidgetProfile, plus: Bool) -> WidgetProfile {
        guard !plus else { return stored }
        var value = stored; value.theme = "BLEND"; value.contentMode = "MIXED"; value.catalogIds = []; value.scheduleMode = "ANYTIME"; value.typography = "EDITORIAL"; value.spacing = "COMFORTABLE"; value.manualNext = false
        return value
    }
    static func select(feed: [FeedItem], profile: WidgetProfile, key: String, date: Date) -> FeedItem? {
        guard !feed.isEmpty else { return nil }
        let byMode = feed.filter { item in
            switch profile.contentMode {
            case "PERSONAL": return item.source == .personal
            case "COLLECTIONS": return item.source == .system && (profile.catalogIds.isEmpty || !Set(item.catalogIds).isDisjoint(with: profile.catalogIds))
            default: return profile.catalogIds.isEmpty || item.source == .personal || !Set(item.catalogIds).isDisjoint(with: profile.catalogIds)
            }
        }
        let base = byMode.isEmpty ? feed : byMode
        let terms = contextualTerms(profile.scheduleMode, date)
        let contextual = base.filter { item in terms.contains { "\(item.categories.joined(separator: " ")) \(item.author ?? "") \(item.text)".lowercased().contains($0) } }
        let candidates = contextual.isEmpty ? base : contextual
        let ranked = candidates.sorted {
            score($0, profile, terms) > score($1, profile, terms)
        }
        let withoutCurrent = ranked.filter { $0.id != profile.currentContentId }
        let pool = withoutCurrent.isEmpty ? ranked : withoutCurrent
        let day = Calendar.current.ordinality(of: .day, in: .era, for: date) ?? 0
        let seed = abs(stableHash(key) + profile.manualOffset + day)
        return pool[seed % pool.count]
    }
    private static func score(_ item: FeedItem, _ profile: WidgetProfile, _ terms: [String]) -> Int {
        (item.favorite ? 8 : 0) + (profile.contentMode != "COLLECTIONS" && item.source == .personal ? 4 : 0) + terms.filter { "\(item.categories.joined(separator: " ")) \(item.text)".lowercased().contains($0) }.count
    }
    private static func contextualTerms(_ mode: String, _ date: Date) -> [String] {
        let hour = Calendar.current.component(.hour, from: date), weekday = Calendar.current.component(.weekday, from: date)
        if mode == "DAY_RHYTHM" { if 5...11 ~= hour { return ["morning","motivation","discipline","drive","focus"] }; if 18...23 ~= hour { return ["reflection","calm","gratitude","affirmation","faith"] } }
        if mode == "CONTEXTUAL" { if weekday == 1 || weekday == 7 { return ["life","family","calm","fitness","reflection"] }; if 8...17 ~= hour { return ["business","focus","discipline","confidence","goal"] }; return ["reflection","affirmation","gratitude","calm"] }
        return []
    }
    private static func stableHash(_ value: String) -> Int { value.utf8.reduce(2_166_136_261) { (result, byte) in (result ^ Int(byte)) &* 16_777_619 } }
}

struct PingLetProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> PingLetEntry { PingLetEntry(date: .now, key: "default", profile: WidgetProfile(currentText: "What you keep should find its way back."), isPlus: false) }
    func snapshot(for configuration: PingLetWidgetConfiguration, in context: Context) async -> PingLetEntry { entry(configuration.profile.rawValue, .now) }
    func timeline(for configuration: PingLetWidgetConfiguration, in context: Context) async -> Timeline<PingLetEntry> {
        let calendar = Calendar.current, now = Date(), minute = calendar.component(.minute, from: now)
        let first = calendar.date(byAdding: .minute, value: minute < 30 ? 30 - minute : 60 - minute, to: now) ?? now.addingTimeInterval(1800)
        let dates = [now] + (0..<12).compactMap { calendar.date(byAdding: .minute, value: $0 * 30, to: first) }
        return Timeline(entries: dates.map { entry(configuration.profile.rawValue, $0) }, policy: .atEnd)
    }
    private func entry(_ key: String, _ date: Date) -> PingLetEntry {
        let store = SharedStore(), plus = store.entitlement?.plan == "PLUS"
        let stored = store.widgetProfile(key: key), effective = WidgetSelector.effective(stored, plus: plus)
        guard let item = WidgetSelector.select(feed: store.feed, profile: effective, key: key, date: date) else { return PingLetEntry(date: date, key: key, profile: effective, isPlus: plus) }
        var shown = effective; shown.currentContentId = item.id; shown.currentText = item.text; shown.currentAuthor = item.author; shown.currentSourceUrl = item.sourceUrl; shown.currentFavorite = item.favorite; shown.shownAt = Int64(date.timeIntervalSince1970 * 1000); shown.nextChangeAt = shown.shownAt + 1_800_000
        return PingLetEntry(date: date, key: key, profile: shown, isPlus: plus)
    }
}

struct FavoriteIntent: AppIntent {
    static var title: LocalizedStringResource = "Favorite PingLet"; @Parameter var contentID: String; @Parameter var profileKey: String; @Parameter var favorite: Bool
    init() {}
    init(contentID: String, profileKey: String, favorite: Bool) {
        self.contentID = contentID
        self.profileKey = profileKey
        self.favorite = favorite
    }
    func perform() async throws -> some IntentResult {
        let store = SharedStore(); var profile = store.widgetProfile(key: profileKey); profile.currentFavorite = favorite; store.setWidgetProfile(profile, key: profileKey); store.queueFavorite(contentID: contentID, favorite: favorite); WidgetCenter.shared.reloadAllTimelines(); return .result()
    }
}
struct NextIntent: AppIntent {
    static var title: LocalizedStringResource = "Show another"; @Parameter var profileKey: String
    init() {}
    init(profileKey: String) {
        self.profileKey = profileKey
    }
    func perform() async throws -> some IntentResult { let store = SharedStore(); guard store.entitlement?.plan == "PLUS" else { return .result() }; var profile = store.widgetProfile(key: profileKey); profile.manualOffset += 1; store.setWidgetProfile(profile, key: profileKey); WidgetCenter.shared.reloadAllTimelines(); return .result() }
}

struct PingLetWidgetView: View {
    let entry: PingLetEntry
    private var colors: (Color, Color, Color) {
        switch entry.profile.theme {
        case "FOREST":
            return (
                Color(red: 0.09, green: 0.19, blue: 0.16),
                .white,
                Color(red: 0.56, green: 0.82, blue: 0.67)
            )
        case "CLAY":
            return (
                Color(red: 0.32, green: 0.16, blue: 0.14),
                .white,
                Color(red: 0.94, green: 0.66, blue: 0.47)
            )
        default:
            return (
                Color(red: 0.06, green: 0.07, blue: 0.06),
                .white,
                Color.widgetGold
            )
        }
    }
    var body: some View { VStack(alignment: .leading, spacing: entry.profile.spacing == "COMPACT" ? 7 : 11) {
        HStack { Circle().fill(colors.2).frame(width: 8); Text("PINGLET").font(.caption.bold()); Spacer(); if entry.isPlus && entry.profile.manualNext { Button(intent: NextIntent(profileKey: entry.key)) { Image(systemName: "arrow.right") }.buttonStyle(.plain) }; if !entry.profile.currentContentId.isEmpty { Button(intent: FavoriteIntent(contentID: entry.profile.currentContentId, profileKey: entry.key, favorite: !entry.profile.currentFavorite)) { Image(systemName: entry.profile.currentFavorite ? "heart.fill" : "heart") }.buttonStyle(.plain) } }
        Link(destination: URL(string: "pinglet://content/\(entry.profile.currentContentId)")!) { Text(cleanWidgetText(entry.profile.currentText.isEmpty ? "Add something worth keeping and it will live here." : entry.profile.currentText)).font(entry.profile.textScale == "LARGE" ? .title2 : .title3).fontDesign(entry.profile.typography == "EDITORIAL" ? .serif : .default).frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading) }.buttonStyle(.plain)
        Rectangle().fill(colors.2).frame(width: 28, height: 2); Text(entry.profile.currentAuthor?.uppercased() ?? "A THOUGHT WORTH KEEPING").font(.caption2).foregroundStyle(.secondary)
    }.foregroundStyle(colors.1).containerBackground(colors.0.opacity(Double(entry.profile.opacity) / 100), for: .widget) }
}
private func cleanWidgetText(_ value: String) -> String { value.replacingOccurrences(of: #"([.!?])\s+(?:\d{1,3}(?:[,.]\d{3})*|\d+(?:\.\d+)?[KkMmBb])\s*$"#, with: "$1", options: .regularExpression) }

@main struct PingLetWidgetBundle: WidgetBundle {
    var body: some Widget { AppIntentConfiguration(kind: "PingLetWidget", intent: PingLetWidgetConfiguration.self, provider: PingLetProvider()) { PingLetWidgetView(entry: $0) }.configurationDisplayName("PingLet").description("Keep one meaningful idea within reach.").supportedFamilies([.systemMedium, .systemLarge]) }
}
