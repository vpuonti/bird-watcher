package fi.valtteri.birdwatcher.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import fi.valtteri.birdwatcher.data.BirdService
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.InputStream
import javax.inject.Singleton

@Module(includes = [
    DatabaseModule::class
])
class AppModule {

    @Provides
    @Singleton
    fun sharedPrefs(app: Application) : SharedPreferences {
        return app.getSharedPreferences("bird_watcher_shared_preferences", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideMoshi() : Moshi {
        return Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideAppContext(app: Application) : Context = app.applicationContext

    @Provides
    @Singleton
    fun provideOkHttp(context: Context) : OkHttpClient {
        // mock bird data API
        val mockApi = Interceptor { chain ->
            val url: HttpUrl = chain.request().url()
            var response = ""
            when(url.encodedPath()) {
                "species" -> {
                    val inputStream: InputStream = context.assets.open("species-names.json")
                    response = String(inputStream.readBytes(), Charsets.UTF_8)
                    inputStream.close()
                }
            }
            return@Interceptor Response.Builder()
                .code(200)
                .message(response)
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .body(ResponseBody.create(MediaType.parse("application/json"), response.toByteArray()))
                .addHeader("content-type", "application/json")
                .build()
        }

        return OkHttpClient.Builder()
            .addInterceptor(mockApi)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl("bla")
            .build()
    }

    @Provides
    @Singleton
    fun provideBirdClient(retrofit: Retrofit) : BirdService {
        return retrofit.create(BirdService::class.java)
    }


}