plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.doancuoiki_dotcuoi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.doancuoiki_dotcuoi"
        minSdk = 26
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation ("com.vanniktech:emoji:0.9.0")
    implementation ("com.vanniktech:emoji-google:0.9.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("org.osmdroid:osmdroid-android:6.1.16")
    implementation ("com.google.android.libraries.places:places:3.4.0")
    implementation("com.cloudinary:cloudinary-android:2.3.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-auth:21.0.1")
    implementation("com.google.firebase:firebase-core:21.0.0")
    implementation("com.google.firebase:firebase-firestore:24.0.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-auth:20.6.0")
    implementation("androidx.credentials:credentials:1.0.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.0.0")
    implementation("com.google.firebase:firebase-storage:20.2.2")
    implementation ("com.google.android.libraries.places:places:3.3.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.navigation.fragment)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.play.services.auth)
    implementation(libs.recyclerview)
    implementation(libs.glide)

}
