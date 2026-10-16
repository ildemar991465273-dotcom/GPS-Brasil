import java.util.Properties
plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("org.jetbrains.kotlin.plugin.compose")
}
val local = Properties().apply { val f=rootProject.file("local.properties"); if(f.exists()) f.inputStream().use { load(it) } }
val key = local.getProperty("MAPS_API_KEY") ?: System.getenv("MAPS_API_KEY") ?: ""
android {
 namespace = "br.com.gpsbrasil"
 compileSdk = 35
 defaultConfig {
  applicationId = "br.com.gpsbrasil"
  minSdk = 26
  targetSdk = 35
  versionCode = 1
  versionName = "1.0"
  manifestPlaceholders["MAPS_API_KEY"] = key
 }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 buildFeatures { compose=true }
}
dependencies {
 implementation(platform("androidx.compose:compose-bom:2024.12.01"))
 implementation("androidx.activity:activity-compose:1.9.3")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.foundation:foundation")
 implementation("androidx.compose.ui:ui")
 implementation("com.google.maps.android:maps-compose:6.4.1")
 implementation("com.google.android.gms:play-services-location:21.3.0")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}