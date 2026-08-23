package com.linger.app.ui.queue;

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
public final class ProcessingQueueViewModel_Factory implements Factory<ProcessingQueueViewModel> {
  private final Provider<AppApiService> apiProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<ContentRepository> contentRepositoryProvider;

  public ProcessingQueueViewModel_Factory(Provider<AppApiService> apiProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<ContentRepository> contentRepositoryProvider) {
    this.apiProvider = apiProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.contentRepositoryProvider = contentRepositoryProvider;
  }

  @Override
  public ProcessingQueueViewModel get() {
    return newInstance(apiProvider.get(), sessionManagerProvider.get(), contentRepositoryProvider.get());
  }

  public static ProcessingQueueViewModel_Factory create(
      javax.inject.Provider<AppApiService> apiProvider,
      javax.inject.Provider<SessionManager> sessionManagerProvider,
      javax.inject.Provider<ContentRepository> contentRepositoryProvider) {
    return new ProcessingQueueViewModel_Factory(Providers.asDaggerProvider(apiProvider), Providers.asDaggerProvider(sessionManagerProvider), Providers.asDaggerProvider(contentRepositoryProvider));
  }

  public static ProcessingQueueViewModel_Factory create(Provider<AppApiService> apiProvider,
      Provider<SessionManager> sessionManagerProvider,
      Provider<ContentRepository> contentRepositoryProvider) {
    return new ProcessingQueueViewModel_Factory(apiProvider, sessionManagerProvider, contentRepositoryProvider);
  }

  public static ProcessingQueueViewModel newInstance(AppApiService api,
      SessionManager sessionManager, ContentRepository contentRepository) {
    return new ProcessingQueueViewModel(api, sessionManager, contentRepository);
  }
}
