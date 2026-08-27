import UIKit
import UniformTypeIdentifiers

final class ShareViewController: UIViewController {
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        Task { await captureSharedValue() }
    }
    private func captureSharedValue() async {
        for item in extensionContext?.inputItems as? [NSExtensionItem] ?? [] {
            for provider in item.attachments ?? [] {
                if provider.hasItemConformingToTypeIdentifier(UTType.url.identifier), let value = try? await provider.loadItem(forTypeIdentifier: UTType.url.identifier), let url = value as? URL { SharedStore().setPendingShare(url.absoluteString); finish(); return }
                if provider.hasItemConformingToTypeIdentifier(UTType.plainText.identifier), let value = try? await provider.loadItem(forTypeIdentifier: UTType.plainText.identifier), let text = value as? String { SharedStore().setPendingShare(text); finish(); return }
            }
        }
        finish()
    }
    private func finish() { extensionContext?.completeRequest(returningItems: nil) }
}
private extension SharedStore { func setPendingShare(_ value: String) { UserDefaults(suiteName: Self.appGroup)?.set(value, forKey: "pending_share") } }
