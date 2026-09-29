import SwiftUI
import WidgetKit

private struct LegacySettingsView: View {
    @EnvironmentObject private var env: AppEnvironment; @State private var deleting = false; @State private var deletionCodeSent = false; @State private var code = ""; @State private var error: String?
    var body: some View { NavigationStack { PingLetPage(eyebrow: "Settings", title: "Your PingLet, your rhythm.", subtitle: "A quieter place for your account and experience.") {
        accountCard
        accountActions
        Text("EXPERIENCE").font(.caption.bold()); PingLetCard { Text("Every 30 minutes").font(.headline); Text("Approximate timing, optimized for battery").foregroundStyle(.secondary); Picker("Content balance", selection: Binding(get: { env.shared.contentMix }, set: { value in env.shared.contentMix = value; Task { await patchMix(value) } })) { Text("Mine").tag("MOSTLY_MINE"); Text("Balanced").tag("BALANCED"); Text("Discover").tag("MORE_DISCOVERY") }.pickerStyle(.segmented); Text("Personal saves are always prioritized.").font(.caption); NavigationLink("Widget appearance") { WidgetSettingsView() }; Divider(); NavigationLink("Processing queue") { ProcessingQueueView() } }
        Text("ABOUT").font(.caption.bold()); PingLetCard { Text("PingLet for iOS"); Text("Version \(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0")").foregroundStyle(.secondary); Divider(); Link("Privacy policy", destination: URL(string: "https://pinglet.ai/privacy")!); Divider(); Link("Terms of service", destination: URL(string: "https://pinglet.ai/terms")!) }
        Text("Rotation can shift slightly while iOS is conserving battery.").font(.caption).foregroundStyle(.secondary)
    }.task { await env.refreshEntitlement() }.alert("Delete account and data?", isPresented: $deleting) { TextField("Verification code", text: $code); Button(deletionCodeSent ? "DELETE PERMANENTLY" : "SEND VERIFICATION CODE", role: .destructive) { Task { await deleteAction() } }; Button("CANCEL", role: .cancel) {} } message: { Text(deletionCodeSent ? "Enter the six-digit code sent to \(env.entitlement?.email ?? "your email"). This cannot be undone." : "This permanently deletes your account, personal saves, imports, favorites, devices, and account history.") } } }
    private var accountCard: some View { PingLetCard { let e = env.entitlement; HStack { VStack(alignment: .leading) { Text(e?.trialStatus == "ACTIVE" ? "PingLet Plus trial" : e?.plan == "PLUS" ? "PingLet Plus" : e?.isAnonymous == false ? "Free account" : "Guest profile").font(.title2); Text(e?.email ?? "Not connected to an email").foregroundStyle(.secondary) }; Spacer(); Text(e?.isAnonymous == false ? "VERIFIED" : "LOCAL").font(.caption.bold()) }; if let e { Divider(); HStack { Text("SAVES\n\(e.saveLimit.map { "\(e.saveCount) of \($0)" } ?? "Unlimited")"); Spacer(); Text("AI IMPORTS\n\(e.socialImportsUsed) of \(e.socialImportLimit)") }; if e.trialStatus == "ACTIVE" { Text("\(e.trialDaysRemaining) days of Plus remaining. Your account returns to Free automatically; you will not be charged.") }; if e.isAnonymous { Text("CONNECT EMAIL").font(.headline) } else { Divider(); HStack { Button("SIGN OUT") { Task { do { try await env.signOut() } catch { self.error = "Sign-out could not reach PingLet. Check your connection and try again." } } }; Spacer(); Button("DELETE ACCOUNT", role: .destructive) { deleting = true } } } }; if let error { Text(error).foregroundStyle(.red) } } }
    @ViewBuilder private var accountActions: some View {
        if env.entitlement?.isAnonymous != false { NavigationLink("CONNECT EMAIL") { AccountConnectionView() }.buttonStyle(.borderedProminent) }
        else if env.entitlement?.trialStatus != "ACTIVE" && env.entitlement?.plan != "PLUS" && env.entitlement?.trialEligible == true { NavigationLink("TRY PINGLET PLUS - 7 DAYS FREE") { TrialOfferView() }.buttonStyle(.borderedProminent) }
        if env.entitlement?.paidPlansEnabled == true && env.entitlement?.plan != "PLUS" { NavigationLink("VIEW PAID PLANS") { PlusPlansView() }.buttonStyle(.bordered) }
    }
    private func patchMix(_ value: String) async { struct Body: Encodable { let personalSystemMix: String }; let response: PreferenceResponse? = try? await env.session.perform("/api/v1/me/preferences", method: .patch, body: Body(personalSystemMix: value)); if response != nil { await env.syncFeed() } }
    private func deleteAction() async { guard let email = env.entitlement?.email else { return }; do { if !deletionCodeSent { let _: EmailOTPResponse = try await env.session.perform("/api/v1/auth/email/request", method: .post, body: EmailOTPRequest(email: email)); deletionCodeSent = true; deleting = true } else { struct Body: Encodable { let email: String; let code: String }; let _: BoolResponse = try await env.session.perform("/api/v1/auth/account", method: .delete, body: Body(email: email, code: code)); try await env.session.resetToAnonymous(); await env.bootstrap() } } catch { self.error = error.localizedDescription } }
}

struct ProcessingQueueView: View {
    @EnvironmentObject private var env: AppEnvironment
    @State private var items: [Ingestion] = []
    @State private var loading = true
    @State private var error: String?
    @State private var detailID: String?
    @State private var showingAdd = false
    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 16) {
                Text("From a saved link to a thought worth keeping.").font(.subheadline).foregroundStyle(Color.pingletMutedInk)
                if loading && items.isEmpty { ProgressView("Loading your imports").frame(maxWidth: .infinity) }
                if let error { Label(error, systemImage: "wifi.exclamationmark"); Button("Try again") { Task { await load() } } }
                if !loading && items.isEmpty && error == nil {
                    ContentUnavailableView("Nothing waiting", systemImage: "tray", description: Text("Posts you share will appear here."))
                }
                ForEach(items) { item in
                    PingLetCard {
                        Label(statusTitle(item.status), systemImage: statusIcon(item.status))
                            .font(.caption.weight(.semibold)).foregroundStyle(Color.pingletClay)
                        Text(item.contentItem?.text ?? item.caption ?? "Shared post")
                            .font(.title3).fontDesign(.serif).lineLimit(4)
                        if item.status == "READY", let content = item.contentItem {
                            Button("Open PingLet") { detailID = content.id }
                        } else if ["FAILED", "REJECTED"].contains(item.status) {
                            Text(item.errorMessage ?? "This post could not be imported. Try another public link or save the words yourself.")
                                .font(.subheadline).foregroundStyle(Color.pingletMutedInk)
                            Button("Save another link or write a note") { showingAdd = true }
                        } else {
                            Label("Extracting the words and ideas. You can leave this screen.", systemImage: "clock")
                                .font(.subheadline).foregroundStyle(Color.pingletMutedInk)
                        }
                    }
                }
            }.padding(22)
        }
        .background(PingLetCanvas()).navigationTitle("Your imports").navigationBarTitleDisplayMode(.inline)
        .refreshable { await load() }
        .sheet(isPresented: Binding(get: { detailID != nil }, set: { if !$0 { detailID = nil } })) {
            if let detailID { ContentDetailView(contentID: detailID) }
        }
        .sheet(isPresented: $showingAdd) { AddPingLetView(initialText: "") }
        .task {
            while !Task.isCancelled {
                await load()
                try? await Task.sleep(for: .seconds(5))
            }
        }
    }
    private func statusTitle(_ status: String) -> String {
        switch status { case "READY": return "Ready to read"; case "FAILED": return "Needs attention"; case "REJECTED": return "Could not be added"; default: return "In progress" }
    }
    private func statusIcon(_ status: String) -> String {
        switch status { case "READY": return "checkmark.circle"; case "FAILED", "REJECTED": return "exclamationmark.circle"; default: return "clock" }
    }
    private func load() async {
        defer { loading = false }
        do {
            let rows: [Ingestion] = try await env.session.perform("/api/v1/me/ingestions")
            let previous = Set(items.filter { $0.status == "READY" }.map(\.id))
            items = rows
            error = nil
            if rows.contains(where: { $0.status == "READY" && !previous.contains($0.id) }) {
                try? await env.refreshLibrary()
                await env.syncFeed()
            }
        } catch { self.error = "Imports could not be refreshed. Check your connection and try again." }
    }
}

