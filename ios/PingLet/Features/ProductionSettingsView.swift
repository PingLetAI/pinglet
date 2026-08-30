import SwiftUI

struct SettingsView: View {
    @EnvironmentObject private var env: AppEnvironment
    @State private var showSignOut = false
    @State private var showDelete = false
    @State private var deletionCodeSent = false
    @State private var deletionCode = ""
    @State private var working = false
    @State private var workingMessage = ""
    @State private var error: String?

    private var entitlement: Entitlement? { env.entitlement }
    private var isConnected: Bool { entitlement?.isAnonymous == false }
    private var isPlus: Bool { entitlement?.plan == "PLUS" }

    var body: some View {
        NavigationStack {
            PingLetPage(
                eyebrow: "Settings",
                title: "Your PingLet, your rhythm.",
                subtitle: "Account, rotation, and the details that shape your experience."
            ) {
                accountSection
                experienceSection
                if isConnected { accountManagementSection }
                aboutSection
                Text("Widget timing may shift slightly while iOS is conserving battery.")
                    .font(.system(size: 12, weight: .medium, design: .rounded))
                    .foregroundStyle(Color.pingletMutedInk.opacity(0.68))
                    .frame(maxWidth: .infinity, alignment: .center)
                    .multilineTextAlignment(.center)
                    .padding(.top, 4)
            }
            .task { await env.refreshEntitlement() }
            .alert("Sign out on this device?", isPresented: $showSignOut) {
                Button("Cancel", role: .cancel) {}
                Button("Sign out", role: .destructive) { Task { await signOut() } }
            } message: {
                Text("Your PingLets remain in your account. This device returns to a new guest profile and clears its account-specific cache.")
            }
            .alert("Delete account and data?", isPresented: $showDelete) {
                if deletionCodeSent {
                    TextField("Six-digit verification code", text: $deletionCode)
                        .keyboardType(.numberPad)
                }
                Button("Cancel", role: .cancel) { resetDeletion() }
                Button(working ? "Please wait..." : deletionCodeSent ? "Delete permanently" : "Send verification code", role: .destructive) {
                    Task { await deletionAction() }
                }
                .disabled(working || (deletionCodeSent && deletionCode.count != 6))
            } message: {
                Text(deletionCodeSent
                     ? "Enter the code sent to \(entitlement?.email ?? "your email"). This cannot be undone."
                     : "This permanently deletes your account, personal saves, imports, favorites, devices, and account history.")
            }
            .overlay {
                if working {
                    ZStack {
                        Color.pingletInk.opacity(0.24).ignoresSafeArea()
                        VStack(spacing: 14) {
                            ProgressView()
                                .controlSize(.large)
                                .tint(Color.pingletGold)
                            Text(workingMessage)
                                .font(.system(size: 16, weight: .semibold, design: .rounded))
                                .foregroundStyle(Color.pingletPaper)
                        }
                        .padding(.horizontal, 34)
                        .padding(.vertical, 28)
                        .background(Color.pingletInk, in: RoundedRectangle(cornerRadius: 24, style: .continuous))
                        .shadow(color: Color.black.opacity(0.18), radius: 30, y: 16)
                    }
                    .transition(.opacity)
                }
            }
        }
    }

