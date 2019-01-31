package fi.valtteri.birdwatcher.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.Lifecycle
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import fi.valtteri.birdwatcher.MainActivity
import javax.inject.Singleton

@Module(includes = [
    DatabaseModule::class,
    ViewModelModule::class
])
class AppModule {

    @Provides
    @Singleton
    fun sharedPrefs(app: Application) : SharedPreferences {
        return app.getSharedPreferences("bird_watcher_shared_preferences", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun moshi() : Moshi {
        return Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideAppContext(app: Application) : Context = app.applicationContext

}