// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20" apply false
}

// Repositories are configured in settings.gradle.kts

// Common versions for all modules
extra["compileSdk"] = 35
extra["minSdk"] = 29
extra["targetSdk"] = 35
extra["kotlinVersion"] = "1.9.20"
extra["coroutinesVersion"] = "1.7.3"
extra["lifecycleVersion"] = "2.7.0"
extra["composeVersion"] = "1.5.8"
extra["accompanistVersion"] = "0.32.0"