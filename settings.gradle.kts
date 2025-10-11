pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "AgentZeroMobile"

include(":app")
include(":core")
include(":agents")
include(":sensors")
include(":voice")
include(":knowledge")
include(":security")
include(":briar-bridge")
include(":commons")