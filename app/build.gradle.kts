plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    signingConfigs.getByName("debug") {
        storeFile = rootProject.file("ci/lazygarden-debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
    }
    buildFeatures { buildConfig = true }
    defaultConfig {
        val adApp = providers.gradleProperty("admobAppId").orNull
        manifestPlaceholders["admobAppId"] = adApp ?: "ca-app-pub-3940256099942544~3347511713"
        buildConfigField("boolean", "ADMOB_CONFIGURED", (adApp != null).toString())
        buildConfigField("boolean", "ADS_TEST_MODE", providers.gradleProperty("adsTestMode").orElse("false").get())
    }
    namespace = "com.lazygarden.game"
    compileSdk = 35
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"; applicationId = "com.lazygarden.game"; minSdk = 24; targetSdk = 35; versionCode = 13; versionName = "0.11.1" }
}

dependencies {
    implementation("com.unity3d.ads:unity-ads:4.16.6")
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("junit:junit:4.13.2")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
