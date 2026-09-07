import Foundation
import StoreKit

private struct ApplePurchaseRequest: Encodable { let signedTransaction: String }

@MainActor final class AppleSubscriptionManager: ObservableObject {
    static let monthlyProductID = "ai.pinglet.app.plus.monthly"
    static let annualProductID = "ai.pinglet.app.plus.annual"
    static let subscriptionGroupID = "22344016"
    private static let productIDs = [monthlyProductID, annualProductID]

    @Published private(set) var products: [Product] = []
    @Published var selectedProductID = annualProductID
    @Published private(set) var loading = false
    @Published private(set) var purchasing = false
    @Published private(set) var purchaseCompleted = false
    @Published private(set) var activationPending = false
    @Published private(set) var message: String?
    @Published private(set) var error: String?

    private var updatesTask: Task<Void, Never>?
    private var started = false
    private var pendingActivation: VerificationResult<Transaction>?
    private enum ReconciliationOutcome { case none, activated, failed }

    deinit { updatesTask?.cancel() }

    var selectedProduct: Product? { products.first { $0.id == selectedProductID } }
    var monthlyProduct: Product? { products.first { $0.id == Self.monthlyProductID } }
    var annualProduct: Product? { products.first { $0.id == Self.annualProductID } }
    var annualSavingsPercent: Int? {
        guard let monthlyProduct, let annualProduct else { return nil }
        let annualAtMonthlyRate = NSDecimalNumber(decimal: monthlyProduct.price).multiplying(by: 12)
        guard annualAtMonthlyRate.compare(NSDecimalNumber(value: 0)) == .orderedDescending else { return nil }
        let ratio = NSDecimalNumber(decimal: annualProduct.price).dividing(by: annualAtMonthlyRate).doubleValue
        return max(0, Int(((1 - ratio) * 100).rounded()))
    }

    func start(_ environment: AppEnvironment) {
        guard !started else { return }
        started = true
        updatesTask = Task { [weak self, weak environment] in
            for await result in Transaction.updates {
                guard !Task.isCancelled, let self, let environment else { return }
                await self.process(result, environment: environment, reportErrors: false)
            }
        }
    }

    func prepare(_ environment: AppEnvironment) async {
        #if DEBUG
        print("[PingLetStoreKit] paidPlansEnabled=\(String(describing: environment.entitlement?.paidPlansEnabled))")
        #endif
        guard environment.entitlement?.paidPlansEnabled == true else { return }
        guard !purchasing else { return }
        purchaseCompleted = false
        await loadProducts()
        await reconcileCurrentEntitlements(environment)
    }

    func purchase(_ environment: AppEnvironment) async {
        guard !purchasing, !purchaseCompleted else { return }
        purchasing = true
        defer { purchasing = false }
        error = nil
        message = nil
        // Apple has already accepted this purchase. Retry activation, never payment.
        if let pendingActivation {
            await process(pendingActivation, environment: environment, reportErrors: true)
            return
        }
        guard let product = selectedProduct else { return }
        await environment.track("PURCHASE_STARTED", metadata: product.id)
        do {
            switch try await product.purchase() {
            case .success(let result):
                await process(result, environment: environment, reportErrors: true)
            case .pending:
                message = "Your purchase is pending approval. Plus will unlock when Apple confirms it."
            case .userCancelled:
                break
            @unknown default:
                error = "The purchase could not be completed. Try again."
            }
        } catch {
            self.error = error.localizedDescription
        }
    }

    func restore(_ environment: AppEnvironment) async {
        guard !purchasing else { return }
        purchasing = true
        purchaseCompleted = false
        error = nil
        message = nil
        do {
            try await AppStore.sync()
            switch await reconcileCurrentEntitlements(environment) {
            case .activated:
                message = "Your PingLet Plus subscription has been restored."
            case .none:
                error = "No active PingLet subscription was found for this Apple Account."
            case .failed:
                if error == nil {
                    error = "Apple found your subscription, but Plus could not be activated. Retry activation without purchasing again."
                }
            }
        } catch {
            self.error = "Purchases could not be restored. Try again."
        }
        purchasing = false
    }

    func duration(for product: Product) -> String {
        guard let period = product.subscription?.subscriptionPeriod else { return "" }
        let unit: String
        switch period.unit {
        case .day: unit = period.value == 1 ? "day" : "days"
        case .week: unit = period.value == 1 ? "week" : "weeks"
        case .month: unit = period.value == 1 ? "month" : "months"
        case .year: unit = period.value == 1 ? "year" : "years"
        @unknown default: unit = "period"
        }
        return period.value == 1 ? unit : "\(period.value) \(unit)"
    }

