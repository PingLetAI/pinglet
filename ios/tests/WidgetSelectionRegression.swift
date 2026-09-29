import Foundation

@main struct WidgetSelectionRegression {
    static func main() throws {
        let data = Data(#"[{"id":"a","text":"A personal thought","type":"QUOTE","source":"PERSONAL"},{"id":"b","text":"A curated thought","type":"QUOTE","source":"SYSTEM","catalogIds":["business"]},{"id":"c","text":"Another personal thought","type":"QUOTE","source":"PERSONAL"}]"#.utf8)
        let feed = try JSONDecoder().decode([FeedItem].self, from: data)
        let date = Date(timeIntervalSince1970: 1_800_000)
        var stored = WidgetProfile()
        stored.currentContentId = "deleted"
        stored.currentText = "Deleted text"
        stored.currentAuthor = "Old author"
        stored.currentFavorite = true
        let empty = SharedWidgetSelector.resolvedProfile(feed: [], stored: stored, plus: true, key: "default", date: date)
        precondition(empty.currentContentId.isEmpty && empty.currentText.isEmpty && empty.currentAuthor == nil && !empty.currentFavorite)
        stored.contentMode = "PERSONAL"
        precondition(SharedWidgetSelector.select(feed: [feed[1]], profile: stored, key: "default", date: date) == nil)
        stored.contentMode = "COLLECTIONS"
        stored.catalogIds = ["missing"]
        precondition(SharedWidgetSelector.select(feed: feed, profile: stored, key: "default", date: date) == nil)
        stored.catalogIds = ["business"]
        precondition(SharedWidgetSelector.select(feed: feed, profile: stored, key: "default", date: date)?.id == "b")
        stored = WidgetProfile()
        let first = SharedWidgetSelector.resolvedProfile(feed: feed, stored: stored, plus: true, key: "default", date: date)
        let repeatEntry = SharedWidgetSelector.resolvedProfile(feed: feed.reversed(), stored: first, plus: true, key: "default", date: date)
        precondition(first.currentContentId == repeatEntry.currentContentId)
        var favorited = feed
        for index in favorited.indices { favorited[index].favorite = true }
        precondition(SharedWidgetSelector.select(feed: favorited, profile: stored, key: "default", date: date)?.id == first.currentContentId)
        stored.manualOffset = 1
        precondition(SharedWidgetSelector.select(feed: feed, profile: stored, key: "default", date: date)?.id != first.currentContentId)
        stored.manualOffset = 0
        precondition(SharedWidgetSelector.select(feed: feed, profile: stored, key: "default", date: date.addingTimeInterval(1800))?.id != first.currentContentId)
        print("Widget regressions passed: empty cache, source filters, stable favorites, deterministic selection, next and scheduled rotation.")
    }
}
