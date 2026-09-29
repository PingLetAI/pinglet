import XCTest
import UIKit

final class PosterRendererTests: XCTestCase {
    func testStoryIsDefault() {
        XCTAssertEqual(PosterFormat.defaultFormat, .story)
        XCTAssertEqual(PosterFormat.defaultFormat.size, CGSize(width: 1080, height: 1920))
        XCTAssertEqual(PosterFormat.allCases.first, .story)
    }
    private let text = "You do not need to have every answer before you begin. Keep showing up, pay attention to what works, and let small steps become something worth keeping."
    @MainActor func testEveryFormatAndThemeExportsExactSizeAndVisibleAttribution() throws {
        let content = PosterContent(id: "test", text: text, author: "A little perspective", sourceURL: nil)
        for format in PosterFormat.allCases {
            for theme in PosterTheme.allCases {
                let image = try PosterRenderer.image(content: content, excerpt: text, format: format, theme: theme)
                XCTAssertEqual(image.cgImage?.width, 1080)
                XCTAssertEqual(image.cgImage?.height, Int(format.size.height))
                let layout = try PosterRenderer.layout(content: content, excerpt: text, format: format, theme: theme)
                XCTAssertEqual(layout.text.string, text)
                XCTAssertEqual(layout.author?.string, "A little perspective")
                XCTAssertGreaterThanOrEqual(layout.fontSize, 36)
                XCTAssertLessThan(layout.textRect.maxY, layout.authorRect.minY)
                XCTAssertLessThan(layout.authorRect.maxY, format.size.height - 150)
                XCTAssertFalse(layout.isExcerpt)
                let attachment = XCTAttachment(image: image)
                attachment.name = "\(format.rawValue)-\(theme.rawValue)"
                attachment.lifetime = .keepAlways
                add(attachment)
            }
        }
    }
    @MainActor func testLongTextRequiresExcerptInsteadOfTruncating() throws {
        let long = String(repeating: text + " ", count: 20)
        let content = PosterContent(id: "long", text: long, author: "Original creator", sourceURL: nil)
        for format in PosterFormat.allCases {
            XCTAssertThrowsError(try PosterRenderer.image(content: content, excerpt: long, format: format, theme: .paper))
            let layout = try PosterRenderer.layout(content: content, excerpt: text, format: format, theme: .paper)
            XCTAssertTrue(layout.isExcerpt)
            XCTAssertEqual(layout.author?.string, content.author)
            XCTAssertEqual(layout.text.string, text)
        }
    }
    @MainActor func testRejectsEmptyAndChangedQuotes() throws {
        let content = PosterContent(id: "test", text: text, author: "Creator", sourceURL: nil)
        XCTAssertThrowsError(try PosterRenderer.image(content: content, excerpt: "  ", format: .portrait, theme: .ink))
        XCTAssertThrowsError(try PosterRenderer.image(content: content, excerpt: "Invented quote", format: .portrait, theme: .ink))
    }
    @MainActor func testLongCreatorNameNeverSilentlyTruncates() {
        let content = PosterContent(id: "test", text: text, author: String(repeating: "Creator ", count: 100), sourceURL: nil)
        XCTAssertThrowsError(try PosterRenderer.image(content: content, excerpt: text, format: .portrait, theme: .forest))
    }
}
