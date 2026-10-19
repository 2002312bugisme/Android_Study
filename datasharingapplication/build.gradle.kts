plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.zzz.datasharingapplication"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.zzz.datasharingapplication"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Room 把每个版本的建表 DDL 导出成 JSON，用于版本间 diff 与迁移测试
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
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

    // Room：runtime 参与运行时，compiler 仅编译期生成 _Impl 代码
    // 本模块是纯 Java，用 annotationProcessor；若以后接 Kotlin，改装 KSP 插件 + ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}