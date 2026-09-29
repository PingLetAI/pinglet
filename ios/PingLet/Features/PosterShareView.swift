import SwiftUI
import Photos

struct PosterShareButton: View {
    var dark = false
    let action: () -> Void
    var body: some View {
        Button(action: action) {
            Label("Share", systemImage: "square.and.arrow.up")
                .font(.system(size: 13, weight: .semibold))
                .padding(.horizontal, 12).frame(height: 34)
                .foregroundStyle(dark ? Color.pingletPaper : Color.pingletInk)
                .background(dark ? Color.white.opacity(0.09) : Color.pingletMint.opacity(0.4), in: Capsule())
                .overlay(Capsule().stroke(dark ? Color.white.opacity(0.15) : Color.pingletInk.opacity(0.09), lineWidth: 1))
                .frame(minHeight: 44).contentShape(Rectangle())
        }.buttonStyle(.plain).accessibilityLabel("Share PingLet as image")
    }
}

struct PosterShareView: View {
    let content: PosterContent
    @Environment(\.dismiss) private var dismiss
    @State private var format: PosterFormat = .defaultFormat
    @State private var theme: PosterTheme = .paper
    @State private var excerpt: String
    @State private var preview: UIImage?
    @State private var fitError: String?
    @State private var notice: String?
    @State private var saving = false
    @State private var activity: PosterActivity?
    init(content: PosterContent) {
        self.content = content
        _excerpt = State(initialValue: content.text)
    }
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    if let preview {
                        Image(uiImage: preview).resizable().scaledToFit()
                            .frame(maxHeight: 430).frame(maxWidth: .infinity)
                            .accessibilityLabel("Poster preview: \(excerpt)")
                    } else {
                        ContentUnavailableView("Choose a shorter excerpt", systemImage: "text.quote", description: Text(fitError ?? "Preparing your poster…"))
                    }
                    Picker("Format", selection: $format) {
                        ForEach(PosterFormat.allCases) { Text($0.label).tag($0) }
                    }.pickerStyle(.segmented)
                    Picker("Theme", selection: $theme) {
                        ForEach(PosterTheme.allCases) { Text($0.rawValue).tag($0) }
                    }.pickerStyle(.segmented)
                    VStack(alignment: .leading, spacing: 3) {
                        Text(format.guidance)
                        Text(format.dimensions)
                    }.font(.caption).foregroundStyle(Color.pingletMutedInk)
                    DisclosureGroup("Choose an excerpt", isExpanded: Binding(get: { editingExcerpt }, set: { editingExcerpt = $0 })) {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Remove words from the beginning or end. Creator attribution stays on the image.").font(.caption).foregroundStyle(Color.pingletMutedInk)
                            TextEditor(text: $excerpt).frame(minHeight: 150).font(.body)
                                .accessibilityLabel("Poster excerpt")
                            Button("Use full PingLet") { excerpt = content.text }
                        }.padding(.top, 8)
                    }
                    if let fitError { Label(fitError, systemImage: "exclamationmark.circle").font(.subheadline).foregroundStyle(Color.pingletClay) }
                    if let notice { Text(notice).font(.subheadline).foregroundStyle(Color.pingletMutedInk).accessibilityAddTraits(.updatesFrequently) }

                }.padding(22)
            }
            .scrollDismissesKeyboard(.interactively)
            .safeAreaInset(edge: .bottom) { actions.padding(.horizontal, 22).padding(.vertical, 12).background(Color.pingletPaper) }
            .background(PingLetCanvas()).navigationTitle("Share a PingLet").navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Done") { dismiss() } } }
            .onAppear { render() }
            .onChange(of: format) { _, _ in render() }
            .onChange(of: theme) { _, _ in render() }
            .onChange(of: excerpt) { _, _ in render() }
            .sheet(item: $activity) { item in PosterActivitySheet(image: item.image) }
        }
    }
    private var actions: some View {
        VStack(spacing: 10) {
                    Button(action: share) { Label("Share image", systemImage: "square.and.arrow.up") }
                        .buttonStyle(PingLetPrimaryButtonStyle()).disabled(preview == nil || saving)
                    HStack(spacing: 12) {
                        Button { Task { await saveImage() } } label: { Label(saving ? "Saving…" : "Save image", systemImage: "arrow.down.to.line") }
                            .buttonStyle(PingLetSecondaryButtonStyle()).disabled(preview == nil || saving)
                        Button {
                            UIPasteboard.general.string = [excerpt, content.author, content.sourceURL].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: "\n\n")
                            notice = "Text copied."
                        } label: { Label("Copy text", systemImage: "doc.on.doc") }
                            .buttonStyle(PingLetSecondaryButtonStyle()).disabled(excerpt.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || !content.text.contains(excerpt.trimmingCharacters(in: .whitespacesAndNewlines)))
                    }
        }
    }
    @State private var editingExcerpt = false
    private func render() {
        notice = nil
        do {
            preview = try PosterRenderer.image(content: content, excerpt: excerpt, format: format, theme: theme)
            fitError = nil
        } catch {
            preview = nil
            fitError = error.localizedDescription
            editingExcerpt = true
        }
    }
    private func share() {
        guard let preview else { return }
        activity = PosterActivity(image: preview)
    }
    @MainActor private func saveImage() async {
        guard let image = preview, !saving else { return }
        saving = true
        defer { saving = false }
        let permission = await PHPhotoLibrary.requestAuthorization(for: .addOnly)
        guard permission == .authorized || permission == .limited else {
            notice = "Photo access is unavailable. You can use Share image to send it or save it to Files."
            return
        }
        do {
            try await PHPhotoLibrary.shared().performChanges { PHAssetChangeRequest.creationRequestForAsset(from: image) }
            notice = "Saved to Photos."
        } catch { notice = "The image could not be saved. Please try again." }
    }
}

private struct PosterActivity: Identifiable {
    let id = UUID()
    let image: UIImage
}
private struct PosterActivitySheet: UIViewControllerRepresentable {
    let image: UIImage
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: [image], applicationActivities: nil)
    }
    func updateUIViewController(_ controller: UIActivityViewController, context: Context) {}
}
