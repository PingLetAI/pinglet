package com.linger.app.di;

import com.linger.app.data.local.dao.ContentDao;
import com.linger.app.data.local.db.WidgetDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideContentDaoFactory implements Factory<ContentDao> {
  private final Provider<WidgetDatabase> dbProvider;

  public AppModule_ProvideContentDaoFactory(Provider<WidgetDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ContentDao get() {
    return provideContentDao(dbProvider.get());
  }

  public static AppModule_ProvideContentDaoFactory create(
      javax.inject.Provider<WidgetDatabase> dbProvider) {
    return new AppModule_ProvideContentDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvideContentDaoFactory create(Provider<WidgetDatabase> dbProvider) {
    return new AppModule_ProvideContentDaoFactory(dbProvider);
  }

  public static ContentDao provideContentDao(WidgetDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideContentDao(db));
  }
}
