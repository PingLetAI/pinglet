import SwiftUI

@MainActor final class AddPingLetModel: ObservableObject {
    @Published var text = ""; @Published var author = ""; @Published var saving = false; @Published var error: String?; @Published var showTerms = false; @Published var queued = false
    private var termsAccepted: Bool?
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
    var body: some View {
        NavigationStack {
            Form { Section("YOUR WORDS OR A DISCOVERED POST") { TextEditor(text: $model.text).frame(minHeight: 150); TextField("Author (optional)", text: $model.author) }
                if let error = model.error { Text(error).foregroundStyle(.red) }
                Button(model.saving ? "SAVING..." : "EXTRACT AND SAVE") { Task { await model.save(env) } }.disabled(model.saving || model.text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
            }
            .navigationTitle("Add a PingLet").toolbar { ToolbarItem(placement: .cancellationAction) { Button("Close") { dismiss() } } }
            .task { await model.prepare(env) }.onChange(of: model.queued) { _, ready in if ready { dismiss() } }
            .confirmationDialog("Sharing content with PingLet", isPresented: $model.showTerms, titleVisibility: .visible) {
                Button("AGREE AND CONTINUE") { Task { await model.acceptAndSave(env) } }; Button("Cancel", role: .cancel) {}
            } message: { Text("By continuing, you agree to our Terms of Use. Only submit content you are permitted to share. PingLet may analyze public links and use eligible AI-derived excerpts, topics, source attribution, and links in public Explore catalogs. Your personal notes, account information, and full saved details remain private.") }
        }
    }
}
