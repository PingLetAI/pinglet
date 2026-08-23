package com.linger.app.di;

import com.linger.app.data.remote.AppApiService;
import com.linger.app.data.repository.AuthRepository;
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
public final class AppModule_ProvideAuthRepositoryFactory implements Factory<AuthRepository> {
  private final Provider<AppApiService> apiProvider;

  public AppModule_ProvideAuthRepositoryFactory(Provider<AppApiService> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public AuthRepository get() {
    return provideAuthRepository(apiProvider.get());
  }

  public static AppModule_ProvideAuthRepositoryFactory create(
      javax.inject.Provider<AppApiService> apiProvider) {
    return new AppModule_ProvideAuthRepositoryFactory(Providers.asDaggerProvider(apiProvider));
  }

  public static AppModule_ProvideAuthRepositoryFactory create(Provider<AppApiService> apiProvider) {
    return new AppModule_ProvideAuthRepositoryFactory(apiProvider);
  }

  public static AuthRepository provideAuthRepository(AppApiService api) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAuthRepository(api));
  }
}
