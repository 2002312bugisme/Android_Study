plugins {
    alias(libs.plugins.android.application)
    // Hilt：除了依赖，还必须 apply 这个插件。
    // 它做两件事：① 汇总各模块的注解处理结果（跨模块注入要靠它）；② 对 @AndroidEntryPoint 的类做字节码插桩。
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.zzz.retrofitapplication"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.zzz.retrofitapplication"
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
    // Retrofit 本体：把「HTTP 接口」写成 Java 接口 + 注解（@GET/@POST/@Query…），
    // 运行时 Retrofit 用动态代理生成实现类，真正发请求的还是它内部的 OkHttpClient
    implementation(libs.retrofit)
    // Converter：负责响应体（JSON 字符串）→ Java 对象的反序列化。
    // 少了它，方法返回值不是 ResponseBody/String 时会抛
    // "Unable to create converter for class ..."
    implementation(libs.retrofit.converter.gson)
    // 日志拦截器：把请求行、请求头、响应体打到 Logcat，排查「请求到底发出去没有」最有用；
    // 纯学习期建议留着，上线前可删
    implementation(libs.okhttp.logging.interceptor)
    // 第三步：RxJava3 。adapter 负责把 Call 适配成 Observable，rxandroid 提供主线程调度器
    implementation(libs.retrofit.adapter.rxjava3)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    // Hilt 运行时：@HiltAndroidApp / @AndroidEntryPoint / @Inject 的背后实现
    implementation(libs.hilt.android)
    // Hilt 注解处理器：编译期生成 Dagger 组件。
    // 这是纯 Java 模块，所以用 annotationProcessor；如果有 Kotlin 代码要换成 kapt/ksp
    annotationProcessor(libs.hilt.android.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}