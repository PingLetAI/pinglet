import Foundation

enum HTTPMethod: String { case get = "GET", post = "POST", patch = "PATCH", delete = "DELETE" }
struct EmptyBody: Encodable {}
enum APIError: Error, LocalizedError {
    case invalidResponse, decoding(Error), http(status: Int, code: String?, message: String?)
    var errorDescription: String? { if case let .http(_, _, message) = self { return message }; return "Something went wrong. Try again." }
    var status: Int? { if case let .http(status, _, _) = self { return status }; return nil }
    var code: String? { if case let .http(_, code, _) = self { return code }; return nil }
}

actor APIClient {
    private let baseURL = URL(string: "https://api.pinglet.ai")!
    private let session: URLSession
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()
    init(session: URLSession = .shared) { self.session = session }

    func send<Response: Decodable, Body: Encodable>(_ path: String, method: HTTPMethod, body: Body? = nil, token: String? = nil) async throws -> Response {
        guard let url = URL(string: path, relativeTo: baseURL)?.absoluteURL else { throw APIError.invalidResponse }
        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let token { request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization") }
        if let body { request.setValue("application/json", forHTTPHeaderField: "Content-Type"); request.httpBody = try encoder.encode(body) }
        let (data, response) = try await session.data(for: request)
        guard let http = response as? HTTPURLResponse else { throw APIError.invalidResponse }
        guard (200..<300).contains(http.statusCode) else {
            let payload = try? decoder.decode(ErrorPayload.self, from: data)
            throw APIError.http(status: http.statusCode, code: payload?.code, message: payload?.message)
        }
        do { return try decoder.decode(Response.self, from: data) } catch { throw APIError.decoding(error) }
    }
}
private struct ErrorPayload: Decodable { let code: String?; let message: String? }
