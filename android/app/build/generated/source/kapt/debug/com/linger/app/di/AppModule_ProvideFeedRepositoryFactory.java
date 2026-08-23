package com.linger.app.di;

import com.linger.app.data.local.dao.ContentDao;
import com.linger.app.data.repository.FeedRepository;
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
public final class AppModule_ProvideFeedRepositoryFactory implements Factory<FeedRepository> {
  private final Provider<ContentDao> daoProvider;

  public AppModule_ProvideFeedRepositoryFactory(Provider<ContentDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public FeedRepository get() {
    return provideFeedRepository(daoProvider.get());
  }

  public static AppModule_ProvideFeedRepositoryFactory create(
      javax.inject.Provider<ContentDao> daoProvider) {
    return new AppModule_ProvideFeedRepositoryFactory(Providers.asDaggerProvider(daoProvider));
  }

  public static AppModule_ProvideFeedRepositoryFactory create(Provider<ContentDao> daoProvider) {
    return new AppModule_ProvideFeedRepositoryFactory(daoProvider);
  }

  public static FeedRepository provideFeedRepository(ContentDao dao) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFeedRepository(dao));
  }
}
