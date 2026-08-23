package com.linger.app.billing;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class PlayBillingManager_Factory implements Factory<PlayBillingManager> {
  private final Provider<Context> contextProvider;

  public PlayBillingManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public PlayBillingManager get() {
    return newInstance(contextProvider.get());
  }

  public static PlayBillingManager_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new PlayBillingManager_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static PlayBillingManager_Factory create(Provider<Context> contextProvider) {
    return new PlayBillingManager_Factory(contextProvider);
  }

  public static PlayBillingManager newInstance(Context context) {
    return new PlayBillingManager(context);
  }
}
