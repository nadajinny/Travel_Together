plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("io.socket:socket.io-client:2.0.0"){
        exclude(group = "org.json", module = "json")
    }
    //firebase sdk dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.6.0")) //firebase Bom
    implementation("com.google.firebase:firebase-auth") //firebase authentication
    implementation("com.google.firebase:firebase-firestore") // firebase firestore
    implementation("com.google.firebase:firebase-analytics")

    //other dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.legacy.support.v13)
    implementation(libs.androidx.activity)

}
apply(plugin = "com.google.gms.google-services") //apply firebase plugin