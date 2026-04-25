import com.github.triplet.gradle.androidpublisher.ReleaseStatus
import java.util.Base64
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.play.publisher)
}

val ciVersionCode = System.getenv("VERSION_CODE")?.toIntOrNull()
val ciVersionName = System.getenv("VERSION_NAME")
val googleServicesJsonFile = layout.projectDirectory.file("google-services.json").asFile
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}
val googleServicesJsonBase64 = System.getenv("GOOGLE_SERVICES_JSON_BASE64")
    ?: localProperties.getProperty("GOOGLE_SERVICES_JSON_BASE64")

val restoreGoogleServicesJson by tasks.registering {
    group = "firebase"
    description = "Restores google-services.json from GOOGLE_SERVICES_JSON_BASE64 env/local.properties."

    doLast {
        if (googleServicesJsonFile.exists()) {
            return@doLast
        }

        if (googleServicesJsonBase64.isNullOrBlank()) {
            throw GradleException(
                "google-services.json is missing. Add app/google-services.json manually " +
                    "or provide GOOGLE_SERVICES_JSON_BASE64 in environment/local.properties."
            )
        }

        val decoded = Base64.getDecoder().decode(googleServicesJsonBase64)
        googleServicesJsonFile.writeBytes(decoded)
    }
}

android {
    namespace = "com.afquintana.weightcontroller"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.afquintana.weightcontroller"
        minSdk = 26
        targetSdk = 36
        versionCode = ciVersionCode ?: 4
        versionName = ciVersionName ?: "1.0.4"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
            val keyAlias = System.getenv("KEY_ALIAS")
            val keyPassword = System.getenv("KEY_PASSWORD")

            if (!keystorePath.isNullOrBlank()) {
                storeFile = file(keystorePath)
            }
            storePassword = keystorePassword
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (System.getenv("CI") == "true") {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            // applicationIdSuffix = ".debug"
            // versionNameSuffix = "-debug"
        }
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
}

tasks.named("preBuild") {
    dependsOn(restoreGoogleServicesJson)
}

play {
    track.set(System.getenv("PLAY_TRACK") ?: "internal")
    releaseStatus.set(ReleaseStatus.COMPLETED)
}

kotlin {
    jvmToolchain(21)
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.google.material)

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.ihsanbal.logging.interceptor)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}
