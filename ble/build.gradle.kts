plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}
apply(from = "../spotless.gradle")
android {
    namespace = "com.acres.ble"
    compileSdk = Libs.ConfigData.compileSdk

    defaultConfig {
        minSdk = Libs.ConfigData.minSdk
        targetSdk = Libs.ConfigData.compileSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    implementation(Libs.NordicBLE.dfu)
    implementation(Libs.NordicBLE.bleKtx)
    implementation(Libs.NordicBLE.bleScanner)
    implementation("androidx.core:core:1.9.0")
    implementation("androidx.activity:activity-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.5.1")

    // Testing.
    androidTestImplementation(Libs.DependencyInjection.hiltAndroidTesting)
    testImplementation(Libs.Testing.junit)
    androidTestImplementation(Libs.Testing.coreTesting)
    androidTestImplementation(Libs.Testing.junitExt)
    androidTestImplementation(Libs.Testing.espressoCore)
    testImplementation(Libs.Testing.mockkCommon)
    testImplementation(Libs.Testing.mockk)
    androidTestImplementation(Libs.Testing.mockkAndroid)
}
