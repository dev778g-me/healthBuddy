import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Secrets live in local.properties (gitignored), never in this file.
val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "org.dev.healthbuddy"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "org.dev.healthbuddy"
        minSdk = 31
        targetSdk = 37
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Groq API key — set GROQ_API_KEY=... in local.properties and rebuild.
        // The build fails empty (AI scanning turns off at runtime, nothing crashes).
        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"${localProps.getProperty("GROQ_API_KEY", "")}\""
        )
    }

    // ./gradlew :app:assembleRelease signs automatically once the four
    // keystore.* lines are present in local.properties.
    if (localProps.containsKey("keystore.file")) {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(localProps.getProperty("keystore.file"))
                storePassword = localProps.getProperty("keystore.password")
                keyAlias = localProps.getProperty("keystore.alias")
                keyPassword = localProps.getProperty("keystore.password")
            }
        }
    }

    buildTypes {
        debug {
            // Temporary: validate that R8 shrinking does not break OCR/AI.
            // Remove this block once verified.
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            // Classic R8 shrinking. The experimental AGP 9 `optimization`
            // block with packageScope merged Kotlin collections classes into
            // something inaccessible and crashed every launch on real devices.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (localProps.containsKey("keystore.file")) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    // Material 3 Expressive (stable in 1.4.0)
    implementation(libs.androidx.compose.material3.expressive)
    // Lucide outline icons — the only icon family used in the UI.
    implementation(libs.composables.icons.lucide)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // ML Kit on-device OCR
    implementation(libs.mlkit.text.recognition)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Groq AI library (optional, for prescription OCR enhancement)
    implementation(libs.groqKotlin)
}