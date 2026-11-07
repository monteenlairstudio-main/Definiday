plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Release signing: values come from ~/.gradle/gradle.properties or environment variables, never from the repo.
fun signingValue(name: String): String? = providers.gradleProperty(name).orElse(providers.environmentVariable(name)).orNull?.takeIf { it.isNotBlank() }
val releaseKeystore = signingValue("DEFINIDAY_KEYSTORE")?.let { file(it) }
val releaseKeystorePassword = signingValue("DEFINIDAY_KEYSTORE_PASSWORD")
if (releaseKeystore != null) {
    require(releaseKeystore.isFile) { "DEFINIDAY_KEYSTORE points to a missing file: $releaseKeystore" }
    requireNotNull(releaseKeystorePassword) { "DEFINIDAY_KEYSTORE is set but DEFINIDAY_KEYSTORE_PASSWORD is not" }
} else {
    logger.warn("Definiday: DEFINIDAY_KEYSTORE not set, release APK signed with the debug key")
}

android {
    namespace = "fr.definiday"
    compileSdk = 36
    defaultConfig {
        applicationId = "fr.definiday"
        minSdk = 26
        targetSdk = 36
        versionCode = 14
        versionName = "0.14.0"
        // in-app update from the GitHub releases (Update.kt); a Google Play build must set it false (docs/mise_a_jour.md)
        buildConfigField("boolean", "SELF_UPDATE", "true")
    }
    signingConfigs {
        if (releaseKeystore != null) create("release") {
            storeFile = releaseKeystore
            storePassword = releaseKeystorePassword
            keyAlias = signingValue("DEFINIDAY_KEY_ALIAS") ?: "definiday"
            keyPassword = signingValue("DEFINIDAY_KEY_PASSWORD") ?: releaseKeystorePassword // PKCS12: key password = store password
        }
    }
    // Debug-signed builds (debug, and release without the keystore: CI branches and PRs) are « Definiday (test) », fr.definiday.dev:
    // they install beside the real app, never over it. Over it, Android would demand an uninstall, which wipes the profile (docs/profil.md).
    sourceSets["debug"].res.srcDir("src/dev/res")
    if (releaseKeystore == null) sourceSets["release"].res.srcDir("src/dev/res")
    buildTypes {
        debug { applicationIdSuffix = ".dev" }
        release {
            if (releaseKeystore == null) applicationIdSuffix = ".dev"
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }
    buildFeatures.buildConfig = true
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions.unitTests.isIncludeAndroidResources = true
    testOptions.unitTests.all { it.jvmArgs("--add-opens=java.base/java.io=ALL-UNNAMED", "--add-exports=java.base/jdk.internal.access=ALL-UNNAMED") } // Robolectric on JDK 21
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.17")
}