    private var accountSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            PingLetSectionLabel(title: "Account")
            PingLetCard(dark: isPlus) {
                HStack(alignment: .center, spacing: 14) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 16, style: .continuous)
                            .fill(isPlus ? Color.pingletGold : Color.pingletMint)
                            .frame(width: 52, height: 52)
                        Image(systemName: isConnected ? "person.crop.circle.badge.checkmark" : "person.crop.circle")
                            .font(.system(size: 23, weight: .semibold))
                            .foregroundStyle(Color.pingletInk)
                    }
                    VStack(alignment: .leading, spacing: 3) {
                        Text(accountTitle)
                            .font(.system(size: 22, weight: .regular, design: .serif))
                        Text(entitlement?.email ?? "Not connected to an email")
                            .font(.system(size: 13, weight: .medium, design: .rounded))
                            .foregroundStyle(isPlus ? Color.pingletPaper.opacity(0.68) : Color.pingletMutedInk)
                            .lineLimit(1)
                    }
                    Spacer()
                    Text(accountBadge)
                        .font(.system(size: 10, weight: .bold, design: .rounded))
                        .tracking(1)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 7)
                        .background((isPlus ? Color.pingletGold : Color.pingletMint).opacity(isPlus ? 1 : 0.8), in: Capsule())
                        .foregroundStyle(Color.pingletInk)
                }

                if let entitlement {
                    Divider().overlay(isPlus ? Color.white.opacity(0.14) : Color.pingletLine)
                    HStack(spacing: 22) {
                        usageMetric(
                            label: "Saves",
                            used: entitlement.saveCount,
                            limit: entitlement.saveLimit,
                            tint: .pingletMint
                        )
                        usageMetric(
                            label: "AI imports",
                            used: entitlement.socialImportsUsed,
                            limit: entitlement.socialImportLimit,
                            tint: .pingletGold
                        )
                    }
                } else {
                    ProgressView().tint(isPlus ? .white : .pingletInk)
                }

                if entitlement?.trialStatus == "ACTIVE" {
                    Divider().overlay(Color.white.opacity(0.14))
                    Label(
                        "\(entitlement?.trialDaysRemaining ?? 0) days remaining · no automatic charge",
                        systemImage: "clock.badge.checkmark"
                    )
                    .font(.system(size: 13, weight: .semibold, design: .rounded))
                    .foregroundStyle(Color.pingletPaper.opacity(0.78))
                }
            }

            accountCTA
            if let error {
                Label(error, systemImage: "exclamationmark.circle.fill")
                    .font(.system(size: 13, weight: .medium, design: .rounded))
                    .foregroundStyle(.red)
            }
        }
    }

    @ViewBuilder private var accountCTA: some View {
        if !isConnected {
            NavigationLink { AccountConnectionView() } label: { Text("CONNECT EMAIL") }
                .buttonStyle(PingLetPrimaryButtonStyle())
        } else if entitlement?.trialStatus != "ACTIVE" && !isPlus && entitlement?.trialEligible == true {
            NavigationLink { TrialOfferView() } label: { Text("TRY PINGLET PLUS · 7 DAYS FREE") }
                .buttonStyle(PingLetPrimaryButtonStyle())
        } else if !isPlus && entitlement?.paidPlansEnabled == true {
            NavigationLink { PlusPlansView() } label: { Text("EXPLORE PINGLET PLUS") }
                .buttonStyle(PingLetPrimaryButtonStyle())
        } else if entitlement?.trialStatus == "ACTIVE" && entitlement?.paidPlansEnabled == true {
            NavigationLink { PlusPlansView() } label: { Text("KEEP PINGLET PLUS") }
                .buttonStyle(PingLetPrimaryButtonStyle())
        }
    }

    private var experienceSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            PingLetSectionLabel(title: "Experience", trailing: "Every 30 minutes")
            PingLetCard {
                Label("Content balance", systemImage: "circle.lefthalf.filled")
                    .font(.system(size: 17, weight: .semibold, design: .rounded))
                Picker(
                    "Content balance",
                    selection: Binding(
                        get: { env.shared.contentMix },
                        set: { value in
                            env.shared.contentMix = value
                            Task { await patchMix(value) }
                        }
                    )
                ) {
                    Text("Mine").tag("MOSTLY_MINE")
                    Text("Balanced").tag("BALANCED")
                    Text("Discover").tag("MORE_DISCOVERY")
                }
                .pickerStyle(.segmented)
                Text("Personal saves are always prioritized.")
                    .font(.system(size: 12, weight: .medium, design: .rounded))
                    .foregroundStyle(Color.pingletMutedInk)

                Divider()
                settingsLink(
                    title: "Widget appearance",
                    detail: "Themes, schedules, typography, and controls",
                    icon: "rectangle.3.group.fill"
                ) { WidgetSettingsView() }
                Divider()
                settingsLink(
                    title: "Processing queue",
                    detail: "Shared-post progress and history",
                    icon: "cloud.fill"
                ) { ProcessingQueueView() }
            }
        }
    }

    private var accountManagementSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            PingLetSectionLabel(title: "Account management")
            PingLetCard {
                Button { showSignOut = true } label: {
                    settingsRow(title: "Sign out", detail: "Return this device to a guest profile", icon: "rectangle.portrait.and.arrow.right")
                }
                .buttonStyle(.plain)
                Divider()
                Button(role: .destructive) { showDelete = true } label: {
                    settingsRow(title: "Delete account and data", detail: "Permanently remove your PingLet account", icon: "trash.fill", destructive: true)
                }
                .buttonStyle(.plain)
            }
        }
    }

    private var aboutSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            PingLetSectionLabel(title: "About")
            PingLetCard {
                settingsRow(
                    title: "PingLet for iOS",
                    detail: "Version \(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0")",
                    icon: "info.circle.fill",
                    showsChevron: false
                )
                Divider()
                Link(destination: URL(string: "https://pinglet.ai/privacy")!) {
                    settingsRow(title: "Privacy policy", detail: "How PingLet handles your data", icon: "hand.raised.fill")
                }
                .buttonStyle(.plain)
                Divider()
                Link(destination: URL(string: "https://pinglet.ai/terms")!) {
                    settingsRow(title: "Terms of service", detail: "The terms for using PingLet", icon: "doc.text.fill")
                }
                .buttonStyle(.plain)
            }
        }
    }

    private var accountTitle: String {
        if entitlement?.trialStatus == "ACTIVE" { return "PingLet Plus trial" }
        if isPlus { return "PingLet Plus" }
        return isConnected ? "Free account" : "Guest profile"
    }

    private var accountBadge: String {
        if entitlement?.trialStatus == "ACTIVE" { return "TRIAL" }
        if isPlus { return "PLUS" }
        return isConnected ? "VERIFIED" : "LOCAL"
    }

    private func usageMetric(label: String, used: Int, limit: Int?, tint: Color) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label.uppercased())
                .font(.system(size: 10, weight: .bold, design: .rounded))
                .tracking(1)
                .foregroundStyle(isPlus ? Color.pingletPaper.opacity(0.62) : Color.pingletMutedInk)
            Text(limit.map { "\(used) of \($0)" } ?? "Unlimited")
                .font(.system(size: 17, weight: .semibold, design: .rounded))
            if let limit {
                GeometryReader { proxy in
                    ZStack(alignment: .leading) {
                        Capsule().fill(isPlus ? Color.white.opacity(0.14) : Color.pingletLine)
                        Capsule().fill(tint).frame(width: proxy.size.width * min(CGFloat(used) / CGFloat(max(limit, 1)), 1))
                    }
                }
                .frame(height: 5)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func settingsLink<Destination: View>(
        title: String,
        detail: String,
        icon: String,
        @ViewBuilder destination: () -> Destination
    ) -> some View {
        NavigationLink(destination: destination()) {
            settingsRow(title: title, detail: detail, icon: icon)
        }
        .buttonStyle(.plain)
    }

    private func settingsRow(
        title: String,
        detail: String,
        icon: String,
        destructive: Bool = false,
        showsChevron: Bool = true
    ) -> some View {
        HStack(spacing: 13) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .semibold))
                .frame(width: 38, height: 38)
                .background((destructive ? Color.red : Color.pingletMint).opacity(0.16), in: RoundedRectangle(cornerRadius: 12, style: .continuous))
                .foregroundStyle(destructive ? Color.red : Color.pingletClay)
            VStack(alignment: .leading, spacing: 3) {
                Text(title).font(.system(size: 16, weight: .semibold, design: .rounded))
                Text(detail).font(.system(size: 12, weight: .medium, design: .rounded)).foregroundStyle(Color.pingletMutedInk)
            }
            Spacer()
            if showsChevron { Image(systemName: "chevron.right").font(.caption.bold()).foregroundStyle(Color.pingletMutedInk.opacity(0.5)) }
        }
        .contentShape(Rectangle())
    }

    private func patchMix(_ value: String) async {
        struct Body: Encodable { let personalSystemMix: String }
        let response: PreferenceResponse? = try? await env.session.perform(
            "/api/v1/me/preferences",
            method: .patch,
            body: Body(personalSystemMix: value)
        )
        guard response != nil else { return }
        await env.syncFeed()
    }

    private func signOut() async {
        working = true
        workingMessage = "Signing out..."
        defer { working = false; workingMessage = "" }
        do { try await env.signOut(); error = nil }
        catch { self.error = "Sign-out could not reach PingLet. Check your connection and try again." }
    }

    private func deletionAction() async {
        guard let email = entitlement?.email else { return }
        working = true
        workingMessage = deletionCodeSent ? "Deleting your account..." : "Sending verification code..."
        defer { working = false; workingMessage = "" }
        do {
            if !deletionCodeSent {
                let _: EmailOTPResponse = try await env.session.perform(
                    "/api/v1/auth/email/request",
                    method: .post,
                    body: EmailOTPRequest(email: email)
                )
                deletionCodeSent = true
                showDelete = true
            } else {
                struct Body: Encodable { let email: String; let code: String }
                let _: BoolResponse = try await env.session.perform(
                    "/api/v1/auth/account",
                    method: .delete,
                    body: Body(email: email, code: deletionCode)
                )
                try await env.session.resetToAnonymous()
                await env.bootstrap()
                resetDeletion()
            }
            error = nil
        } catch {
            self.error = error.localizedDescription
            showDelete = true
        }
    }

    private func resetDeletion() {
        showDelete = false
        deletionCodeSent = false
        deletionCode = ""
    }
}
