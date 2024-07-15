plugins {
    alias(libs.plugins.android.application)
}

android {

    namespace = "com.example.jarvisapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.jarvisapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        // Add more exclusions or inclusions here if needed
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation("com.karumi:dexter:6.2.3")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.29")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.github.webzatec:AI_Webza_Tec:1.0")

    implementation("com.google.api-client:google-api-client-android:1.32.1")
    implementation("com.google.api-client:google-api-client-gson:1.32.1")
    implementation("com.google.apis:google-api-services-youtube:v3-rev222-1.25.0")

    implementation("com.google.api-client:google-api-client-android:1.32.2")
    implementation("com.google.api-client:google-api-client-gson:1.32.2")
    implementation("com.google.apis:google-api-services-youtube:v3-rev222-1.25.0")


    implementation("com.google.api-client:google-api-client:1.32.2")
    implementation("com.google.api-client:google-api-client-gson:1.32.2")
    implementation("com.google.apis:google-api-services-youtube:v3-rev222-1.25.0")
    implementation("com.google.http-client:google-http-client-android:1.38.1")


    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.gms:play-services-location:21.0.1")


    // gemni api
    // add the dependency for the Google AI client SDK for Android
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
    // Required for one-shot operations (to use ListenableFuture from Guava Android)
    implementation("com.google.guava:guava:31.0.1-android")
    // Required for streaming operations (to use Publisher from Reactive Streams)
    implementation("org.reactivestreams:reactive-streams:1.0.4")


    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}