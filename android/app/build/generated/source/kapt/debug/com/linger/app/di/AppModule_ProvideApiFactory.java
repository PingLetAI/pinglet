package com.linger.app.di;

import com.linger.app.data.remote.AppApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideApiFactory implements Factory<AppApiService> {
  @Override
  public AppApiService get() {
    return provideApi();
  }

  public static AppModule_ProvideApiFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AppApiService provideApi() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideApi());
  }

  private static final class InstanceHolder {
    static final AppModule_ProvideApiFactory INSTANCE = new AppModule_ProvideApiFactory();
  }
}
