// Корневой файл сборки. Плагины объявляются здесь и применяются в модулях.
//
// Начиная с AGP 9.x поддержка Kotlin встроена в сам Android Gradle Plugin
// (он создаёт расширение "kotlin" самостоятельно), поэтому отдельный плагин
// org.jetbrains.kotlin.android здесь не подключается — это вызвало бы
// конфликт "Cannot add extension with name 'kotlin'".
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}
