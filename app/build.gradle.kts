plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "com.lazygarden.game"
    compileSdk = 35
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"; applicationId = "com.lazygarden.game"; minSdk = 24; targetSdk = 35; versionCode = 10; versionName = "0.9.0" }
}

dependencies {
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("junit:junit:4.13.2")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
