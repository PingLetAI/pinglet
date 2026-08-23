package com.linger.app.ui.paywall;

import com.linger.app.billing.PlayBillingManager;
import com.linger.app.data.remote.AppApiService;
import com.linger.app.data.repository.SessionManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class PaywallViewModel_Factory implements Factory<PaywallViewModel> {
  private final Provider<PlayBillingManager> billingProvider;

  private final Provider<AppApiService> apiProvider;

  private final Provider<SessionManager> sessionProvider;

  public PaywallViewModel_Factory(Provider<PlayBillingManager> billingProvider,
      Provider<AppApiService> apiProvider, Provider<SessionManager> sessionProvider) {
    this.billingProvider = billingProvider;
    this.apiProvider = apiProvider;
    this.sessionProvider = sessionProvider;
  }

  @Override
  public PaywallViewModel get() {
    return newInstance(billingProvider.get(), apiProvider.get(), sessionProvider.get());
  }

  public static PaywallViewModel_Factory create(
      javax.inject.Provider<PlayBillingManager> billingProvider,
      javax.inject.Provider<AppApiService> apiProvider,
      javax.inject.Provider<SessionManager> sessionProvider) {
    return new PaywallViewModel_Factory(Providers.asDaggerProvider(billingProvider), Providers.asDaggerProvider(apiProvider), Providers.asDaggerProvider(sessionProvider));
  }

  public static PaywallViewModel_Factory create(Provider<PlayBillingManager> billingProvider,
      Provider<AppApiService> apiProvider, Provider<SessionManager> sessionProvider) {
    return new PaywallViewModel_Factory(billingProvider, apiProvider, sessionProvider);
  }

  public static PaywallViewModel newInstance(PlayBillingManager billing, AppApiService api,
      SessionManager session) {
    return new PaywallViewModel(billing, api, session);
  }
}
