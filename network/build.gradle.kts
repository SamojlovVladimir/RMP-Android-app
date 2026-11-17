plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ru.samoylov.matule.network"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)

    // Реализация сетевого слоя. Скрыта за интерфейсом MatuleApiService,
    // поэтому зависимость implementation, а не api: приложение
    // не получает доступ к классам Retrofit напрямую.
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    api(libs.gson)
}
