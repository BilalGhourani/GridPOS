import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("/Users/bilal/Documents/development/freelance/Grids Android/GridKeystore.keystore")
            storePassword = "Grids123"
            keyAlias = "GridsPOS"
            keyPassword = "Grids123"
        }
    }
    namespace = "com.grid.pos"


    compileSdk = 34
    defaultConfig {
        applicationId = "com.grid.pos"
        minSdk = 26
        targetSdk = 34
        versionCode = 13
        versionName = "1.0.1"
        archivesName = "grid_pos_release(${versionName}(${versionCode}))"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}
configurations {
    all {
        exclude(
            group = "stax",
            module = "stax-api"
        )
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.documentfile)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)


    implementation("androidx.compose.material:material-icons-core:1.7.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")

    //Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.9.2")

    //Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.6.1")

    //firebase
    implementation("com.google.firebase:firebase-crashlytics-buildtools:2.9.9")
    implementation("com.google.firebase:firebase-firestore:24.11.1")

    // Glide images
    implementation(libs.compose.glide)
    implementation("io.coil-kt:coil-compose:2.6.0")

    //gson
    implementation("com.google.code.gson:gson:2.10.1")

    //jetPack DataStore
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("androidx.datastore:datastore-preferences-rxjava2:1.1.1")
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.1.1")
    implementation("net.sourceforge.jtds:jtds:1.3.1")

    // For working with .xlsx files
    implementation("org.apache.poi:poi:5.2.3") // Update to the latest version if necessary
    implementation("org.apache.poi:poi-ooxml:5.2.3") {
        exclude(
            group = "org.apache.poi",
            module = "poi-ooxml-lite"
        )
    }
    implementation("org.apache.poi:poi-ooxml:5.2.3") // For XML schemas
    implementation("org.apache.xmlbeans:xmlbeans:5.2.0") // For XML processing
    implementation("org.apache.commons:commons-collections4:4.4") // Commons collections

    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // CameraX dependencies
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    // generate the barcode image
    implementation ("com.google.zxing:core:3.4.1")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")

    // parse html contents
    implementation ("org.jsoup:jsoup:1.15.3")


}