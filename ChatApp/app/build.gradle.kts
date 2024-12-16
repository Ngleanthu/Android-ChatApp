plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Google Services plugin
}

android {
    namespace = "com.example.chatapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.chatapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
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

    buildFeatures {
        viewBinding = true
    }

    buildToolsVersion = "35.0.0"


    packaging { resources.excludes.add("META-INF/*") }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)


    // implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
    implementation("com.google.firebase:firebase-firestore")

    implementation("com.google.firebase:firebase-auth:23.1.0")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.android.gms:play-services-auth:20.6.0")

    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")

    implementation("androidx.multidex:multidex:2.0.1")

    // UI Libraries
    implementation("com.intuit.sdp:sdp-android:1.0.6")
    implementation("com.intuit.ssp:ssp-android:1.0.6")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation ("com.google.firebase:firebase-messaging:23.2.0' // Check for the latest version")


    // http client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")

    // load image from url
    implementation ("com.github.bumptech.glide:glide:4.11.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.11.0")
    //
    implementation ("androidx.activity:activity-ktx:1.2.0")

    implementation("androidx.emoji2:emoji2-emojipicker:1.5.0")



    // passwordless sign-in
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

    // Also add the dependency for the Google Play services library and specify its version
    implementation("com.google.android.gms:play-services-auth:21.3.0")


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //search algolia
    implementation("com.algolia:algoliasearch-android:3.27.0")



    // Thư viện ExoPlayer cơ bản
    implementation ("androidx.media3:media3-exoplayer:1.3.1")
    implementation ("androidx.media3:media3-exoplayer-dash:1.3.1")
    implementation ("androidx.media3:media3-ui:1.3.1")
}


// Apply the Google Services plugin automatically (no need for apply plugin in Kotlin Script)
