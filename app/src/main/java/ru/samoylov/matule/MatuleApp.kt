package ru.samoylov.matule

import android.app.Application
import ru.samoylov.matule.di.ServiceLocator

/**
 * Класс приложения.
 * Единственная задача — собрать зависимости при запуске.
 */
class MatuleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
