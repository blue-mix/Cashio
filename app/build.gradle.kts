plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.realm.kotlin)
}

android {
    namespace = "com.bluemix.cashio"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bluemix.cashio"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("CASHIO_KEYSTORE")
            val storePwd = System.getenv("CASHIO_STORE_PASSWORD")
            val keyAlias = System.getenv("CASHIO_KEY_ALIAS")
            val keyPwd = System.getenv("CASHIO_KEY_PASSWORD")

            if (!keystorePath.isNullOrEmpty() && !storePwd.isNullOrEmpty() && !keyAlias.isNullOrEmpty() && !keyPwd.isNullOrEmpty()) {
                storeFile = file(keystorePath)
                storePassword = storePwd
                this.keyAlias = keyAlias
                keyPassword = keyPwd
            } else {
                println("⚠️ Release signing keys not found in Env Vars. Skipping signing config.")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            if (signingConfigs.getByName("release").storeFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- Core ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

    // --- Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Extended Icons (Required for Category icons)
    implementation(libs.androidx.compose.material.icons.extended)

    // --- Navigation ---
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // --- DataStore ---
    implementation(libs.androidx.datastore.preferences)

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // --- Database (Realm) ---
    implementation(libs.realm.kotlin)

    // --- DI (Koin) ---
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    // --- UI Libs ---
    implementation(libs.ehsannarmani.compose.charts)
    implementation("com.kizitonwose.calendar:compose:2.6.0")

    // --- Desugaring ---
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    // --- Testing ---
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}