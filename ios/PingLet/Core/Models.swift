import Foundation

enum ContentType: String, Codable, CaseIterable { case quote = "QUOTE", reminder = "REMINDER", affirmation = "AFFIRMATION", goal = "GOAL", message = "MESSAGE", note = "NOTE", passage = "PASSAGE" }
enum ContentSource: String, Codable { case personal = "PERSONAL", system = "SYSTEM" }

struct AuthAnonymousRequest: Encodable { let installationId: String; let platform = "IOS"; let timezone: String; let locale: String; let appVersion: String }
struct AuthAnonymousResponse: Decodable { let accessToken: String; let refreshToken: String; let userId: String }
struct AuthRefreshRequest: Encodable { let refreshToken: String }
struct AuthRefreshResponse: Decodable { let accessToken: String; let expiresIn: Int; let tokenType: String }
struct EmailOTPRequest: Encodable { let email: String }
struct EmailOTPVerifyRequest: Encodable { let email: String; let code: String }
struct EmailOTPResponse: Decodable { let sent: Bool; let expiresInSeconds: Int; let devCode: String? }
struct EmailOTPVerifyResponse: Decodable { let verified: Bool; let email: String; let plan: String; let accessToken: String?; let refreshToken: String?; let userId: String? }

struct FeedResponse: Decodable { let items: [FeedItem] }
struct FeedItem: Codable, Identifiable {
    let id: String
    let text: String
    let type: ContentType
    let author: String?
    let sourceUrl: String?
    let categories: [String]
    let catalogIds: [String]
    let source: ContentSource
    let favorite: Bool
    let updatedAt: String?

    private enum CodingKeys: String, CodingKey { case id, text, type, author, sourceUrl, categories, catalogIds, source, favorite, updatedAt }

    init(from decoder: Decoder) throws {
        let values = try decoder.container(keyedBy: CodingKeys.self)
        id = try values.decode(String.self, forKey: .id)
        text = try values.decode(String.self, forKey: .text)
        type = try values.decode(ContentType.self, forKey: .type)
        author = try values.decodeIfPresent(String.self, forKey: .author)
        sourceUrl = try values.decodeIfPresent(String.self, forKey: .sourceUrl)
        categories = try values.decodeIfPresent([String].self, forKey: .categories) ?? []
        catalogIds = try values.decodeIfPresent([String].self, forKey: .catalogIds) ?? []
        source = try values.decodeIfPresent(ContentSource.self, forKey: .source) ?? .personal
        favorite = try values.decodeIfPresent(Bool.self, forKey: .favorite) ?? false
        updatedAt = try values.decodeIfPresent(String.self, forKey: .updatedAt)
    }
}
struct UserContent: Codable, Identifiable { let id: String; let contentItemId: String; var favorite: Bool; let archived: Bool; let contentItem: FeedItem }
struct ContentInsight: Codable, Identifiable { var id: String { title }; let title: String; let explanation: String; let evidence: String }
struct DetailAccess: Codable { let plan: String; let hasAnalysis: Bool; let fullDetailsUnlocked: Bool; let lockedSections: [String]; let isAnonymous: Bool; let entitlementSource: String; let accessExpiresAt: String?; let trialStatus: String; let trialEligible: Bool; let trialEndsAt: String?; let trialDaysRemaining: Int; let paidPlansEnabled: Bool }
struct DetailItem: Codable, Identifiable { let id: String; let text: String; let type: ContentType; let author: String?; let sourceUrl: String?; let sourcePlatform: String?; let favorite: Bool; let categories: [String] }
struct ContentDetail: Codable { let content: DetailItem; let overview: String?; let insights: [ContentInsight]; let comprehensiveSummary: String?; let actions: [String]; let themes: [String]; let takeaways: [DerivedTakeaway]; let transcript: String?; let visibleText: String?; let caption: String?; let access: DetailAccess }
struct CatalogItem: Codable, Identifiable { let id: String; let text: String; let type: String; let author: String?; let sourceUrl: String? }
struct Catalog: Codable, Identifiable { let id: String; let slug: String; let name: String; let description: String?; var enabled: Bool; var itemCount: Int; var previewItems: [CatalogItem] }
struct CatalogDetail: Codable, Identifiable { let id: String; let slug: String; let name: String; let description: String?; var enabled: Bool; var itemCount: Int; var items: [CatalogItem] }
struct ExploreAction: Codable { let success: Bool; let hiddenContentIds: [String] }
struct BoolResponse: Codable { let success: Bool?; let deleted: Bool? }
struct CatalogPreference: Codable { let catalogId: String; let enabled: Bool }
struct PreferenceResponse: Codable { let refreshMinutes: Int; let personalSystemMix: String; let theme: String }
struct IngestionRequest: Encodable { let url: String; let contextText: String? }
struct DerivedTakeaway: Codable { let text: String; let type: String; let confidence: Double }
struct IngestedContent: Codable { let id: String; let text: String; let type: ContentType; let author: String?; let sourceUrl: String?; let sourcePlatform: String? }
struct Ingestion: Codable, Identifiable { let id: String; let status: String; let processingStage: String?; let caption: String?; let transcript: String?; let ocrText: String?; let takeaways: [DerivedTakeaway]?; let extractionConfidence: Double?; let moderationStatus: String?; let errorCode: String?; let errorMessage: String?; let contentItem: IngestedContent? }

struct Entitlement: Codable { let plan: String; let isAnonymous: Bool; let email: String?; let saveCount: Int; let saveLimit: Int?; let socialImportsUsed: Int; let socialImportLimit: Int; let accountPromptRecommended: Bool; let plusExpiresAt: String?; let entitlementSource: String; let accessExpiresAt: String?; let trialStatus: String; let trialEligible: Bool; let trialStartedAt: String?; let trialEndsAt: String?; let trialDaysRemaining: Int; let paidPlansEnabled: Bool }
struct TermsStatus: Codable { let currentVersion: String; let accepted: Bool; let acceptedAt: String? }
struct EventPayload: Encodable { let type: String; let contentItemId: String?; let surface: String; let timestamp: String; let metadata: String? }
struct EventBatch: Encodable { let events: [EventPayload] }

struct WidgetProfile: Codable, Equatable {
    var name = "My PingLet"; var theme = "BLEND"; var contentMode = "MIXED"; var catalogIds = Set<String>(); var scheduleMode = "ANYTIME"
    var typography = "EDITORIAL"; var textScale = "SMALL"; var spacing = "COMFORTABLE"; var opacity = 78; var manualNext = false; var manualOffset = 0
    var currentContentId = ""; var currentText = ""; var currentAuthor: String?; var currentSourceUrl: String?; var currentFavorite = false; var shownAt: Int64 = 0; var nextChangeAt: Int64 = 0
}
struct PendingFavorite: Codable, Identifiable { let id: UUID; let contentID: String; let favorite: Bool; let createdAt: Date }
