import Versions.googleServicesVersion
import Versions.navigationVersion
import Versions.roomVersion
import Versions.spotlessVersion

object Versions {
    const val navigationVersion = "2.5.1"
    const val spotlessVersion = "6.3.0"
    const val googleServicesVersion = "4.3.10"
    const val roomVersion = "2.4.1"
}

object Libs {

    object ConfigData {
        const val ktlint = "0.44.0"
        const val minSdk = 28
        const val compileSdk = 33
        const val versionMajor = 1
        const val versionMinor = 0
        const val versionPatch = 0
        const val buildToolsVersion = "30.0.3"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:7.1.2"
    const val androidGradleVersionPlugin = "com.github.ben-manes:gradle-versions-plugin:0.42.0"
    const val spotlessPlugin = "com.diffplug.spotless:spotless-plugin-gradle::$spotlessVersion"
    const val googleServices = "com.google.gms:google-services:$googleServicesVersion"
    const val safeArgsPlugin =
        "androidx.navigation:navigation-safe-args-gradle-plugin:$navigationVersion"


    object DependencyInjection {

        private const val daggerCoreHiltVersion = "2.42"
        private const val daggerHiltVersion = "1.0.0"


        const val hiltAndroidPlugin =
            "com.google.dagger:hilt-android-gradle-plugin:$daggerCoreHiltVersion"
        const val hiltAndroid = "com.google.dagger:hilt-android:$daggerCoreHiltVersion"
        const val hiltAndroidCompiler =
            "com.google.dagger:hilt-android-compiler:$daggerCoreHiltVersion"
        const val hiltAndroidTesting =
            "com.google.dagger:hilt-android-testing:$daggerCoreHiltVersion"
        const val hiltCompiler = "com.google.dagger:hilt-compiler:$daggerCoreHiltVersion"
        const val hiltCommon = "androidx.hilt:hilt-common:$daggerHiltVersion"
        const val androidXHiltCompiler = "androidx.hilt:hilt-compiler:$daggerHiltVersion"
        const val hiltNavigation = "androidx.hilt:hilt-navigation-compose:$daggerHiltVersion"
        const val hiltWorker = "androidx.hilt:hilt-work:$daggerHiltVersion"

    }

    object ArchitectureComponents {

        private const val fragmentVersion = "1.3.4"
        private const val coreVersion = "1.7.0"
        private const val appCompatVersion = "1.4.1"
        private const val constraintVersion = "2.1.3"
        private const val lifecycleVersion = "2.4.1"
        private const val lifecycleExtVersion = "2.2.0"

        private const val startupVersion = "1.1.1"


        private const val workVersion = "2.7.1"


        const val fragment = "androidx.fragment:fragment-ktx:$fragmentVersion"
        const val coreKtx = "androidx.core:core-ktx:$coreVersion"
        const val appCompat = "androidx.appcompat:appcompat:$appCompatVersion"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:$constraintVersion"
        const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion"
        const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion"
        const val viewModelSavedState =
            "androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion"
        const val lifecycleExtensions =
            "androidx.lifecycle:lifecycle-extensions:$lifecycleExtVersion"
        const val startupRuntime = "androidx.startup:startup-runtime:$startupVersion"
        const val navigationFragment =
            "androidx.navigation:navigation-fragment-ktx:$navigationVersion"
        const val navigationUI = "androidx.navigation:navigation-ui-ktx:$navigationVersion"

        const val roomRuntime = "androidx.room:room-runtime:$roomVersion"
        const val roomCompiler = "androidx.room:room-compiler:$roomVersion"
        const val room = "androidx.room:room-ktx:$roomVersion"

        const val workManager = "androidx.work:work-runtime:$workVersion"
        const val workManagerKtx = "androidx.work:work-runtime-ktx:$workVersion"

    }

    object Firebase {

        private const val firebaseBomVersion = "28.0.1"

        const val firebaseBom = "com.google.firebase:firebase-bom:$firebaseBomVersion"
        const val firebaseMessaging = "com.google.firebase:firebase-messaging-ktx"
        const val firebaseAnalytics = "com.google.firebase:firebase-analytics-ktx"
        const val firebaseDatabase = "com.google.firebase:firebase-database-ktx"
    }


