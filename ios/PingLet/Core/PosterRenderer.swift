import UIKit

struct PosterContent: Identifiable {
    let id: String
    let text: String
    let author: String?
    let sourceURL: String?
}

enum PosterFormat: String, CaseIterable, Identifiable {
    case portrait = "Portrait", square = "Square", story = "Story"
    var id: String { rawValue }
    var size: CGSize { CGSize(width: 1080, height: self == .portrait ? 1350 : self == .square ? 1080 : 1920) }
    var dimensions: String { "1080 × \(Int(size.height))" }
}

enum PosterTheme: String, CaseIterable, Identifiable {
    case paper = "Paper", ink = "Ink", forest = "Forest"
    var id: String { rawValue }
    var background: UIColor {
        switch self {
        case .paper: return UIColor(red: 0.97, green: 0.95, blue: 0.90, alpha: 1)
        case .ink: return UIColor(red: 0.06, green: 0.075, blue: 0.065, alpha: 1)
        case .forest: return UIColor(red: 0.075, green: 0.19, blue: 0.15, alpha: 1)
        }
    }
    var foreground: UIColor { self == .paper ? UIColor(red: 0.10, green: 0.13, blue: 0.11, alpha: 1) : UIColor(red: 0.98, green: 0.97, blue: 0.93, alpha: 1) }
    var accent: UIColor { self == .paper ? UIColor(red: 0.51, green: 0.30, blue: 0.12, alpha: 1) : UIColor(red: 0.92, green: 0.75, blue: 0.40, alpha: 1) }
}

enum PosterError: LocalizedError {
    case empty, tooLong, attributionTooLong, invalidExcerpt
    var errorDescription: String? {
        switch self {
        case .empty: return "Choose some words to share."
        case .tooLong: return "This PingLet is too long for a readable poster. Choose a shorter excerpt or try Story."
        case .attributionTooLong: return "The creator attribution is too long for this poster. You can still copy and share the text."
        case .invalidExcerpt: return "Use a continuous excerpt from the original PingLet. Its wording and creator attribution stay intact."
        }
    }
}

/// Preview and export use this one renderer, at exact pixel dimensions.
@MainActor enum PosterRenderer {
    struct Layout {
        let text: NSAttributedString
        let textRect: CGRect
        let author: NSAttributedString?
        let authorRect: CGRect
        let fontSize: CGFloat
        let isExcerpt: Bool
    }
    static func layout(content: PosterContent, excerpt: String, format: PosterFormat, theme: PosterTheme) throws -> Layout {
        let original = content.text.trimmingCharacters(in: .whitespacesAndNewlines)
        let text = excerpt.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty else { throw PosterError.empty }
        guard original.contains(text) else { throw PosterError.invalidExcerpt }
        guard text.utf16.count < 12000 else { throw PosterError.tooLong }
        let width: CGFloat = 912
        let top: CGFloat = format == .story ? 340 : 230
        let bottom = format.size.height - (format == .story ? 310 : 210)
        let authorText = content.author?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let author = authorText.isEmpty ? nil : NSAttributedString(string: authorText, attributes: [.font: UIFont.systemFont(ofSize: 28, weight: .medium), .foregroundColor: theme.foreground])
        let authorHeight = author.map { measured($0, width: width).height } ?? 0
        guard authorHeight <= 110 else { throw PosterError.attributionTooLong }
        let available = bottom - top - authorHeight - (author == nil ? 0 : 48)
        let maxFont: CGFloat = text.count < 130 ? 82 : text.count < 300 ? 68 : 56
        for step in stride(from: Int(maxFont), through: 36, by: -2) {
            let fontSize = CGFloat(step)
            let base = UIFont.systemFont(ofSize: fontSize, weight: .regular)
            let font = UIFont(descriptor: base.fontDescriptor.withDesign(.serif) ?? base.fontDescriptor, size: fontSize)
            let paragraph = NSMutableParagraphStyle()
            paragraph.lineSpacing = fontSize * 0.16
            paragraph.lineBreakMode = .byWordWrapping
            let attributed = NSAttributedString(string: text, attributes: [.font: font, .foregroundColor: theme.foreground, .paragraphStyle: paragraph])
            let size = measured(attributed, width: width)
            if size.height <= available {
                let y = top + max(0, (available - size.height) * 0.42)
                let rect = CGRect(x: 84, y: y, width: width, height: ceil(size.height) + 2)
                return Layout(text: attributed, textRect: rect, author: author,
                              authorRect: CGRect(x: 84, y: rect.maxY + 48, width: width, height: ceil(authorHeight) + 2), fontSize: fontSize, isExcerpt: text != original)
            }
        }
        throw PosterError.tooLong
    }
    static func image(content: PosterContent, excerpt: String, format: PosterFormat, theme: PosterTheme) throws -> UIImage {
        let layout = try layout(content: content, excerpt: excerpt, format: format, theme: theme)
        let configuration = UIGraphicsImageRendererFormat()
        configuration.scale = 1
        configuration.opaque = true
        return UIGraphicsImageRenderer(size: format.size, format: configuration).image { context in
            theme.background.setFill()
            context.fill(CGRect(origin: .zero, size: format.size))
            let headerY: CGFloat = format == .story ? 210 : 100
            let footerY = format.size.height - (format == .story ? 210 : 104)
            let brand = NSAttributedString(string: "PINGLET", attributes: [.font: UIFont.systemFont(ofSize: 26, weight: .semibold), .foregroundColor: theme.foreground, .kern: 5])
            brand.draw(at: CGPoint(x: 84, y: headerY))
            theme.accent.setFill()
            context.fill(CGRect(x: 84, y: headerY + 61, width: 38, height: 4))
            layout.text.draw(with: layout.textRect, options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil)
            layout.author?.draw(with: layout.authorRect, options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil)
            let footer = NSAttributedString(string: layout.isExcerpt ? "EXCERPT · PINGLET.AI" : "PINGLET.AI", attributes: [.font: UIFont.systemFont(ofSize: 19, weight: .medium), .foregroundColor: theme.foreground.withAlphaComponent(0.75), .kern: 2])
            footer.draw(at: CGPoint(x: 84, y: footerY))
        }
    }
    private static func measured(_ text: NSAttributedString, width: CGFloat) -> CGRect {
        text.boundingRect(with: CGSize(width: width, height: .greatestFiniteMagnitude), options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil)
    }
}
