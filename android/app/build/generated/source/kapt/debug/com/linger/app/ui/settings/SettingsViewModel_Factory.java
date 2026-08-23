package com.linger.app.ui.settings;

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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<AppApiService> apiProvider;

  private final Provider<SessionManager> sessionProvider;

  public SettingsViewModel_Factory(Provider<AppApiService> apiProvider,
      Provider<SessionManager> sessionProvider) {
    this.apiProvider = apiProvider;
    this.sessionProvider = sessionProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(apiProvider.get(), sessionProvider.get());
  }

  public static SettingsViewModel_Factory create(javax.inject.Provider<AppApiService> apiProvider,
      javax.inject.Provider<SessionManager> sessionProvider) {
    return new SettingsViewModel_Factory(Providers.asDaggerProvider(apiProvider), Providers.asDaggerProvider(sessionProvider));
  }

  public static SettingsViewModel_Factory create(Provider<AppApiService> apiProvider,
      Provider<SessionManager> sessionProvider) {
    return new SettingsViewModel_Factory(apiProvider, sessionProvider);
  }

  public static SettingsViewModel newInstance(AppApiService api, SessionManager session) {
    return new SettingsViewModel(api, session);
  }
}
