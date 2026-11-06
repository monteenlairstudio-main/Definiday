---
category: gotcha
subject: local-gradle-maven-429
---

Container builds: plugin resolution hits Maven Central 429 (pluginManagement lacks the googleapis mirror present in dependencyResolutionManagement). Workaround without touching the repo: init script adding https://maven-central.storage-download.googleapis.com/maven2/ via beforeSettings to pluginManagement.repositories. No Android SDK preinstalled: install cmdline-tools to scratchpad and set ANDROID_HOME.
