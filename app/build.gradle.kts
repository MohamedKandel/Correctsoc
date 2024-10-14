plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinParcelize)
}

android {
    namespace = "com.correct.correctsoc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.correct.correctsoc"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        buildFeatures {
            viewBinding = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.activity)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // page indicator
    implementation(libs.dotsindicator)

    // navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.converter.scalars)
    implementation (libs.okhttp)
    implementation (libs.logging.interceptor)

    // viewModel and lifecycle
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    //coroutines
    implementation(libs.kotlinx.coroutines.android)

    // ssp and sdp
    implementation(libs.ssp.android)
    implementation(libs.sdp.android)

    // shared prefs
    implementation(libs.androidx.preference.ktx)

    // get connected device
    implementation (libs.androidnetworktools)
    implementation (libs.androidwifitools)

    // in app update
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)

    // room db
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // payment
    implementation(libs.google.pay)
    implementation(libs.data.collector)

    // gif
    implementation(libs.android.gif.drawable)

    // internet connection
    implementation(libs.network.connetivity)

    // google sign in
    implementation(libs.play.services.auth)
    //implementation(libs.play.services.auth.api.phone)
}