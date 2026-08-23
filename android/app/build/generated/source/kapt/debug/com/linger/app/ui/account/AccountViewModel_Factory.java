package com.linger.app.ui.account;

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
public final class AccountViewModel_Factory implements Factory<AccountViewModel> {
  private final Provider<AppApiService> apiProvider;

  private final Provider<SessionManager> sessionProvider;

  public AccountViewModel_Factory(Provider<AppApiService> apiProvider,
      Provider<SessionManager> sessionProvider) {
    this.apiProvider = apiProvider;
    this.sessionProvider = sessionProvider;
  }

  @Override
  public AccountViewModel get() {
    return newInstance(apiProvider.get(), sessionProvider.get());
  }

  public static AccountViewModel_Factory create(javax.inject.Provider<AppApiService> apiProvider,
      javax.inject.Provider<SessionManager> sessionProvider) {
    return new AccountViewModel_Factory(Providers.asDaggerProvider(apiProvider), Providers.asDaggerProvider(sessionProvider));
  }

  public static AccountViewModel_Factory create(Provider<AppApiService> apiProvider,
      Provider<SessionManager> sessionProvider) {
    return new AccountViewModel_Factory(apiProvider, sessionProvider);
  }

  public static AccountViewModel newInstance(AppApiService api, SessionManager session) {
    return new AccountViewModel(api, session);
  }
}