    object Libraries {
        private const val coroutinesVersion = "1.6.0"
        private const val glideVersion = "4.12.0"
        private const val legacyVersion = "1.0.0"
        private const val viewPagerVersion = "1.0.0"
        private const val materialVersion = "1.6.1"
        private const val coordinatorVersion = "1.2.0"
        private const val exoPlayerVersion = "2.17.0"
        private const val imagePickerVersion = "1.8"
        private const val inlineActivityVersion = "1.0.4"
        private const val supportDesignVersion = "28.0.0"
        private const val swipeRevealLayoutVersion = "1.4.1"


        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"
        const val glide = "com.github.bumptech.glide:glide:$glideVersion"
        const val glideCompiler = "com.github.bumptech.glide:compiler:$glideVersion"
        const val legacySupport = "androidx.legacy:legacy-support-v4:$legacyVersion"
        const val viewPager = "androidx.viewpager2:viewpager2:$viewPagerVersion"
        const val material = "com.google.android.material:material:$materialVersion"
        const val coordinatorLayout =
            "androidx.coordinatorlayout:coordinatorlayout:$coordinatorVersion"
        const val exoPlayerCore = "com.google.android.exoplayer:exoplayer-core:$exoPlayerVersion"
        const val exoPlayerHls = "com.google.android.exoplayer:exoplayer-hls:$exoPlayerVersion"
        const val exoPlayerUI = "com.google.android.exoplayer:exoplayer-ui:$exoPlayerVersion"
        const val imagePicker = "com.github.dhaval2404:imagepicker:$imagePickerVersion"
        const val inlineActivity =
            "com.github.florent37:inline-activity-result-kotlin:$inlineActivityVersion"
        const val swipeRefreshLayout =
            "com.chauthai.swipereveallayout:swipe-reveal-layout:$swipeRevealLayoutVersion"
        const val facebook = "com.facebook.android:facebook-android-sdk:latest.release"


    }

    object NordicBLE {
        private const val dfuVersion = "2.2.0"
        private const val nordicBLEVersion = "2.5.1"
        private const val nordicBLEScanner = "1.6.0"

        const val dfu = "no.nordicsemi.android:dfu:$dfuVersion"
        const val bleKtx = "no.nordicsemi.android:ble-ktx:$nordicBLEVersion"
        const val bleLiveData = "no.nordicsemi.android:ble-livedata:$nordicBLEVersion"
        const val bleScanner = "no.nordicsemi.android.support.v18:scanner:$nordicBLEScanner"
    }

    object PlayServices {
        private const val playServicesAuthVersion = "20.1.0"
        private const val playFitnessAuthVersion = "21.0.1"

        const val playServicesAuth =
            "com.google.android.gms:play-services-auth:$playServicesAuthVersion"
        const val playServicesFitness =
            "com.google.android.gms:play-services-fitness:$playFitnessAuthVersion"
    }

    object Logging {

        private const val timberVersion = "4.7.1"
        private const val sentryVersion = "5.6.0"
        private const val leakcanaryVersion = "2.7"

        const val timber = "com.jakewharton.timber:timber:$timberVersion"
        const val sentry = "io.sentry:sentry-android:$sentryVersion"
        const val sentryTimber = "io.sentry:sentry-android-timber:$sentryVersion"
        const val leakcanary = "com.squareup.leakcanary:leakcanary-android:$leakcanaryVersion"
    }

    object Networking {

        private const val retrofitVersion = "2.9.0"
        private const val okHttpLogVersion = "4.9.1"
        private const val moshiVersion = "1.13.0"
        private const val sandwichVersion = "1.1.0"

        const val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
        const val moshiConverter = "com.squareup.retrofit2:converter-moshi:$retrofitVersion"
        const val okHttpLogging = "com.squareup.okhttp3:logging-interceptor:$okHttpLogVersion"
        const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:$moshiVersion"
        const val moshiAdapter = "com.squareup.moshi:moshi-adapters:$moshiVersion"
        const val moshiCodegen = "com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion"
        const val sandwichApiResponseWrapper = "com.github.skydoves:sandwich:$sandwichVersion"

    }

    object Accompanist {

        private const val version = "0.23.1"
        const val insets = "com.google.accompanist:accompanist-insets:$version"
        const val systemuicontroller =
            "com.google.accompanist:accompanist-systemuicontroller:$version"
        const val flowlayouts = "com.google.accompanist:accompanist-flowlayout:$version"

        const val pager = "com.google.accompanist:accompanist-pager:$version"
        const val pagerindicators = "com.google.accompanist:accompanist-pager-indicators:$version"

        const val swiperefresh = "com.google.accompanist:accompanist-swiperefresh:$version"

        //const val webview = "com.google.accompanist:accompanist-webview:0.26.2-beta"
    }

    object Kotlin {

        private const val version = "1.6.10"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"
    }


    object AndroidX {

        const val coreKtx = "androidx.core:core-ktx:1.7.0"

        object Compose {

            const val snapshot = ""
             const val version = "1.2.1"
            private const val versionConstraintLayout = "1.0.0"
            private const val material3Version = "1.0.0-alpha09"
            private const val accompanistVersion = "v0.24.12-rc"

