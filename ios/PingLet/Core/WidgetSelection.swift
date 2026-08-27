import Foundation

enum SharedWidgetSelector {
    static func effective(_ stored: WidgetProfile, plus: Bool) -> WidgetProfile {
        guard !plus else { return stored }
        var value = stored
        value.theme = "BLEND"
        value.contentMode = "MIXED"
        value.catalogIds = []
        value.scheduleMode = "ANYTIME"
        value.typography = "EDITORIAL"
        value.spacing = "COMFORTABLE"
        value.manualNext = false
        return value
    }

    static func resolvedProfile(feed: [FeedItem], stored: WidgetProfile, plus: Bool, key: String, date: Date) -> WidgetProfile {
        let effective = effective(stored, plus: plus)
        guard let item = select(feed: feed, profile: effective, key: key, date: date) else { return effective }
        var shown = effective
        shown.currentContentId = item.id
        shown.currentText = item.text
        shown.currentAuthor = item.author
        shown.currentSourceUrl = item.sourceUrl
        shown.currentFavorite = item.favorite
        shown.shownAt = Int64(date.timeIntervalSince1970 * 1000)
        shown.nextChangeAt = (Int64(date.timeIntervalSince1970 / 1_800) + 1) * 1_800_000
        return shown
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
        let ranked = candidates.sorted { score($0, profile, terms) > score($1, profile, terms) }
        let withoutCurrent = ranked.filter { $0.id != profile.currentContentId }
        let pool = withoutCurrent.isEmpty ? ranked : withoutCurrent
        let halfHourSlot = Int(date.timeIntervalSince1970 / 1_800)
        let seed = abs(stableHash(key) + profile.manualOffset + halfHourSlot)
        return pool[seed % pool.count]
    }

    private static func score(_ item: FeedItem, _ profile: WidgetProfile, _ terms: [String]) -> Int {
        (item.favorite ? 8 : 0) + (profile.contentMode != "COLLECTIONS" && item.source == .personal ? 4 : 0) + terms.filter { "\(item.categories.joined(separator: " ")) \(item.text)".lowercased().contains($0) }.count
    }

    private static func contextualTerms(_ mode: String, _ date: Date) -> [String] {
        let hour = Calendar.current.component(.hour, from: date), weekday = Calendar.current.component(.weekday, from: date)
        if mode == "DAY_RHYTHM" { if 5...11 ~= hour { return ["morning", "motivation", "discipline", "drive", "focus"] }; if 18...23 ~= hour { return ["reflection", "calm", "gratitude", "affirmation", "faith"] } }
        if mode == "CONTEXTUAL" { if weekday == 1 || weekday == 7 { return ["life", "family", "calm", "fitness", "reflection"] }; if 8...17 ~= hour { return ["business", "focus", "discipline", "confidence", "goal"] }; return ["reflection", "affirmation", "gratitude", "calm"] }
        return []
    }

    private static func stableHash(_ value: String) -> Int { value.utf8.reduce(2_166_136_261) { (result, byte) in (result ^ Int(byte)) &* 16_777_619 } }
}
