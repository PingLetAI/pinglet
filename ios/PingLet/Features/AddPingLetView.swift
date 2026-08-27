import SwiftUI

@MainActor final class AddPingLetModel: ObservableObject {
    @Published var text = ""; @Published var author = ""; @Published var saving = false; @Published var error: String?; @Published var showTerms = false; @Published var queued = false
    private var termsAccepted: Bool?
    init(initialText: String = "") { text = initialText }
    func prepare(_ env: AppEnvironment) async { let status: TermsStatus? = try? await env.session.perform("/api/v1/me/terms"); termsAccepted = status?.accepted }
    func save(_ env: AppEnvironment) async {
        guard !saving else { return }; let url = detectedURL
        if url != nil, termsAccepted != true { showTerms = true; return }
        await performSave(env, url: url)
    }
    func acceptAndSave(_ env: AppEnvironment) async {
        saving = true; error = nil
        do { let _: TermsStatus = try await env.session.perform("/api/v1/me/terms/accept", method: .post, body: EmptyBody()); termsAccepted = true; showTerms = false; saving = false; await performSave(env, url: detectedURL) }
        catch { saving = false; self.error = "Could not record your agreement. Check your connection and try again." }
    }
    private var detectedURL: URL? { text.split(whereSeparator: { $0.isWhitespace }).compactMap { URL(string: String($0)) }.first { ["http", "https"].contains($0.scheme?.lowercased() ?? "") } }
    private func performSave(_ env: AppEnvironment, url: URL?) async {
        saving = true; error = nil
        do {
            if let url {
                let context = text.replacingOccurrences(of: url.absoluteString, with: "").trimmingCharacters(in: .whitespacesAndNewlines)
                let _: Ingestion = try await env.session.perform("/api/v1/me/ingestions", method: .post, body: IngestionRequest(url: url.absoluteString, contextText: context.isEmpty ? nil : context))
            } else {
                struct Body: Encodable { let text: String; let type: String; let author: String? }
                let _: UserContent = try await env.session.perform("/api/v1/me/content", method: .post, body: Body(text: text.trimmingCharacters(in: .whitespacesAndNewlines), type: "QUOTE", author: author.isEmpty ? nil : author))
            }
            saving = false; queued = true
        } catch let api as APIError {
            saving = false
            if api.code == "TERMS_ACCEPTANCE_REQUIRED", url != nil { showTerms = true; return }
            error = api.errorDescription ?? "Could not save this post. Check the link and try again."
        } catch { saving = false; self.error = "Could not save this post. Check the link and try again." }
    }
}

struct AddPingLetView: View {
    @EnvironmentObject private var env: AppEnvironment; @Environment(\.dismiss) private var dismiss; @StateObject private var model = AddPingLetModel()
    private let onSaved: (() -> Void)?
    private let onCancel: (() -> Void)?
    init(initialText: String = "", onSaved: (() -> Void)? = nil, onCancel: (() -> Void)? = nil) {
        _model = StateObject(wrappedValue: AddPingLetModel(initialText: initialText))
        self.onSaved = onSaved
        self.onCancel = onCancel
    }
    var body: some View {
        NavigationStack {
            ZStack {
                PingLetCanvas()
                ScrollView {
                    VStack(alignment: .leading, spacing: 22) {
                        VStack(alignment: .leading, spacing: 9) {
                            Text("NEW PINGLET")
                                .font(.system(size: 12, weight: .bold, design: .rounded))
                                .tracking(1.8)
                                .foregroundStyle(Color.pingletClay)
                            Text("Keep what found you.")
                                .font(.system(size: 40, design: .serif))
                                .foregroundStyle(Color.pingletInk)
                            Text("Write your own words or paste a public post link. PingLet will quietly take it from there.")
                                .font(.system(size: 16, weight: .medium, design: .rounded))
                                .foregroundStyle(Color.pingletMutedInk)
                                .lineSpacing(3)
                        }
                        PingLetCard {
                            PingLetSectionLabel(title: "Your words or a discovered post")
                            TextEditor(text: $model.text)
                                .font(.system(size: 19, design: .serif))
                                .scrollContentBackground(.hidden)
                                .frame(minHeight: 180)
                                .overlay(alignment: .topLeading) {
                                    if model.text.isEmpty {
                                        Text("A thought, quote, or link from Instagram, TikTok, Facebook, YouTube…")
                                            .font(.system(size: 17, design: .serif))
                                            .foregroundStyle(Color.pingletMutedInk.opacity(0.58))
                                            .padding(.top, 8)
                                            .allowsHitTesting(false)
                                    }
                                }
                            Divider()
                            TextField("Author or creator (optional)", text: $model.author)
                                .font(.system(size: 15, weight: .medium, design: .rounded))
                        }
                        if let error = model.error {
                            Label(error, systemImage: "exclamationmark.circle.fill")
                                .font(.system(size: 14, weight: .medium, design: .rounded))
                                .foregroundStyle(Color.red)
                        }
                        Button(model.saving ? "SAVING…" : "EXTRACT AND SAVE") { Task { await model.save(env) } }
                            .buttonStyle(PingLetPrimaryButtonStyle())
                            .disabled(model.saving || model.text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                        Text("Public links may be analyzed for eligible excerpts and Explore topics. Personal notes remain private.")
                            .font(.system(size: 12, weight: .medium, design: .rounded))
                            .foregroundStyle(Color.pingletMutedInk.opacity(0.72))
                            .multilineTextAlignment(.center)
                            .frame(maxWidth: .infinity)
                    }
                    .padding(22)
                    .padding(.bottom, 28)
                }
            }
            .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Close") { if let onCancel { onCancel() } else { dismiss() } }.fontWeight(.semibold) } }
            .task { await model.prepare(env) }.onChange(of: model.queued) { _, ready in if ready { if let onSaved { onSaved() } else { dismiss() } } }
            .confirmationDialog("Sharing content with PingLet", isPresented: $model.showTerms, titleVisibility: .visible) {
                Button("AGREE AND CONTINUE") { Task { await model.acceptAndSave(env) } }; Button("Cancel", role: .cancel) {}
            } message: { Text("By continuing, you agree to our Terms of Use. Only submit content you are permitted to share. PingLet may analyze public links and use eligible AI-derived excerpts, topics, source attribution, and links in public Explore catalogs. Your personal notes, account information, and full saved details remain private.") }
        }
    }
}
