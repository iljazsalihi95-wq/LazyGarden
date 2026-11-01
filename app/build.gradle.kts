plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    signingConfigs.getByName("debug") {
        // Keep one stable signing identity for every installable CI build so Android can
        // install a new APK as an UPDATE instead of forcing an uninstall/data reset.
        storeFile = rootProject.file("ci/lazygarden-debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
    }
    buildFeatures { buildConfig = true }
    defaultConfig {
        val adApp = providers.gradleProperty("admobAppId").orNull ?: "ca-app-pub-9794339868092490~7051142351"
        val rewarded = providers.gradleProperty("admobRewardedId").orNull ?: "ca-app-pub-9794339868092490/1798815672"
        val banner = providers.gradleProperty("admobBannerId").orNull ?: "ca-app-pub-9794339868092490/5546488993"
        val interstitial = providers.gradleProperty("admobInterstitialId").orNull ?: "ca-app-pub-9794339868092490/6788960466"
        manifestPlaceholders["admobAppId"] = adApp
        buildConfigField("String", "ADMOB_REWARDED_ID", "\"$rewarded\"")
        buildConfigField("String", "ADMOB_BANNER_ID", "\"$banner\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$interstitial\"")
        buildConfigField("boolean", "ADMOB_CONFIGURED", "true")
        buildConfigField("boolean", "ADS_TEST_MODE", providers.gradleProperty("adsTestMode").orElse("false").get())
    }
    namespace = "com.lazygarden.game"
    compileSdk = 35
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        applicationId = "com.lazygarden.game"
        minSdk = 24
        targetSdk = 35
        // Every distributed build must move forward. Together with the stable package
        // and keystore above this preserves garden_progress during normal APK updates.
        versionCode = 14
        versionName = "0.12.0"
    }
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
