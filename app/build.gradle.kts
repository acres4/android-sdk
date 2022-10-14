


plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

apply(from = "../spotless.gradle")

extra["isKotlinDsl"] = true

fun generateVersionCode(): Int {
    return Libs.ConfigData.minSdk * 10000000 + Libs.ConfigData.versionMajor * 10000 + Libs.ConfigData.versionMinor * 100 + Libs.ConfigData.versionPatch
}

fun generateVersionName(): String {
    return "${Libs.ConfigData.versionMajor}.${Libs.ConfigData.versionMinor}.${Libs.ConfigData.versionPatch}"
}

android {
    namespace = "com.acres.blesdk"
    compileSdk = Libs.ConfigData.compileSdk

    defaultConfig {
        applicationId = "com.acres.blesdk"
        minSdk = Libs.ConfigData.minSdk
        targetSdk = Libs.ConfigData.compileSdk
        versionCode = generateVersionCode()
        versionName = generateVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.1"
    }
    packagingOptions {
        resources {
            excludes += listOf("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
    sourceSets {
        // Adds exported schema location as test app assets.

        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }
}

dependencies {
    implementation(project(":ble"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libs.DependencyInjection.hiltAndroid)
    kapt(Libs.DependencyInjection.hiltAndroidCompiler)
    implementation(Libs.DependencyInjection.hiltCommon)
    kapt(Libs.DependencyInjection.androidXHiltCompiler)
    implementation(Libs.DependencyInjection.hiltNavigation)

    implementation(Libs.ArchitectureComponents.fragment)
    implementation(Libs.ArchitectureComponents.coreKtx)
    implementation(Libs.ArchitectureComponents.appCompat)
    implementation(Libs.ArchitectureComponents.constraintLayout)
    implementation(Libs.ArchitectureComponents.liveData)
    implementation(Libs.ArchitectureComponents.viewModel)
    implementation(Libs.ArchitectureComponents.viewModelSavedState)
    implementation(Libs.ArchitectureComponents.lifecycleExtensions)
    implementation(Libs.ArchitectureComponents.startupRuntime)
    implementation(Libs.ArchitectureComponents.navigationFragment)
    implementation(Libs.ArchitectureComponents.navigationUI)

    implementation(Libs.Libraries.coroutines)
    implementation(Libs.Logging.timber)

    implementation(Libs.AndroidX.Lifecycle.viewModelCompose)
    implementation(Libs.AndroidX.Lifecycle.lifecycleCompose)

    implementation(Libs.AndroidX.Compose.ui)
    implementation(Libs.AndroidX.Compose.material)
    implementation(Libs.AndroidX.Compose.tooling)
    implementation(Libs.AndroidX.Compose.runtime)
    implementation(Libs.AndroidX.Compose.foundation)
    implementation(Libs.AndroidX.Compose.layout)
    implementation(Libs.AndroidX.Compose.uiUtil)
    implementation(Libs.AndroidX.Compose.animation)
    implementation(Libs.AndroidX.Compose.iconsExtended)
    implementation(Libs.AndroidX.Compose.material3)
    implementation(Libs.AndroidX.Compose.constraintLayoutCompose)

    implementation(Libs.AndroidX.Activity.activityCompose)
    implementation(Libs.AndroidX.Navigation.navigationCompose)

    implementation(Libs.Accompanist.systemuicontroller)
    implementation(Libs.Accompanist.flowlayouts)

    // Debug.
    debugImplementation(Libs.Logging.leakcanary)

    // Testing.
    testImplementation(Libs.Testing.junit)
    androidTestImplementation(Libs.Testing.coreTesting)
    androidTestImplementation(Libs.Testing.junitExt)

    androidTestImplementation(Libs.Testing.espressoCore)
    testImplementation(Libs.Testing.mockkCommon)
    testImplementation(Libs.Testing.mockk)
    androidTestImplementation(Libs.Testing.mockkAndroid)
}
