import Foundation
import SwiftUI

struct PingLetWidgetArtwork<Controls: View>: View {
    let profile: WidgetProfile
    let large: Bool
    @ViewBuilder var controls: Controls
    private var empty: Bool { profile.currentContentId.isEmpty }
    var surface: Color {
        switch profile.theme {
        case "INK": return Color(red: 0.06, green: 0.07, blue: 0.06)
        case "FOREST": return Color(red: 0.09, green: 0.19, blue: 0.16)
        case "CLAY": return Color(red: 0.32, green: 0.16, blue: 0.14)
        default: return Color(red: 0.96, green: 0.94, blue: 0.89)
        }
    }
    private var ink: Color { profile.theme == "BLEND" ? Color(red: 0.09, green: 0.12, blue: 0.10) : .white }
    private var accent: Color { profile.theme == "BLEND" ? Color(red: 0.50, green: 0.26, blue: 0.08) : Color(red: 0.94, green: 0.76, blue: 0.39) }
    private var gap: CGFloat { profile.spacing == "COMPACT" ? 5 : profile.spacing == "AIRY" ? 13 : 9 }
    private var font: Font {
        let size: CGFloat = profile.textScale == "LARGE" ? (large ? 29 : 22) : profile.textScale == "MEDIUM" ? (large ? 25 : 19) : (large ? 22 : 17)
        return .system(size: size, weight: profile.typography == "COMPACT" ? .medium : .regular, design: profile.typography == "EDITORIAL" ? .serif : profile.typography == "COMPACT" ? .rounded : .default)
    }
    private var destination: URL { URL(string: empty ? "pinglet://add" : "pinglet://content/\(profile.currentContentId)")! }
    private var message: String {
        guard empty else { return profile.currentText }
        if profile.contentMode == "COLLECTIONS" { return "Your next discovery starts in Explore." }
        return "Keep a little thought. Make room for a good idea."
    }
    var body: some View {
        VStack(alignment: .leading, spacing: gap) {
            HStack(spacing: 6) {
                Image(systemName: "sparkle").foregroundStyle(accent)
                Text("PINGLET").font(.system(size: 11, weight: .bold)).tracking(1.5)
                Spacer(minLength: 4)
                controls
            }
            Link(destination: empty && profile.contentMode == "COLLECTIONS" ? URL(string: "pinglet://explore")! : destination) {
                Text(message).font(font)
                    .lineLimit(large ? 11 : 4).minimumScaleFactor(0.85)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
                    .multilineTextAlignment(.leading)
            }.buttonStyle(.plain)
            HStack(alignment: .center) {
                Text(empty ? "TAP TO GET STARTED" : profile.currentAuthor ?? "A thought worth keeping")
                    .font(.system(size: 11, weight: .medium)).lineLimit(1)
                Spacer(minLength: 8)
                Image(systemName: "arrow.up.right").font(.system(size: 10, weight: .semibold))
            }.foregroundStyle(ink.opacity(0.8))
            if large { Rectangle().fill(accent).frame(width: 30, height: 2) }
        }.foregroundStyle(ink)
    }
}

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
        guard let item = select(feed: feed, profile: effective, key: key, date: date) else {
            var empty = effective
            empty.currentContentId = ""
            empty.currentText = ""
            empty.currentAuthor = nil
            empty.currentSourceUrl = nil
            empty.currentFavorite = false
            empty.nextChangeAt = 0
            return empty
        }
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
        let base = byMode
        guard !base.isEmpty else { return nil }
        let terms = contextualTerms(profile.scheduleMode, date)
        let contextual = base.filter { item in terms.contains { "\(item.categories.joined(separator: " ")) \(item.author ?? "") \(item.text)".lowercased().contains($0) } }
        let candidates = contextual.isEmpty ? base : contextual
        let pool = candidates.sorted {
            let lhs = score($0, profile, terms), rhs = score($1, profile, terms)
            return lhs == rhs ? $0.id < $1.id : lhs > rhs
        }
        let halfHourSlot = Int(date.timeIntervalSince1970 / 1_800)
        let seed = Int(UInt(bitPattern: stableHash(key) &+ profile.manualOffset &+ halfHourSlot) % UInt(pool.count))
        return pool[seed % pool.count]
    }

    private static func score(_ item: FeedItem, _ profile: WidgetProfile, _ terms: [String]) -> Int {
        (profile.contentMode != "COLLECTIONS" && item.source == .personal ? 4 : 0) + terms.filter { "\(item.categories.joined(separator: " ")) \(item.text)".lowercased().contains($0) }.count
    }

    private static func contextualTerms(_ mode: String, _ date: Date) -> [String] {
        let hour = Calendar.current.component(.hour, from: date), weekday = Calendar.current.component(.weekday, from: date)
        if mode == "DAY_RHYTHM" { if 5...11 ~= hour { return ["morning", "motivation", "discipline", "drive", "focus"] }; if 18...23 ~= hour { return ["reflection", "calm", "gratitude", "affirmation", "faith"] } }
        if mode == "CONTEXTUAL" { if weekday == 1 || weekday == 7 { return ["life", "family", "calm", "fitness", "reflection"] }; if 8...17 ~= hour { return ["business", "focus", "discipline", "confidence", "goal"] }; return ["reflection", "affirmation", "gratitude", "calm"] }
        return []
    }

    private static func stableHash(_ value: String) -> Int { value.utf8.reduce(2_166_136_261) { (result, byte) in (result ^ Int(byte)) &* 16_777_619 } }
}
