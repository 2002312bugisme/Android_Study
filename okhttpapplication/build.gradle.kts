plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.zzz.okhttpapplication"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.zzz.okhttpapplication"
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
    implementation(libs.material)
    // OkHttp：网络请求库，内部自带连接池与线程池，版本集中在 gradle/libs.versions.toml
    implementation(libs.okhttp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}