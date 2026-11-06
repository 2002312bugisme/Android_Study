// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // Hilt 插件统一在这里声明版本、不应用（apply false）；
    // 真正 apply 它的模块才能生成 @HiltAndroidApp / @AndroidEntryPoint 的字节码
    alias(libs.plugins.hilt) apply false
}