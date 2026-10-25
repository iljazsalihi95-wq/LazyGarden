plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "com.lazygarden.game"
    compileSdk = 35
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    defaultConfig { applicationId = "com.lazygarden.game"; minSdk = 24; targetSdk = 35; versionCode = 3; versionName = "0.3.0" }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
