import UIKit
import UniformTypeIdentifiers

final class ShareViewController: UIViewController {
    private let titleLabel = UILabel()
    private let detailLabel = UILabel()
    private let previewLabel = UILabel()
    private let saveButton = UIButton(type: .system)
    private let cancelButton = UIButton(type: .system)
    private let spinner = UIActivityIndicatorView(style: .medium)
    private var sharedValue: String?

    override func viewDidLoad() {
        super.viewDidLoad()
        configureView()
        Task { await captureSharedValue() }
    }

    private func configureView() {
        view.backgroundColor = UIColor(red: 0.97, green: 0.95, blue: 0.90, alpha: 1)
        titleLabel.text = "Send it to PingLet"
        titleLabel.font = .systemFont(ofSize: 28, weight: .medium)
        titleLabel.textColor = UIColor(red: 0.07, green: 0.08, blue: 0.07, alpha: 1)
        detailLabel.text = "Keep the useful part close. You can review the link in PingLet before it is processed."
        detailLabel.font = .systemFont(ofSize: 15, weight: .regular)
        detailLabel.textColor = UIColor(red: 0.30, green: 0.29, blue: 0.26, alpha: 1)
        detailLabel.numberOfLines = 0
        previewLabel.font = .systemFont(ofSize: 15, weight: .medium)
        previewLabel.textColor = UIColor(red: 0.18, green: 0.19, blue: 0.17, alpha: 1)
        previewLabel.numberOfLines = 3
        previewLabel.text = "Reading shared link…"
        previewLabel.backgroundColor = UIColor.white.withAlphaComponent(0.72)
        previewLabel.layer.cornerRadius = 16
        previewLabel.layer.masksToBounds = true
        previewLabel.setContentCompressionResistancePriority(.required, for: .vertical)

        var configuration = UIButton.Configuration.filled()
        configuration.title = "SAVE TO PINGLET"
        configuration.baseBackgroundColor = UIColor(red: 0.08, green: 0.10, blue: 0.07, alpha: 1)
        configuration.baseForegroundColor = .white
        configuration.cornerStyle = .capsule
        configuration.contentInsets = NSDirectionalEdgeInsets(top: 15, leading: 20, bottom: 15, trailing: 20)
        saveButton.configuration = configuration
        saveButton.isEnabled = false
        saveButton.addTarget(self, action: #selector(save), for: .touchUpInside)
        cancelButton.setTitle("Cancel", for: .normal)
        cancelButton.setTitleColor(UIColor(red: 0.35, green: 0.28, blue: 0.23, alpha: 1), for: .normal)
        cancelButton.addTarget(self, action: #selector(cancel), for: .touchUpInside)
        spinner.startAnimating()

        let header = UIStackView(arrangedSubviews: [titleLabel, detailLabel])
        header.axis = .vertical
        header.spacing = 10
        let content = UIStackView(arrangedSubviews: [header, previewLabel, spinner, saveButton, cancelButton])
        content.axis = .vertical
        content.spacing = 18
        content.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(content)
        NSLayoutConstraint.activate([
            content.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: 24),
            content.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: -24),
            content.centerYAnchor.constraint(equalTo: view.safeAreaLayoutGuide.centerYAnchor),
            previewLabel.heightAnchor.constraint(greaterThanOrEqualToConstant: 86)
        ])
    }

    private func captureSharedValue() async {
        for item in extensionContext?.inputItems as? [NSExtensionItem] ?? [] {
            for provider in item.attachments ?? [] {
                if let value = await sharedValue(from: provider) {
                    await MainActor.run { show(value) }
                    return
                }
            }
        }
        await MainActor.run {
            spinner.stopAnimating()
            previewLabel.text = "PingLet could not find a public link in this share. Try copying the link from Instagram and paste it into PingLet."
            saveButton.isHidden = true
        }
    }

    private func sharedValue(from provider: NSItemProvider) async -> String? {
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
        let detector = try? NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue)
        let range = NSRange(trimmed.startIndex..., in: trimmed)
        guard let match = detector?.firstMatch(in: trimmed, range: range), let url = match.url else { return trimmed }
        let context = trimmed.replacingOccurrences(of: url.absoluteString, with: "").trimmingCharacters(in: .whitespacesAndNewlines)
        return context.isEmpty ? url.absoluteString : "\(context)\n\(url.absoluteString)"
    }

    @MainActor private func show(_ value: String) {
        sharedValue = value
        spinner.stopAnimating()
        previewLabel.text = value
        previewLabel.layoutMargins = UIEdgeInsets(top: 16, left: 16, bottom: 16, right: 16)
        saveButton.isEnabled = true
    }

    @objc private func save() {
        guard let sharedValue else { return }
        SharedStore().savePendingShare(sharedValue)
        finish()
    }

    @objc private func cancel() { extensionContext?.cancelRequest(withError: NSError(domain: "PingLetShare", code: NSUserCancelledError)) }
    private func finish() { extensionContext?.completeRequest(returningItems: nil) }
}
