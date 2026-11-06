pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositories {
        google()
        maven("https://maven-central.storage-download.googleapis.com/maven2/") // Maven Central mirror, avoids 429s
        mavenCentral()
    }
}
rootProject.name = "Definiday"
include(":app")
