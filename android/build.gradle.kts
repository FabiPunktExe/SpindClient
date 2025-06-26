plugins {
    id("com.android.application") version "8.11.0"
}

android {
    namespace = group as String
    compileSdk = 36

    defaultConfig {
        applicationId = group as String
        minSdk = 32
        targetSdk = 36
        versionCode = version.toString().replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
        versionName = version as String
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes += "META-INF/kotlin-stdlib-common.kotlin_module"
        resources.excludes += "META-INF/kotlin-stdlib-jdk7.kotlin_module"
        resources.excludes += "META-INF/kotlin-stdlib-jdk8.kotlin_module"
        resources.excludes += "META-INF/kotlin-stdlib.kotlin_module"
        resources.excludes += "kotlin/annotation/annotation.kotlin_builtins"
        resources.excludes += "kotlin/collections/collections.kotlin_builtins"
        resources.excludes += "kotlin/coroutines/coroutines.kotlin_builtins"
        resources.excludes += "kotlin/internal/internal.kotlin_builtins"
        resources.excludes += "kotlin/kotlin.kotlin_builtins"
        resources.excludes += "kotlin/ranges/ranges.kotlin_builtins"
        resources.excludes += "kotlin/reflect/reflect.kotlin_builtins"
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("commons-io:commons-io:2.19.0")
    implementation(project(":common"))
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.06.01"))
    implementation("androidx.compose.ui:ui:1.8.3")
    runtimeOnly("com.squareup.okhttp3:okhttp:4.12.0")
}
