enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "prismatic"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://repo.papermc.io/repository/maven-public/") {
            name = "papermc-repo-releases"
            mavenContent { releasesOnly() }
        }
        maven(url = "https://repo.papermc.io/repository/maven-snapshots/") {
            name = "papermc-repo-snapshots"
            mavenContent { snapshotsOnly() }
        }
        maven(url = "https://repo.hboyd.dev/releases/") {
            name = "hboyd-dev-repo-releases"
            mavenContent { releasesOnly() }
        }
        maven(url = "https://repo.hboyd.dev/snapshots/") {
            name = "hboyd-dev-repo-snapshots"
            mavenContent { snapshotsOnly() }
        }
        maven(url = "https://repo.codemc.io/repository/maven-releases/") {
            name = "codemc-releases"
            mavenContent { releasesOnly() }
        }
        maven(url = "https://repo.codemc.io/repository/maven-snapshots/") {
            name = "codemc-snapshots"
            mavenContent { snapshotsOnly() }
        }
        gradlePluginPortal()
    }
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://repo.papermc.io/repository/maven-public/") {
            name = "papermc-repo-releases"
            mavenContent { releasesOnly() }
        }
        maven(url = "https://repo.papermc.io/repository/maven-snapshots/") {
            name = "papermc-repo-snapshots"
            mavenContent { snapshotsOnly() }
        }
        maven(url = "https://repo.hboyd.dev/releases/") {
            name = "hboyd-dev-repo-releases"
            mavenContent { releasesOnly() }
        }
        maven(url = "https://repo.hboyd.dev/snapshots/") {
            name = "hboyd-dev-repo-snapshots"
            mavenContent { snapshotsOnly() }
        }
        gradlePluginPortal()
    }
}

sequenceOf(
    "lib",
    "paper"
).forEach {
    include("${rootProject.name}-$it")
    project(":${rootProject.name}-$it").projectDir = file(it)
}