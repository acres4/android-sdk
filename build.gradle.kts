plugins {
    id("com.android.application") version "7.3.0" apply false
    id("com.android.library") version "7.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.7.10" apply false
    id("com.diffplug.spotless") version "6.3.0"
}
apply(plugin = "com.github.ben-manes.versions")

buildscript {

    dependencies {
        classpath("com.github.ben-manes:gradle-versions-plugin:0.42.0")
        classpath(Libs.DependencyInjection.hiltAndroidPlugin)
    }
}

allprojects {
    apply(plugin = "com.github.ben-manes.versions")

    tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {

        resolutionStrategy {
            componentSelection {

                all {
                    if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
                        reject("Not Stable")
                    }

                }

            }
        }
    }

    tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates")
        .configure {
            checkConstraints = true
            checkBuildEnvironmentConstraints = true
            checkForGradleUpdate = true
            outputFormatter = "json"
            outputDir = "build/dependencyUpdates"
            reportfileName = "report"
        }
}


fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}