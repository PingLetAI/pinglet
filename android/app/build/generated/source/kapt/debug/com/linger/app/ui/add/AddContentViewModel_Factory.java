package com.linger.app.ui.add;

import com.linger.app.data.remote.AppApiService;
import com.linger.app.data.repository.ContentRepository;
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
public final class AddContentViewModel_Factory implements Factory<AddContentViewModel> {
  private final Provider<ContentRepository> repositoryProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<AppApiService> apiProvider;

  public AddContentViewModel_Factory(Provider<ContentRepository> repositoryProvider,
      Provider<SessionManager> sessionManagerProvider, Provider<AppApiService> apiProvider) {
    this.repositoryProvider = repositoryProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.apiProvider = apiProvider;
  }

  @Override
  public AddContentViewModel get() {
    return newInstance(repositoryProvider.get(), sessionManagerProvider.get(), apiProvider.get());
  }

  public static AddContentViewModel_Factory create(
      javax.inject.Provider<ContentRepository> repositoryProvider,
      javax.inject.Provider<SessionManager> sessionManagerProvider,
      javax.inject.Provider<AppApiService> apiProvider) {
    return new AddContentViewModel_Factory(Providers.asDaggerProvider(repositoryProvider), Providers.asDaggerProvider(sessionManagerProvider), Providers.asDaggerProvider(apiProvider));
  }

  public static AddContentViewModel_Factory create(Provider<ContentRepository> repositoryProvider,
      Provider<SessionManager> sessionManagerProvider, Provider<AppApiService> apiProvider) {
    return new AddContentViewModel_Factory(repositoryProvider, sessionManagerProvider, apiProvider);
  }

  public static AddContentViewModel newInstance(ContentRepository repository,
      SessionManager sessionManager, AppApiService api) {
    return new AddContentViewModel(repository, sessionManager, api);
  }
}
