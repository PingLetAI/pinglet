package com.linger.app.ui.library;

import android.content.Context;
import com.linger.app.data.local.DataStoreManager;
import com.linger.app.data.local.dao.ContentDao;
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
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class LibraryViewModel_Factory implements Factory<LibraryViewModel> {
  private final Provider<AppApiService> apiProvider;

  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<ContentDao> daoProvider;

  private final Provider<DataStoreManager> dataStoreProvider;

  private final Provider<Context> contextProvider;

  public LibraryViewModel_Factory(Provider<AppApiService> apiProvider,
      Provider<SessionManager> sessionManagerProvider, Provider<ContentDao> daoProvider,
      Provider<DataStoreManager> dataStoreProvider, Provider<Context> contextProvider) {
    this.apiProvider = apiProvider;
    this.sessionManagerProvider = sessionManagerProvider;
    this.daoProvider = daoProvider;
    this.dataStoreProvider = dataStoreProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public LibraryViewModel get() {
    return newInstance(apiProvider.get(), sessionManagerProvider.get(), daoProvider.get(), dataStoreProvider.get(), contextProvider.get());
  }

  public static LibraryViewModel_Factory create(javax.inject.Provider<AppApiService> apiProvider,
      javax.inject.Provider<SessionManager> sessionManagerProvider,
      javax.inject.Provider<ContentDao> daoProvider,
      javax.inject.Provider<DataStoreManager> dataStoreProvider,
      javax.inject.Provider<Context> contextProvider) {
    return new LibraryViewModel_Factory(Providers.asDaggerProvider(apiProvider), Providers.asDaggerProvider(sessionManagerProvider), Providers.asDaggerProvider(daoProvider), Providers.asDaggerProvider(dataStoreProvider), Providers.asDaggerProvider(contextProvider));
  }

  public static LibraryViewModel_Factory create(Provider<AppApiService> apiProvider,
      Provider<SessionManager> sessionManagerProvider, Provider<ContentDao> daoProvider,
      Provider<DataStoreManager> dataStoreProvider, Provider<Context> contextProvider) {
    return new LibraryViewModel_Factory(apiProvider, sessionManagerProvider, daoProvider, dataStoreProvider, contextProvider);
  }

  public static LibraryViewModel newInstance(AppApiService api, SessionManager sessionManager,
      ContentDao dao, DataStoreManager dataStore, Context context) {
    return new LibraryViewModel(api, sessionManager, dao, dataStore, context);
  }
}
