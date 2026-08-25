plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.linger.app"
    compileSdk = 35

    val debugApiBaseUrl =
        (project.findProperty("PINGLET_API_BASE_URL_DEBUG") as String?)
            ?: System.getenv("PINGLET_API_BASE_URL_DEBUG")
            ?: (project.findProperty("LINGER_API_BASE_URL_DEBUG") as String?)
            ?: System.getenv("LINGER_API_BASE_URL_DEBUG")
            ?: "http://10.0.2.2:3000"
    val releaseApiBaseUrl =
        (project.findProperty("PINGLET_API_BASE_URL_RELEASE") as String?)
            ?: System.getenv("PINGLET_API_BASE_URL_RELEASE")
            ?: (project.findProperty("PINGLET_API_BASE_URL") as String?)
            ?: System.getenv("PINGLET_API_BASE_URL")
            ?: (project.findProperty("LINGER_API_BASE_URL_RELEASE") as String?)
            ?: System.getenv("LINGER_API_BASE_URL_RELEASE")
            ?: (project.findProperty("LINGER_API_BASE_URL") as String?)
            ?: System.getenv("LINGER_API_BASE_URL")
            ?: "https://api.pinglet.ai"

    fun escapeForBuildConfig(value: String): String =
        "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

    fun signingSecret(name: String): String? =
        (project.findProperty(name) as String?)
            ?: System.getenv(name)

    val uploadStoreFile = signingSecret("PINGLET_UPLOAD_STORE_FILE")
    val uploadStorePassword = signingSecret("PINGLET_UPLOAD_STORE_PASSWORD")
    val uploadKeyAlias = signingSecret("PINGLET_UPLOAD_KEY_ALIAS")
    val uploadKeyPassword = signingSecret("PINGLET_UPLOAD_KEY_PASSWORD")
    val releaseSigningConfigured = listOf(
        uploadStoreFile,
        uploadStorePassword,
        uploadKeyAlias,
        uploadKeyPassword,
    ).all { !it.isNullOrBlank() }

    signingConfigs {
        create("release") {
            if (releaseSigningConfigured) {
                storeFile = file(uploadStoreFile!!)
                storePassword = uploadStorePassword
                keyAlias = uploadKeyAlias
                keyPassword = uploadKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "ai.pinglet.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.1.1"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", escapeForBuildConfig(debugApiBaseUrl))
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", escapeForBuildConfig(releaseApiBaseUrl))
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    tasks.matching { it.name == "bundleRelease" || it.name == "assembleRelease" }.configureEach {
        doFirst {
            check(releaseSigningConfigured) {
                "Release signing is not configured. Set the PINGLET_UPLOAD_* properties in ~/.gradle/gradle.properties."
            }
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.android.billingclient:billing:9.1.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("androidx.room:room-ktx:2.7.0")
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.datastore:datastore-preferences:1.1.4")
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    implementation("com.google.dagger:hilt-android:2.55")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    kapt("com.google.dagger:hilt-android-compiler:2.55")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    ksp("androidx.room:room-compiler:2.7.0")

    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")

    testImplementation("junit:junit:4.13.2")
}
