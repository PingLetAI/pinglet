import StoreKit
import SwiftUI

@MainActor final class AccountConnectionModel: ObservableObject {
    @Published var email = ""; @Published var code = ""; @Published var sent = false; @Published var loading = false; @Published var error: String?
    func request(_ env: AppEnvironment) async { loading = true; error = nil; do { let _: EmailOTPResponse = try await env.session.perform("/api/v1/auth/email/request", method: .post, body: EmailOTPRequest(email: email.trimmingCharacters(in: .whitespacesAndNewlines))); sent = true } catch { self.error = error.localizedDescription }; loading = false }
    func verify(_ env: AppEnvironment) async -> Bool { loading = true; error = nil; do { let response: EmailOTPVerifyResponse = try await env.session.perform("/api/v1/auth/email/verify", method: .post, body: EmailOTPVerifyRequest(email: email.trimmingCharacters(in: .whitespacesAndNewlines), code: code.trimmingCharacters(in: .whitespacesAndNewlines))); _ = try await env.session.installVerifiedSession(response); await env.bootstrap(); loading = false; return response.verified } catch { self.error = error.localizedDescription; loading = false; return false } }
}
struct AccountConnectionView: View {
    @EnvironmentObject private var env: AppEnvironment; @Environment(\.dismiss) private var dismiss; @StateObject private var model = AccountConnectionModel()
    var body: some View { ZStack { PingLetCanvas(); ScrollView { VStack(alignment: .leading, spacing: 22) { Image(systemName: "person.crop.circle.badge.checkmark").font(.system(size: 34)).foregroundStyle(Color.pingletClay); Text("Connect your email").font(.system(size: 40, design: .serif)); Text("Use the same email on another phone to return to everything you kept.").font(.system(size: 17, weight: .medium, design: .rounded)).foregroundStyle(Color.pingletMutedInk); PingLetCard { TextField("Email address", text: $model.email).textInputAutocapitalization(.never).keyboardType(.emailAddress).textContentType(.emailAddress); if model.sent { Divider(); TextField("Six-digit code", text: $model.code).keyboardType(.numberPad).textContentType(.oneTimeCode) } }; if let error = model.error { Label(error, systemImage: "exclamationmark.circle.fill").foregroundStyle(.red) }; Button(model.loading ? "PLEASE WAIT…" : model.sent ? "VERIFY EMAIL" : "SEND CODE") { Task { if model.sent { if await model.verify(env) { dismiss() } } else { await model.request(env) } } }.buttonStyle(PingLetPrimaryButtonStyle()).disabled(model.loading || model.email.isEmpty || (model.sent && model.code.count != 6)); Label("No password to remember", systemImage: "lock.shield").font(.system(size: 13, weight: .semibold, design: .rounded)).foregroundStyle(Color.pingletMutedInk).frame(maxWidth: .infinity) }.padding(24) } }.navigationTitle("Account").navigationBarTitleDisplayMode(.inline) }
}

struct TrialOfferView: View {
    @EnvironmentObject private var env: AppEnvironment; @Environment(\.dismiss) private var dismiss; @State private var loading = false; @State private var error: String?
    var body: some View { ZStack { PingLetCanvas(); ScrollView { VStack(alignment: .leading, spacing: 20) { PingLetSectionLabel(title: "PingLet Plus", trailing: "7 days"); Text("Try the whole experience.").font(.system(size: 42, design: .serif)); Text("Enjoy everything in Plus free for 7 days.").font(.system(size: 18, weight: .medium, design: .rounded)).foregroundStyle(Color.pingletMutedInk); PingLetCard(dark: true) { ForEach(["Full summaries, transcripts, insights, and takeaways", "Premium widget themes, profiles, and scheduled modes", "More AI imports and unlimited personal saves", "Personalized rotation and manual show-another controls"], id: \.self) { Label($0, systemImage: "checkmark.circle.fill").font(.system(size: 15, weight: .medium, design: .rounded)).foregroundStyle(Color.pingletPaper) } }; PingLetCard { Label("No payment required", systemImage: "shield.fill").font(.headline); Text("No card. No automatic subscription. You return to Free when the trial ends.").foregroundStyle(Color.pingletMutedInk) }; if let error { Text(error).foregroundStyle(.red) }; Button(loading ? "ACTIVATING…" : "TRY PLUS FREE") { Task { await start() } }.buttonStyle(PingLetPrimaryButtonStyle()).disabled(loading || env.entitlement?.trialEligible != true); Button("CONTINUE WITH FREE") { dismiss() }.font(.system(size: 13, weight: .bold, design: .rounded)).frame(maxWidth: .infinity) }.padding(24) } }.navigationTitle("PingLet Plus").navigationBarTitleDisplayMode(.inline) }
    private func start() async { loading = true; do { let entitlement: Entitlement = try await env.session.perform("/api/v1/me/entitlements/trial", method: .post, body: EmptyBody()); env.entitlement = entitlement; env.shared.entitlement = entitlement; await env.track("TRIAL_STARTED", metadata: "SETTINGS"); dismiss() } catch { self.error = "Your free trial could not be started. Refresh and try again." }; loading = false }
}

