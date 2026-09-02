plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    iosArm64()
    iosSimulatorArm64()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.coil3.compose)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.coil3.network.okhttp)
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.ui)
            // Downloader.android.kt 经 CommonApp.app 取 Context（与 app 既有做法一致）
            implementation(libs.common)
        }
        iosMain.dependencies {
            implementation(libs.coil3.network.ktor3)
            implementation(libs.ktor.client.darwin)
            implementation("io.github.alexzhirkevich:qrose:1.0.1")
        }
    }
}

android {
    namespace = "me.wcy.music.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
