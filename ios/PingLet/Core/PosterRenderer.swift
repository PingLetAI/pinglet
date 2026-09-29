import UIKit

struct PosterContent: Identifiable {
    let id: String
    let text: String
    let author: String?
    let sourceURL: String?
}

enum PosterFormat: String, CaseIterable, Identifiable {
    case story = "Story", portrait = "Portrait", square = "Square"
    var id: String { rawValue }
    var size: CGSize { CGSize(width: 1080, height: self == .portrait ? 1350 : self == .square ? 1080 : 1920) }
    var dimensions: String { "1080 × \(Int(size.height))" }
    static let defaultFormat: PosterFormat = .story
    var label: String { rawValue }
    var guidance: String {
        switch self {
        case .portrait: return "4:5 · Instagram feed & everyday sharing"
        case .square: return "1:1 · Square posts"
        case .story: return "9:16 · TikTok & Instagram Stories"
        }
    }
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
        let width: CGFloat = format == .story ? 816 : 880
        let top: CGFloat = format == .story ? 340 : 230
        let bottom = format.size.height - (format == .story ? 400 : 230)
        let authorText = content.author?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        let author = authorText.isEmpty ? nil : NSAttributedString(string: authorText, attributes: [.font: UIFont.systemFont(ofSize: 28, weight: .medium), .foregroundColor: theme.foreground])
        let authorHeight = author.map { measured($0, width: width - 52).height } ?? 0
        guard authorHeight <= 110 else { throw PosterError.attributionTooLong }
        let available = bottom - top - authorHeight - (author == nil ? 0 : 48)
        let maxFont: CGFloat = text.count < 130 ? 82 : text.count < 300 ? 68 : 56
        for step in stride(from: Int(maxFont), through: 36, by: -2) {
            let fontSize = CGFloat(step)
            let base = UIFont.systemFont(ofSize: fontSize, weight: .regular)
            let font = UIFont(descriptor: base.fontDescriptor.withDesign(.serif) ?? base.fontDescriptor, size: fontSize)
            let paragraph = NSMutableParagraphStyle()
            paragraph.lineSpacing = fontSize * 0.12
            paragraph.lineBreakMode = .byWordWrapping
            let attributed = NSAttributedString(string: text, attributes: [.font: font, .foregroundColor: theme.foreground, .paragraphStyle: paragraph])
            let size = measured(attributed, width: width)
            if size.height <= available {
                let y = top + max(0, (available - size.height) * 0.38)
                let rect = CGRect(x: 100, y: y, width: width, height: ceil(size.height) + 2)
                return Layout(text: attributed, textRect: rect, author: author,
                              authorRect: CGRect(x: 152, y: rect.maxY + 48, width: width - 52, height: ceil(authorHeight) + 2), fontSize: fontSize, isExcerpt: text != original)
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
            let cg = context.cgContext
            if let gradient = CGGradient(colorsSpace: CGColorSpaceCreateDeviceRGB(), colors: [UIColor.white.withAlphaComponent(theme == .paper ? 0.28 : 0.035).cgColor, UIColor.clear.cgColor] as CFArray, locations: [0, 1]) {
                cg.drawLinearGradient(gradient, start: .zero, end: CGPoint(x: 1080, y: format.size.height), options: [])
            }
            // A quiet printed-paper border; content stays well inside social overlays.
            cg.setStrokeColor(theme.foreground.withAlphaComponent(0.12).cgColor)
            cg.setLineWidth(1)
            cg.stroke(CGRect(x: 48, y: 48, width: 984, height: format.size.height - 96))
            let headerY: CGFloat = format == .story ? 210 : 100
            let footerY = format.size.height - (format == .story ? 290 : 106)
            let brand = NSAttributedString(string: "PingLet", attributes: [.font: UIFont.systemFont(ofSize: 28, weight: .semibold), .foregroundColor: theme.foreground, .kern: 0.4])
            brand.draw(at: CGPoint(x: 140, y: headerY))
            theme.accent.setFill()
            let mark = UIBezierPath()
            mark.move(to: CGPoint(x: 112, y: headerY + 1))
            mark.addQuadCurve(to: CGPoint(x: 126, y: headerY + 16), controlPoint: CGPoint(x: 113, y: headerY + 15))
            mark.addQuadCurve(to: CGPoint(x: 112, y: headerY + 31), controlPoint: CGPoint(x: 113, y: headerY + 17))
            mark.addQuadCurve(to: CGPoint(x: 98, y: headerY + 16), controlPoint: CGPoint(x: 111, y: headerY + 17))
            mark.addQuadCurve(to: CGPoint(x: 112, y: headerY + 1), controlPoint: CGPoint(x: 111, y: headerY + 15))
            mark.close(); mark.fill()
            context.fill(CGRect(x: 100, y: layout.textRect.minY - 42, width: 44, height: 3))
            layout.text.draw(with: layout.textRect, options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil)
            if layout.author != nil {
                theme.accent.setFill()
                context.fill(CGRect(x: 100, y: layout.authorRect.minY + 17, width: 30, height: 2))
            }
            layout.author?.draw(with: layout.authorRect, options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil)
            let footer = NSAttributedString(string: layout.isExcerpt ? "EXCERPT · PINGLET.AI" : "PINGLET.AI", attributes: [.font: UIFont.systemFont(ofSize: 19, weight: .medium), .foregroundColor: theme.foreground.withAlphaComponent(0.75), .kern: 2])
            footer.draw(at: CGPoint(x: 100, y: footerY))
        }
    }
    private static func measured(_ text: NSAttributedString, width: CGFloat) -> CGRect {
        text.boundingRect(with: CGSize(width: width, height: .greatestFiniteMagnitude), options: [.usesLineFragmentOrigin, .usesFontLeading], context: nil)
    }
}
