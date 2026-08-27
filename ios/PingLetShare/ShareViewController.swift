import SwiftUI
import UIKit
import UniformTypeIdentifiers

final class ShareViewController: UIViewController {
    private let environment = AppEnvironment()
    private var hostingController: UIViewController?
    private let spinner = UIActivityIndicatorView(style: .medium)

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(red: 0.97, green: 0.95, blue: 0.90, alpha: 1)
        spinner.translatesAutoresizingMaskIntoConstraints = false
        spinner.startAnimating()
        view.addSubview(spinner)
        NSLayoutConstraint.activate([
            spinner.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            spinner.centerYAnchor.constraint(equalTo: view.centerYAnchor)
        ])
        Task { await loadShare() }
    }

    private func loadShare() async {
        let value = await extractSharedValue()
        showSharedAddScreen(initialText: value ?? "")
    }

    private func showSharedAddScreen(initialText: String) {
        spinner.stopAnimating()
        spinner.removeFromSuperview()
        let root = AddPingLetView(
            initialText: initialText,
            onSaved: { [weak self] in self?.finish() },
            onCancel: { [weak self] in self?.cancel() }
        )
        .environmentObject(environment)
        let host = UIHostingController(rootView: root)
        host.view.backgroundColor = .clear
        addChild(host)
        host.view.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(host.view)
        NSLayoutConstraint.activate([
            host.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            host.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            host.view.topAnchor.constraint(equalTo: view.topAnchor),
            host.view.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        host.didMove(toParent: self)
        hostingController = host
    }

    private func extractSharedValue() async -> String? {
        for item in extensionContext?.inputItems as? [NSExtensionItem] ?? [] {
            for provider in item.attachments ?? [] {
                if let value = await loadSharedValue(from: provider) { return value }
            }
        }
        return nil
    }

    private func loadSharedValue(from provider: NSItemProvider) async -> String? {
        if provider.hasItemConformingToTypeIdentifier(UTType.url.identifier),
           let item = try? await provider.loadItem(forTypeIdentifier: UTType.url.identifier) {
            if let url = item as? URL { return url.absoluteString }
            if let url = item as? NSURL { return url.absoluteString }
            if let text = item as? String { return normalized(text) }
        }
        if provider.hasItemConformingToTypeIdentifier(UTType.plainText.identifier),
           let item = try? await provider.loadItem(forTypeIdentifier: UTType.plainText.identifier) {
            if let text = item as? String { return normalized(text) }
            if let attributed = item as? NSAttributedString { return normalized(attributed.string) }
            if let data = item as? Data, let text = String(data: data, encoding: .utf8) { return normalized(text) }
        }
        return nil
    }

    private func normalized(_ value: String) -> String? {
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return nil }
        guard let url = firstWebURL(in: trimmed) else { return trimmed }
        let context = trimmed.replacingOccurrences(of: url.absoluteString, with: "").trimmingCharacters(in: .whitespacesAndNewlines)
        return context.isEmpty ? url.absoluteString : "\(context)\n\(url.absoluteString)"
    }

    private func firstWebURL(in value: String) -> URL? {
        guard let detector = try? NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue) else { return nil }
        let range = NSRange(value.startIndex..., in: value)
        return detector.matches(in: value, range: range).compactMap(\.url).first {
            ["http", "https"].contains($0.scheme?.lowercased() ?? "")
        }
    }

    private func finish() { extensionContext?.completeRequest(returningItems: nil) }
    private func cancel() { extensionContext?.cancelRequest(withError: NSError(domain: "PingLetShare", code: NSUserCancelledError)) }
}