    private func loadProducts() async {
        loading = true
        error = nil
        #if DEBUG
        print("[PingLetStoreKit] bundle=\(Bundle.main.bundleIdentifier ?? "unknown") version=\(Bundle.main.infoDictionary?["CFBundleShortVersionString"] ?? "unknown") build=\(Bundle.main.infoDictionary?["CFBundleVersion"] ?? "unknown")")
        print("[PingLetStoreKit] requested=\(Self.productIDs.joined(separator: ", "))")
        if let storefront = await Storefront.current {
            print("[PingLetStoreKit] storefront=\(storefront.id) country=\(storefront.countryCode)")
        } else {
            print("[PingLetStoreKit] storefront unavailable")
        }
        #endif
        do {
            products = try await Product.products(for: Self.productIDs).sorted { lhs, rhs in
                if lhs.id == Self.annualProductID { return true }
                if rhs.id == Self.annualProductID { return false }
                return lhs.price < rhs.price
            }
            #if DEBUG
            print("[PingLetStoreKit] returnedCount=\(products.count)")
            for product in products {
                print("[PingLetStoreKit] product=\(product.id) type=\(product.type) price=\(product.displayPrice)")
            }
            let missing = Self.productIDs.filter { id in !products.contains { $0.id == id } }
            print("[PingLetStoreKit] missing=\(missing.joined(separator: ", "))")
            #endif
            if products.isEmpty { error = "Subscriptions are temporarily unavailable. Try again later." }
        } catch {
            #if DEBUG
            let storeError = error as NSError
            print("[PingLetStoreKit] product request failed domain=\(storeError.domain) code=\(storeError.code)")
            if let underlying = storeError.userInfo[NSUnderlyingErrorKey] as? NSError {
                print("[PingLetStoreKit] underlying domain=\(underlying.domain) code=\(underlying.code)")
            }
            #endif
            self.error = "Apple subscriptions are temporarily unavailable."
        }
        loading = false
    }

    @discardableResult private func reconcileCurrentEntitlements(_ environment: AppEnvironment) async -> ReconciliationOutcome {
        var found = false
        var activated = false
        for await result in Transaction.currentEntitlements {
            guard case .verified(let transaction) = result,
                  Self.productIDs.contains(transaction.productID),
                  transaction.revocationDate == nil,
                  transaction.expirationDate.map({ $0 > Date() }) ?? true else { continue }
            found = true
            let succeeded = await process(result, environment: environment, reportErrors: true)
            activated = activated || succeeded
        }
        if !found { await environment.refreshEntitlement() }
        return activated ? .activated : found ? .failed : .none
    }

    @discardableResult private func process(_ result: VerificationResult<Transaction>, environment: AppEnvironment, reportErrors: Bool) async -> Bool {
        guard case .verified(let transaction) = result else {
            if reportErrors { error = "Apple could not verify this transaction." }
            return false
        }
        guard Self.productIDs.contains(transaction.productID) else { return false }
        do {
            let entitlement: Entitlement = try await environment.session.perform(
                "/api/v1/me/entitlements/apple",
                method: .post,
                body: ApplePurchaseRequest(signedTransaction: result.jwsRepresentation)
            )
            environment.entitlement = entitlement
            environment.shared.entitlement = entitlement
            await transaction.finish()
            if case .verified(let pending) = pendingActivation, pending.id == transaction.id {
                pendingActivation = nil
                activationPending = false
            }
            let active = transaction.revocationDate == nil
                && (transaction.expirationDate.map { $0 > Date() } ?? false)
                && entitlement.plan == "PLUS"
            if active {
                error = nil
                message = "PingLet Plus is active."
                purchaseCompleted = true
                await environment.track("PURCHASE_COMPLETED", metadata: transaction.productID)
            }
            return active
        } catch {
            if transaction.revocationDate == nil,
               transaction.expirationDate.map({ $0 > Date() }) ?? false {
                pendingActivation = result
                activationPending = true
                if let apiError = error as? APIError, apiError.code == "PURCHASE_ACCOUNT_MISMATCH" {
                    self.error = "This subscription belongs to another PingLet account. Sign in to the account you originally used, then restore purchases."
                } else {
                    self.error = "Apple confirmed your subscription, but PingLet could not activate Plus. Retry activation; you do not need to purchase again."
                }
            } else if reportErrors {
                self.error = error.localizedDescription
            }
            return false
        }
    }
}
