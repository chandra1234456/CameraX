plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.chandra.practicefunctionalities"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.chandra.practicefunctionalities"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    viewBinding {
        enable = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt") ,
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
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    /*implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")*/
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

// CameraX core library
    implementation("androidx.camera:camera-core:1.4.0-alpha03")

// CameraX Camera2 extensions
    implementation("androidx.camera:camera-camera2:1.4.0-alpha03")

// CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:1.4.0-alpha03")

// CameraX View class
    implementation("androidx.camera:camera-view:1.4.0-alpha03")
    implementation("com.github.bumptech.glide:glide:4.16.0")


}