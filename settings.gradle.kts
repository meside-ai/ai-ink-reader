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
        // Repository for epublib
        maven {
            url = uri("https://github.com/psiegman/mvn-repo/raw/master/releases")
        }
        // Fallback repositories
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "ink-agent"
include(":app") 