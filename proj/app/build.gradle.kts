buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.8.0")
    }
}
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("kotlinx-serialization")
}

android {
    namespace = "com.example.androidproject"
    compileSdk = 34

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        // exclude("META-INF/OTHER_DUPLICATE_FILE")
    }

    defaultConfig {
        applicationId = "com.example.androidproject"
        minSdk = 26
        targetSdk = 33
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
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion ="1.5.3"
    }
}


dependencies {
    implementation("androidx.compose.ui:ui-android:1.5.4")
    implementation("androidx.test.ext:junit-ktx:1.1.5")
    implementation("com.google.firebase:firebase-auth-ktx:22.2.0")
    implementation("com.google.android.engage:engage-core:1.3.1")
    debugImplementation("androidx.fragment:fragment-testing:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    debugImplementation ("androidx.fragment:fragment-testing:1.6.1")
    androidTestImplementation ("androidx.arch.core:core-testing:2.2.0")
//  light theme from the Material Components library with a dark action bar
    implementation ("com.google.android.material:material:1.10.0")

    implementation ("androidx.compose.ui:ui:1.5.4")
    implementation ("androidx.compose.material:material:1.5.4")
    implementation ("androidx.compose.foundation:foundation:1.5.4")
    implementation ("androidx.compose.runtime:runtime-livedata:1.5.4")

    // Room
    implementation("androidx.room:room-ktx:2.6.0")
    implementation("androidx.room:room-common:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")
    implementation ("androidx.room:room-runtime:2.6.0")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    androidTestImplementation ("androidx.navigation:navigation-testing:2.7.4")
    androidTestImplementation ("org.mockito:mockito-android:3.12.4")
    androidTestImplementation ("org.mockito.kotlin:mockito-kotlin:3.2.0")
    androidTestImplementation ("io.mockk:mockk:1.12.0")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:1.5.4")
    implementation ("io.mockk:mockk:1.12.0")
    testImplementation ("org.mockito:mockito-core:3.12.4")
    androidTestImplementation ("androidx.navigation:navigation-testing:2.7.4")

    androidTestImplementation ("androidx.room:room-testing:2.6.0")
    androidTestImplementation ("androidx.test:rules:1.5.0")
    androidTestImplementation ("androidx.test:runner:1.5.2")

    implementation(platform("com.google.firebase:firebase-bom:32.4.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")


    //Ktor
    implementation("io.ktor:ktor-client-core:2.3.5")
    implementation("io.ktor:ktor-client-apache:2.3.5")
//    implementation("io.ktor:ktor-client-content-jvm:2.3.5")
    implementation("io.ktor:ktor-client-serialization:2.3.5")
    implementation("io.ktor:ktor-client-android:2.3.5")
    implementation("io.ktor:ktor-client-serialization-jvm:2.3.5")
    implementation("io.ktor:ktor-client-logging:2.3.5")
    implementation("io.ktor:ktor-client-auth:2.3.5")
    implementation("io.ktor:ktor-client-android:2.3.5")
    implementation("io.ktor:ktor-client-serialization-jvm:2.3.5")
    implementation("com.google.accompanist:accompanist-coil:0.10.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")

    implementation("io.coil-kt:coil-compose:1.4.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
//    implementation("com.squareup.okhttp3:okhttp:4.0.0")

}