struct PlusPlansView: View {
    @EnvironmentObject private var env: AppEnvironment
    @EnvironmentObject private var subscriptions: AppleSubscriptionManager
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ZStack {
            PingLetCanvas()
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    PingLetSectionLabel(title: "PingLet Plus", trailing: "More room to remember")
                    Text("Keep everything worth remembering.").font(.system(size: 42, design: .serif))
                    Text("Complete understanding for every post, and a Home Screen that feels distinctly yours.")
                        .font(.system(size: 17, weight: .medium, design: .rounded))
                        .foregroundStyle(Color.pingletMutedInk)
                    PingLetCard(dark: true) {
                        ForEach(["Complete AI breakdowns", "Every useful insight and takeaway", "50 social imports every month", "Unlimited personal saves", "Independent premium widget profiles"], id: \.self) {
                            Label($0, systemImage: "checkmark.seal.fill")
                                .font(.system(size: 15, weight: .semibold, design: .rounded))
                                .foregroundStyle(Color.pingletPaper)
                        }
                    }
                    if env.entitlement?.paidPlansEnabled == true {
                        plans
                        if subscriptions.loading { ProgressView().frame(maxWidth: .infinity) }
                        if let message = subscriptions.message { Label(message, systemImage: "checkmark.circle.fill").foregroundStyle(Color.pingletForest) }
                        if let error = subscriptions.error { Label(error, systemImage: "exclamationmark.circle.fill").foregroundStyle(.red) }
                        Button(subscriptions.purchasing ? "CONFIRMING…" : continueTitle) {
                            Task { await subscriptions.purchase(env) }
                        }
                        .buttonStyle(PingLetPrimaryButtonStyle())
                        .disabled(subscriptions.selectedProduct == nil || subscriptions.loading || subscriptions.purchasing)
                        Button("RESTORE PURCHASES") { Task { await subscriptions.restore(env) } }
                            .font(.system(size: 13, weight: .bold, design: .rounded))
                            .frame(maxWidth: .infinity)
                            .disabled(subscriptions.purchasing)
                        Text("Payment will be charged to your Apple Account after confirmation. Your subscription renews automatically unless canceled at least 24 hours before the end of the current period. Manage or cancel subscriptions in your Apple Account settings.")
                            .font(.system(size: 11, weight: .medium, design: .rounded))
                            .foregroundStyle(Color.pingletMutedInk)
                            .lineSpacing(2)
                            .multilineTextAlignment(.center)
                    } else {
                        PingLetCard { Text("Subscriptions are coming soon").font(.headline); Text("The seven-day PingLet Plus trial remains free, requires no payment method, and never starts an automatic subscription.").foregroundStyle(Color.pingletMutedInk) }
                    }
                    if env.entitlement?.trialEligible == true {
                        NavigationLink { TrialOfferView() } label: {
                            Label("TRY PLUS FREE FOR 7 DAYS — NO CARD", systemImage: "sparkles")
                                .font(.system(size: 13, weight: .bold, design: .rounded))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 15)
                                .background(Color.pingletMint.opacity(0.7), in: RoundedRectangle(cornerRadius: 18))
                        }
                        .buttonStyle(.plain)
                    }
                    HStack { Spacer(); Link("Terms", destination: URL(string: "https://pinglet.ai/terms")!); Text("·"); Link("Privacy", destination: URL(string: "https://pinglet.ai/privacy")!); Spacer() }.font(.caption.bold())
                }
                .padding(24)
                .padding(.bottom, 30)
            }
        }
        .navigationTitle("PingLet Plus")
        .navigationBarTitleDisplayMode(.inline)
        .task { await subscriptions.prepare(env) }
        .onChange(of: subscriptions.purchaseCompleted) { _, completed in if completed { dismiss() } }
    }

    private var plans: some View {
        VStack(spacing: 12) {
            if let annual = subscriptions.annualProduct { planChoice(annual, title: "Annual", badge: subscriptions.annualSavingsPercent.map { "SAVE \($0)%" } ?? "BEST VALUE") }
            if let monthly = subscriptions.monthlyProduct { planChoice(monthly, title: "Monthly", badge: nil) }
        }
    }

    private func planChoice(_ product: Product, title: String, badge: String?) -> some View {
        let selected = subscriptions.selectedProductID == product.id
        return Button { subscriptions.selectedProductID = product.id } label: {
            HStack(spacing: 13) {
                Image(systemName: selected ? "checkmark.circle.fill" : "circle").font(.title3).foregroundStyle(selected ? Color.pingletForest : Color.pingletMutedInk)
                VStack(alignment: .leading, spacing: 3) {
                    Text(title.uppercased()).font(.caption.bold()).tracking(1.2).foregroundStyle(Color.pingletClay)
                    Text("\(product.displayPrice) per \(subscriptions.duration(for: product))").font(.system(size: 23, design: .serif)).foregroundStyle(Color.pingletInk)
                }
                Spacer()
                if let badge { Text(badge).font(.caption.bold()).foregroundStyle(Color.pingletInk).padding(.horizontal, 9).padding(.vertical, 6).background(Color.pingletMint, in: Capsule()) }
            }
            .padding(17)
            .background(selected ? Color.pingletMint.opacity(0.32) : Color.white.opacity(0.68), in: RoundedRectangle(cornerRadius: 22))
            .overlay(RoundedRectangle(cornerRadius: 22).stroke(selected ? Color.pingletForest : Color.pingletMutedInk.opacity(0.22), lineWidth: selected ? 1.5 : 1))
        }
        .buttonStyle(.plain)
    }

    private var continueTitle: String {
        guard let product = subscriptions.selectedProduct else { return "CONTINUE" }
        let plan = product.id == AppleSubscriptionManager.annualProductID ? "ANNUAL" : "MONTHLY"
        return "CONTINUE WITH \(plan) — \(product.displayPrice)"
    }
}
