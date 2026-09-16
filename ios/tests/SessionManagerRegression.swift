// Runs the production SessionManager and APIClient with in-memory storage and
// intercepted HTTP requests. No Keychain, production account, or network access.
import Foundation

struct Credentials { let accessToken: String; let refreshToken: String; let userId: String }
final class SecureStore {
    var value: Credentials?
    init(_ value: Credentials? = nil) { self.value = value }
    func read() -> Credentials? { value }
    func write(_ value: Credentials) throws { self.value = value }
    func clear() { value = nil }
}
final class SharedStore {
    var installationID = "old-installation"
    var cleared = false
    func rotateInstallationID() { installationID = UUID().uuidString }
    func clearAccountData() { cleared = true }
}

final class MockHTTP: URLProtocol {
    static var handler: ((URLRequest) -> (Int, String))!
    override class func canInit(with request: URLRequest) -> Bool { true }
    override class func canonicalRequest(for request: URLRequest) -> URLRequest { request }
    override func startLoading() {
        let (status, body) = Self.handler(request)
        // Allow concurrent callers to reach the session manager during bootstrap.
        DispatchQueue.global().asyncAfter(deadline: .now() + 0.03) {
            let response = HTTPURLResponse(url: self.request.url!, statusCode: status, httpVersion: nil, headerFields: ["Content-Type": "application/json"])!
            self.client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .notAllowed)
            self.client?.urlProtocol(self, didLoad: Data(body.utf8))
            self.client?.urlProtocolDidFinishLoading(self)
        }
    }
    override func stopLoading() {}
}

struct Reply: Decodable { let success: Bool }

@main struct SessionManagerRegression {
    static func manager(_ secure: SecureStore, _ shared: SharedStore) -> SessionManager {
        let config = URLSessionConfiguration.ephemeral
        config.protocolClasses = [MockHTTP.self]
        return SessionManager(api: APIClient(session: URLSession(configuration: config)), secure: secure, shared: shared)
    }

    static func request(_ manager: SessionManager) async throws -> Reply {
        try await manager.perform("/test")
    }

    static func main() async throws {
        try await concurrentBootstrap()
        try await installationCollision()
        try await transientRefreshFailure()
        print("3 SessionManager regression checks passed")
    }

    static func concurrentBootstrap() async throws {
        let secure = SecureStore(), shared = SharedStore()
        let session = manager(secure, shared)
        var bootstraps = 0
        MockHTTP.handler = { request in
            if request.url!.path == "/api/v1/auth/anonymous" {
                bootstraps += 1
                return (200, #"{"accessToken":"guest-access","refreshToken":"guest-refresh","userId":"guest"}"#)
            }
            precondition(request.value(forHTTPHeaderField: "Authorization") == "Bearer guest-access")
            return (200, #"{"success":true}"#)
        }
        async let first = request(session)
        async let second = request(session)
        let results = try await (first, second)
        precondition(results.0.success && results.1.success && bootstraps == 1)
        precondition(secure.value?.userId == "guest" && shared.cleared)
    }

    static func installationCollision() async throws {
        let secure = SecureStore(), shared = SharedStore()
        let session = manager(secure, shared)
        var bootstraps = 0
        MockHTTP.handler = { request in
            if request.url!.path == "/api/v1/auth/anonymous" {
                bootstraps += 1
                if bootstraps == 1 {
                    return (409, #"{"code":"INSTALLATION_ID_IN_USE","message":"Sign in to recover your account."}"#)
                }
                return (200, #"{"accessToken":"new-access","refreshToken":"new-refresh","userId":"new-guest"}"#)
            }
            return (200, #"{"success":true}"#)
        }
        _ = try await request(session)
        precondition(bootstraps == 2 && shared.installationID != "old-installation")
        precondition(secure.value?.userId == "new-guest")
    }

    static func transientRefreshFailure() async throws {
        let secure = SecureStore(Credentials(accessToken: "old-access", refreshToken: "valid-refresh", userId: "verified-user"))
        let shared = SharedStore()
        let session = manager(secure, shared)
        var bootstraps = 0
        MockHTTP.handler = { request in
            if request.url!.path == "/api/v1/auth/anonymous" { bootstraps += 1 }
            if request.url!.path == "/api/v1/auth/refresh" {
                return (503, #"{"message":"Temporarily unavailable"}"#)
            }
            return (401, #"{"message":"Expired access token"}"#)
        }
        do {
            _ = try await request(session)
            preconditionFailure("Expected refresh failure")
        } catch let error as APIError {
            precondition(error.status == 503)
        }
        precondition(bootstraps == 0 && secure.value?.refreshToken == "valid-refresh")
        precondition(!shared.cleared && shared.installationID == "old-installation")
    }
}
