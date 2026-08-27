import UIKit
import UniformTypeIdentifiers

final class ShareViewController: UIViewController {
    private let sharedStore = SharedStore()
    private lazy var session = SessionManager(api: APIClient(), secure: SecureStore(), shared: sharedStore)
    private let titleLabel = UILabel(), detailLabel = UILabel(), previewLabel = UILabel(), errorLabel = UILabel()
    private let saveButton = UIButton(type: .system), cancelButton = UIButton(type: .system)
    private let spinner = UIActivityIndicatorView(style: .medium)
    private var sharedValue: String?

    override func viewDidLoad() { super.viewDidLoad(); configureView(); Task { await captureSharedValue() } }

    private func configureView() {
        view.backgroundColor = UIColor(red: 0.97, green: 0.95, blue: 0.90, alpha: 1)
        titleLabel.text = "Keep what found you."; titleLabel.font = .systemFont(ofSize: 32, weight: .medium); titleLabel.textColor = UIColor(red: 0.07, green: 0.08, blue: 0.07, alpha: 1)
        detailLabel.text = "PingLet will read the post's words, images, and speech in the background."; detailLabel.font = .systemFont(ofSize: 15); detailLabel.textColor = UIColor(red: 0.30, green: 0.29, blue: 0.26, alpha: 1); detailLabel.numberOfLines = 0
        previewLabel.font = .systemFont(ofSize: 16, weight: .medium); previewLabel.textColor = UIColor(red: 0.18, green: 0.19, blue: 0.17, alpha: 1); previewLabel.numberOfLines = 4; previewLabel.text = "Reading shared link…"; previewLabel.backgroundColor = UIColor.white.withAlphaComponent(0.72); previewLabel.layer.cornerRadius = 16; previewLabel.layer.masksToBounds = true
        errorLabel.font = .systemFont(ofSize: 13, weight: .medium); errorLabel.textColor = .systemRed; errorLabel.numberOfLines = 0; errorLabel.isHidden = true
        var configuration = UIButton.Configuration.filled(); configuration.title = "EXTRACT AND SAVE"; configuration.baseBackgroundColor = UIColor(red: 0.08, green: 0.10, blue: 0.07, alpha: 1); configuration.baseForegroundColor = .white; configuration.cornerStyle = .capsule; configuration.contentInsets = NSDirectionalEdgeInsets(top: 15, leading: 20, bottom: 15, trailing: 20)
        saveButton.configuration = configuration; saveButton.isEnabled = false; saveButton.addTarget(self, action: #selector(save), for: .touchUpInside)
        cancelButton.setTitle("Cancel", for: .normal); cancelButton.setTitleColor(UIColor(red: 0.35, green: 0.28, blue: 0.23, alpha: 1), for: .normal); cancelButton.addTarget(self, action: #selector(cancel), for: .touchUpInside); spinner.startAnimating()
        let header = UIStackView(arrangedSubviews: [titleLabel, detailLabel]); header.axis = .vertical; header.spacing = 10
        let content = UIStackView(arrangedSubviews: [header, previewLabel, errorLabel, spinner, saveButton, cancelButton]); content.axis = .vertical; content.spacing = 18; content.translatesAutoresizingMaskIntoConstraints = false; view.addSubview(content)
        NSLayoutConstraint.activate([content.leadingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.leadingAnchor, constant: 24), content.trailingAnchor.constraint(equalTo: view.safeAreaLayoutGuide.trailingAnchor, constant: -24), content.centerYAnchor.constraint(equalTo: view.safeAreaLayoutGuide.centerYAnchor), previewLabel.heightAnchor.constraint(greaterThanOrEqualToConstant: 92)])
    }

    private func captureSharedValue() async {
        for item in extensionContext?.inputItems as? [NSExtensionItem] ?? [] { for provider in item.attachments ?? [] { if let value = await loadSharedValue(from: provider) { sharedValue = value; spinner.stopAnimating(); previewLabel.text = value; saveButton.isEnabled = true; return } } }
        spinner.stopAnimating(); showError("PingLet could not find a public link. Copy the link from Instagram and paste it into PingLet.")
    }
    private func loadSharedValue(from provider: NSItemProvider) async -> String? {
        if provider.hasItemConformingToTypeIdentifier(UTType.url.identifier), let item = try? await provider.loadItem(forTypeIdentifier: UTType.url.identifier) { if let url = item as? URL { return url.absoluteString }; if let url = item as? NSURL { return url.absoluteString }; if let text = item as? String { return normalized(text) } }
        if provider.hasItemConformingToTypeIdentifier(UTType.plainText.identifier), let item = try? await provider.loadItem(forTypeIdentifier: UTType.plainText.identifier) { if let text = item as? String { return normalized(text) }; if let attributed = item as? NSAttributedString { return normalized(attributed.string) }; if let data = item as? Data, let text = String(data: data, encoding: .utf8) { return normalized(text) } }
        return nil
    }
    private func normalized(_ value: String) -> String? { let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines); guard !trimmed.isEmpty else { return nil }; guard let url = firstWebURL(in: trimmed) else { return trimmed }; let context = trimmed.replacingOccurrences(of: url.absoluteString, with: "").trimmingCharacters(in: .whitespacesAndNewlines); return context.isEmpty ? url.absoluteString : "\(context)\n\(url.absoluteString)" }
    private func firstWebURL(in value: String) -> URL? { guard let detector = try? NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue) else { return nil }; let range = NSRange(value.startIndex..., in: value); return detector.matches(in: value, range: range).compactMap(\.url).first { ["http", "https"].contains($0.scheme?.lowercased() ?? "") } }

    @objc private func save() { Task { await submit() } }
    private func submit() async {
        guard let value = sharedValue, let url = firstWebURL(in: value) else { showError("This share does not contain a valid public link."); return }
        setSubmitting(true); let context = value.replacingOccurrences(of: url.absoluteString, with: "").trimmingCharacters(in: .whitespacesAndNewlines)
        do { let _: Ingestion = try await session.perform("/api/v1/me/ingestions", method: .post, body: IngestionRequest(url: url.absoluteString, contextText: context.isEmpty ? nil : context)); extensionContext?.completeRequest(returningItems: nil) }
        catch let api as APIError where api.code == "TERMS_ACCEPTANCE_REQUIRED" { setSubmitting(false); showTermsPrompt() }
        catch let api as APIError { setSubmitting(false); showError(api.errorDescription ?? "This post could not be queued. Check your connection and try again.") }
        catch { setSubmitting(false); showError("This post could not be queued. Check your connection and try again.") }
    }
    private func showTermsPrompt() {
        let alert = UIAlertController(title: "Sharing content with PingLet", message: "By continuing, you agree to our Terms of Use. Only submit content you are permitted to share. Eligible AI-derived excerpts and attribution from public links may appear in Explore.", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel)); alert.addAction(UIAlertAction(title: "Agree and continue", style: .default) { [weak self] _ in Task { await self?.acceptTermsAndSubmit() } }); present(alert, animated: true)
    }
    private func acceptTermsAndSubmit() async { setSubmitting(true); do { let _: TermsStatus = try await session.perform("/api/v1/me/terms/accept", method: .post, body: EmptyBody()); setSubmitting(false); await submit() } catch { setSubmitting(false); showError("Your agreement could not be recorded. Check your connection and try again.") } }
    private func setSubmitting(_ active: Bool) { saveButton.isEnabled = !active; cancelButton.isEnabled = !active; if active { spinner.startAnimating(); saveButton.configuration?.title = "ADDING TO QUEUE…" } else { spinner.stopAnimating(); saveButton.configuration?.title = "EXTRACT AND SAVE" } }
    private func showError(_ message: String) { errorLabel.text = message; errorLabel.isHidden = false }
    @objc private func cancel() { extensionContext?.cancelRequest(withError: NSError(domain: "PingLetShare", code: NSUserCancelledError)) }
}
