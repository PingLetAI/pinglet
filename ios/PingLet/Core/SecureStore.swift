import Foundation
import Security

struct Credentials: Codable { let accessToken: String; let refreshToken: String; let userId: String }
final class SecureStore {
    private let service = "ai.pinglet.app.session"
    private var sharedAccessGroup: String? { Bundle.main.object(forInfoDictionaryKey: "PingLetKeychainAccessGroup") as? String }
    func read() -> Credentials? {
        if let credentials = read(accessGroup: sharedAccessGroup) { return credentials }
        guard let legacy = read(accessGroup: nil) else { return nil }
        try? write(legacy)
        return legacy
    }
    private func read(accessGroup: String?) -> Credentials? {
        var query = baseQuery(accessGroup: accessGroup); query[kSecReturnData as String] = true
        var result: CFTypeRef?
        guard SecItemCopyMatching(query as CFDictionary, &result) == errSecSuccess, let data = result as? Data else { return nil }
        return try? JSONDecoder().decode(Credentials.self, from: data)
    }
    func write(_ credentials: Credentials) throws {
        let data = try JSONEncoder().encode(credentials)
        let query = baseQuery(accessGroup: sharedAccessGroup)
        SecItemDelete(query as CFDictionary)
        var item = query; item[kSecValueData as String] = data; item[kSecAttrAccessible as String] = kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
        let status = SecItemAdd(item as CFDictionary, nil); guard status == errSecSuccess else { throw NSError(domain: NSOSStatusErrorDomain, code: Int(status)) }
    }
    func clear() { SecItemDelete(baseQuery(accessGroup: sharedAccessGroup) as CFDictionary); SecItemDelete(baseQuery(accessGroup: nil) as CFDictionary) }
    private func baseQuery(accessGroup: String?) -> [String: Any] {
        var query: [String: Any] = [kSecClass as String: kSecClassGenericPassword, kSecAttrService as String: service]
        if let accessGroup, !accessGroup.isEmpty { query[kSecAttrAccessGroup as String] = accessGroup }
        return query
    }
}