            const val foundation = "androidx.compose.foundation:foundation:$version"
            const val layout = "androidx.compose.foundation:foundation-layout:$version"
            const val ui = "androidx.compose.ui:ui:$version"
            const val uiUtil = "androidx.compose.ui:ui-util:$version"
            const val runtime = "androidx.compose.runtime:runtime:$version"
            const val material = "androidx.compose.material:material:$version"
            const val animation = "androidx.compose.animation:animation:$version"
            const val tooling = "androidx.compose.ui:ui-tooling:$version"
            const val toolingPreview = "androidx.compose.ui:ui-tooling-preview:$version"
            const val iconsExtended = "androidx.compose.material:material-icons-extended:$version"

            const val uiTest = "androidx.compose.ui:ui-test-junit4:$version"


            const val material3 = "androidx.compose.material3:material3:$material3Version"
            const val constraintLayoutCompose =
                "androidx.constraintlayout:constraintlayout-compose:$versionConstraintLayout"

//
//            const val accompanist = "com.google.accompanist:$accompanistVersion"
        }

        object Media3 {

            private const val version = "1.0.0-beta02"

            //https://developer.android.com/jetpack/androidx/releases/media3
            //https://android-developers.googleblog.com/2021/10/jetpack-media3.html
            //https://github.com/androidx/media/tree/main/demos/session
            //https://proandroiddev.com/video-playback-in-lazycolumn-in-jetpack-compose-df355097f26e
            //https://github.com/Skyyo/compose-video-playback
            const val exoPlayer = "androidx.media3:media3-exoplayer:$version"
            const val exoPlayerDash = "androidx.media3:media3-exoplayer-dash:$version"
            const val exoPlayerHLS = "androidx.media3:media3-exoplayer-hls:$version"
            const val exoPlayerRTSP = "androidx.media3:media3-exoplayer-rtsp:$version"
            const val exoPlayerMediaAds = "androidx.media3:media3-exoplayer-ima:$version"
            const val exoPlayerOkHttp = "androidx.media3:media3-datasource-okhttp:$version"
            const val exoPlayerPlaybackUI = "androidx.media3:media3-ui:$version"
            const val exoPlayerMediaSession = "androidx.media3:media3-session:$version"
            const val exoPlayerMediaContainerData = "androidx.media3:media3-extractor:$version"
            const val exoPlayerMediaDecoder = "androidx.media3:media3-decoder:$version"
            const val exoPlayerMediaLoader = "androidx.media3:media3-datasource:$version"
            const val exoPlayerMediaCommon = "androidx.media3:media3-common:$version"

        }


        object Activity {

            const val activityCompose = "androidx.activity:activity-compose:1.4.0"
        }

        object Lifecycle {

            const val viewModelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0-alpha02"
            const val lifecycleCompose = "androidx.lifecycle:lifecycle-runtime-compose:2.6.0-alpha02"
        }

        object Navigation {

            const val navigationCompose = "androidx.navigation:navigation-compose:2.4.1"
        }


//        object Test {
//
//            private const val version = "1.4.0"
//            private const val archComponentVersion = "2.1.0"
//            const val core = "androidx.test:core:$version"
//            const val runner = "androidx.test:runner:$version"
//            const val rules = "androidx.test:rules:$version"
//
//            object Ext {
//
//                private const val version = "1.1.2"
//                const val junit = "androidx.test.ext:junit-ktx:$version"
//            }
//
//            const val espressoCore = "androidx.test.espresso:espresso-core:3.2.0"
//
//
//            const val coreTesting = "androidx.arch.core:core-testing:$archComponentVersion"
//            const val navigationTest = "androidx.navigation:navigation-testing:$navigationVersion"
//        }
    }

    object Testing {

        private const val junitVersion = "4.13.2"
        private const val androidxTestJUnitVersion = "1.1.3"
        private const val espressoVersion = "3.4.0"
        private const val mockkVersion = "1.11.0"
        const val isBleMockEnabled = false
        private const val archComponentVersion = "2.1.0"


        const val junit = "junit:junit:$junitVersion"
        const val junitExt = "androidx.test.ext:junit:$androidxTestJUnitVersion"
        const val coreTesting = "androidx.arch.core:core-testing:$archComponentVersion"
        const val espressoCore = "androidx.test.espresso:espresso-core:$espressoVersion"
        const val mockkCommon = "io.mockk:mockk-common:$mockkVersion"
        const val mockk = "io.mockk:mockk:$mockkVersion"
        const val mockkAndroid = "io.mockk:mockk-android:$mockkVersion"
        const val roomTesting = "androidx.room:room-testing:$roomVersion"

    }

    object Coil {

        const val coilCompose = "io.coil-kt:coil-compose:2.1.0"
    }

}