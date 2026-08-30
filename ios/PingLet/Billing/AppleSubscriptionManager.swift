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
    @Published private(set) var message: String?
    @Published private(set) var error: String?

    private var updatesTask: Task<Void, Never>?
    private var started = false

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
        guard environment.entitlement?.paidPlansEnabled == true else { return }
        await loadProducts()
        await reconcileCurrentEntitlements(environment)
    }

    func purchase(_ environment: AppEnvironment) async {
        guard let product = selectedProduct, !purchasing else { return }
        purchasing = true
        error = nil
        message = nil
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
        purchasing = false
    }

    func restore(_ environment: AppEnvironment) async {
        guard !purchasing else { return }
        purchasing = true
        error = nil
        message = nil
        do {
            try await AppStore.sync()
            let found = await reconcileCurrentEntitlements(environment)
            if found {
                message = "Your PingLet Plus subscription has been restored."
            } else {
                error = "No active PingLet subscription was found for this Apple Account."
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
        do {
            products = try await Product.products(for: Self.productIDs).sorted { lhs, rhs in
                if lhs.id == Self.annualProductID { return true }
                if rhs.id == Self.annualProductID { return false }
                return lhs.price < rhs.price
            }
            if products.isEmpty { error = "Subscriptions are temporarily unavailable. Try again later." }
        } catch {
            self.error = "Apple subscriptions are temporarily unavailable."
        }
        loading = false
    }

    @discardableResult private func reconcileCurrentEntitlements(_ environment: AppEnvironment) async -> Bool {
        var found = false
        for await result in Transaction.currentEntitlements {
            guard case .verified(let transaction) = result,
                  Self.productIDs.contains(transaction.productID),
                  transaction.revocationDate == nil,
                  transaction.expirationDate.map({ $0 > Date() }) ?? true else { continue }
            found = true
            await process(result, environment: environment, reportErrors: false)
        }
        if !found { await environment.refreshEntitlement() }
        return found
    }

    private func process(_ result: VerificationResult<Transaction>, environment: AppEnvironment, reportErrors: Bool) async {
        guard case .verified(let transaction) = result else {
            if reportErrors { error = "Apple could not verify this transaction." }
            return
        }
        guard Self.productIDs.contains(transaction.productID) else { return }
        do {
            let entitlement: Entitlement = try await environment.session.perform(
                "/api/v1/me/entitlements/apple",
                method: .post,
                body: ApplePurchaseRequest(signedTransaction: result.jwsRepresentation)
            )
            environment.entitlement = entitlement
            environment.shared.entitlement = entitlement
            await transaction.finish()
            purchaseCompleted = entitlement.plan == "PLUS"
            await environment.track("PURCHASE_COMPLETED", metadata: transaction.productID)
        } catch {
            if reportErrors { self.error = error.localizedDescription }
        }
    }
}
