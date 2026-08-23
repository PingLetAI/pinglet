package com.linger.app.di;

import com.linger.app.data.local.dao.ContentDao;
import com.linger.app.data.remote.AppApiService;
import com.linger.app.data.repository.ContentRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideContentRepositoryFactory implements Factory<ContentRepository> {
  private final Provider<AppApiService> apiProvider;

  private final Provider<ContentDao> daoProvider;

  public AppModule_ProvideContentRepositoryFactory(Provider<AppApiService> apiProvider,
      Provider<ContentDao> daoProvider) {
    this.apiProvider = apiProvider;
    this.daoProvider = daoProvider;
  }

  @Override
  public ContentRepository get() {
    return provideContentRepository(apiProvider.get(), daoProvider.get());
  }

  public static AppModule_ProvideContentRepositoryFactory create(
      javax.inject.Provider<AppApiService> apiProvider,
      javax.inject.Provider<ContentDao> daoProvider) {
    return new AppModule_ProvideContentRepositoryFactory(Providers.asDaggerProvider(apiProvider), Providers.asDaggerProvider(daoProvider));
  }

  public static AppModule_ProvideContentRepositoryFactory create(
      Provider<AppApiService> apiProvider, Provider<ContentDao> daoProvider) {
    return new AppModule_ProvideContentRepositoryFactory(apiProvider, daoProvider);
  }

  public static ContentRepository provideContentRepository(AppApiService api, ContentDao dao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideContentRepository(api, dao));
  }
}
