import Foundation

actor SessionManager {
    private let api: APIClient; private let secure: SecureStore; private let shared: SharedStore
    private var credentials: Credentials?
    init(api: APIClient, secure: SecureStore, shared: SharedStore) { self.api = api; self.secure = secure; self.shared = shared; credentials = secure.read() }

    func perform<Response: Decodable, Body: Encodable>(_ path: String, method: HTTPMethod = .get, body: Body? = nil) async throws -> Response {
        try await ensureSession()
        do { return try await api.send(path, method: method, body: body, token: credentials?.accessToken) }
        catch let error as APIError where error.status == 401 {
            try await ensureSession(forceRefresh: true)
            return try await api.send(path, method: method, body: body, token: credentials?.accessToken)
        }
    }
    func perform<Response: Decodable>(_ path: String, method: HTTPMethod = .get) async throws -> Response { try await perform(path, method: method, body: Optional<EmptyBody>.none) }

    func installVerifiedSession(_ response: EmailOTPVerifyResponse) throws -> Bool {
        guard let access = response.accessToken, let refresh = response.refreshToken, let user = response.userId, !access.isEmpty, !refresh.isEmpty, !user.isEmpty else { return false }
        shared.clearAccountData(); secure.clear()
        let replacement = Credentials(accessToken: access, refreshToken: refresh, userId: user)
        try secure.write(replacement); credentials = replacement; return true
    }
    func resetToAnonymous() async throws { secure.clear(); credentials = nil; shared.clearAccountData(); try await ensureSession() }

    private func ensureSession(forceRefresh: Bool = false) async throws {
        if !forceRefresh, credentials != nil { return }
        if let current = credentials {
            do {
                let refreshed: AuthRefreshResponse = try await api.send("/api/v1/auth/refresh", method: .post, body: AuthRefreshRequest(refreshToken: current.refreshToken))
                let next = Credentials(accessToken: refreshed.accessToken, refreshToken: current.refreshToken, userId: current.userId)
                try secure.write(next); credentials = next; return
            } catch { secure.clear(); credentials = nil }
        }
        let request = AuthAnonymousRequest(installationId: shared.installationID, timezone: TimeZone.current.identifier, locale: Locale.current.identifier, appVersion: Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0")
        let anonymous: AuthAnonymousResponse = try await api.send("/api/v1/auth/anonymous", method: .post, body: request)
        let next = Credentials(accessToken: anonymous.accessToken, refreshToken: anonymous.refreshToken, userId: anonymous.userId)
        try secure.write(next); credentials = next
    }
}