struct WidgetSettingsView: View {
    @EnvironmentObject private var env: AppEnvironment
    @State private var key = "default"
    @State private var profile = WidgetProfile()
    @State private var large = false
    private var plus: Bool { env.entitlement?.plan == "PLUS" }
    private var preview: WidgetProfile {
        var value = SharedWidgetSelector.resolvedProfile(feed: env.feed, stored: profile, plus: plus, key: key, date: .now)
        if value.currentContentId.isEmpty {
            value.currentContentId = "preview"
            value.currentText = "The things you keep can change the way you see the day."
            value.currentAuthor = "Your next little PingLet"
        }
        return value
    }
    var body: some View {
        Form {
            Section {
                Picker("Profile", selection: $key) {
                    Text("Default").tag("default")
                    Text("Widget 2").tag("profile2")
                    Text("Widget 3").tag("profile3")
                }.pickerStyle(.segmented)
                Text("Hold a Home Screen widget, choose Edit Widget, then select the matching profile.")
                    .font(.footnote).foregroundStyle(Color.pingletMutedInk)
            }
            Section("Live preview") {
                Picker("Size", selection: $large) { Text("Medium").tag(false); Text("Large").tag(true) }.pickerStyle(.segmented)
                let artwork = PingLetWidgetArtwork(profile: preview, large: large) {
                    if plus && profile.manualNext { Image(systemName: "arrow.right").frame(width: 44, height: 36) }
                    Image(systemName: preview.currentFavorite ? "heart.fill" : "heart").frame(width: 44, height: 36)
                }
                artwork.padding(16).frame(height: large ? 330 : 180)
                    .background(artwork.surface.opacity(Double(profile.opacity) / 100), in: RoundedRectangle(cornerRadius: 24))
                    .allowsHitTesting(false).accessibilityLabel("Widget appearance preview")
                Text("Preview only. Your Home Screen layout adapts to the widget size.").font(.caption).foregroundStyle(Color.pingletMutedInk)
            }
            Section("Text & surface") {
                Picker("Text size", selection: $profile.textScale) {
                    Text("Small").tag("SMALL"); Text("Medium").tag("MEDIUM"); Text("Large").tag("LARGE")
                }.pickerStyle(.segmented)
                Picker("Surface opacity", selection: $profile.opacity) {
                    Text("Soft").tag(62); Text("Balanced").tag(78); Text("Solid").tag(100)
                }.pickerStyle(.segmented)
            }
            if !plus {
                Section {
                    Label("Make it yours with Plus", systemImage: "sparkles").font(.headline)
                    Text("Distinct themes, typography, independent content profiles, and a button for your next thought.")
                    if env.entitlement?.isAnonymous != false {
                        NavigationLink("Connect your account") { AccountConnectionView() }
                    } else if env.entitlement?.trialEligible == true {
                        NavigationLink("Explore PingLet Plus") { TrialOfferView() }
                    } else if env.entitlement?.paidPlansEnabled == true {
                        NavigationLink("Explore PingLet Plus") { PlusPlansView() }
                    }
                }
            }
            Section("Appearance · Plus") {
                Picker("Theme", selection: $profile.theme) {
                    Text("Paper").tag("BLEND"); Text("Ink").tag("INK"); Text("Forest").tag("FOREST"); Text("Clay").tag("CLAY")
                }
                Picker("Typography", selection: $profile.typography) {
                    Text("Editorial").tag("EDITORIAL"); Text("Clean").tag("CLEAN"); Text("Rounded").tag("COMPACT")
                }
                Picker("Spacing", selection: $profile.spacing) {
                    Text("Compact").tag("COMPACT"); Text("Comfortable").tag("COMFORTABLE"); Text("Airy").tag("AIRY")
                }
            }.disabled(!plus)
            Section {
                Picker("Content", selection: $profile.contentMode) {
                    Text("Personal & curated").tag("MIXED"); Text("My saves only").tag("PERSONAL"); Text("Collections only").tag("COLLECTIONS")
                }
                Picker("Rhythm", selection: $profile.scheduleMode) {
                    Text("Anytime").tag("ANYTIME"); Text("Morning & evening").tag("DAY_RHYTHM"); Text("Time of day").tag("CONTEXTUAL")
                }
                Toggle("Show next button", isOn: $profile.manualNext)
            } header: { Text("Content & rhythm · Plus") } footer: {
                Text("Collections use your Explore preferences. If no content matches, your widget will invite you to add some. iOS controls the exact refresh time.")
            }.disabled(!plus)
        }
        .scrollContentBackground(.hidden).background(PingLetCanvas())
        .navigationTitle("Your widget").navigationBarTitleDisplayMode(.inline).tint(Color.pingletClay)
        .onAppear { loadProfile() }
        .onChange(of: key) { _, _ in loadProfile() }
        .onChange(of: profile) { _, value in env.shared.setWidgetProfile(value, key: key); WidgetCenter.shared.reloadAllTimelines() }
    }
    private func loadProfile() {
        profile = env.shared.widgetProfile(key: key)
        if profile.opacity == 90 { profile.opacity = 100 }
    }
}
