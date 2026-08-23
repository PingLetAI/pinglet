package com.linger.app.data.repository;

import com.linger.app.data.local.DataStoreManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SessionManager_Factory implements Factory<SessionManager> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<DataStoreManager> dataStoreProvider;

  public SessionManager_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<DataStoreManager> dataStoreProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public SessionManager get() {
    return newInstance(authRepositoryProvider.get(), dataStoreProvider.get());
  }

  public static SessionManager_Factory create(
      javax.inject.Provider<AuthRepository> authRepositoryProvider,
      javax.inject.Provider<DataStoreManager> dataStoreProvider) {
    return new SessionManager_Factory(Providers.asDaggerProvider(authRepositoryProvider), Providers.asDaggerProvider(dataStoreProvider));
  }

  public static SessionManager_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<DataStoreManager> dataStoreProvider) {
    return new SessionManager_Factory(authRepositoryProvider, dataStoreProvider);
  }

  public static SessionManager newInstance(AuthRepository authRepository,
      DataStoreManager dataStore) {
    return new SessionManager(authRepository, dataStore);
  }
}
