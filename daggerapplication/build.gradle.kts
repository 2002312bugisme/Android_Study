plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.zzz.daggerapplication"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.zzz.daggerapplication"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    // Source: https://mvnrepository.com/artifact/com.google.dagger/dagger
    implementation(libs.dagger)
    // 注解处理器使用 annotationProcessor
    annotationProcessor(libs.dagger.compiler)
    implementation(libs.material)
    testImplementation(libs.junit)

    // Source: https://mvnrepository.com/artifact/com.squareup.retrofit2/retrofit
    implementation(libs.retrofit.v290)
    // Source: https://mvnrepository.com/artifact/com.squareup.retrofit2/converter-gson
    implementation(libs.converter.gson.v290)
    // Source: https://mvnrepository.com/artifact/com.squareup.retrofit2/converter-scalars
    implementation(libs.converter.scalars)

    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}