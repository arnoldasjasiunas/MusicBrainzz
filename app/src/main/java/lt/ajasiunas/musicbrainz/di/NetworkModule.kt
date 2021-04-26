package lt.ajasiunas.musicbrainz.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import lt.ajasiunas.musicbrainz.network.PlacesService
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val BASE_URL = "http://musicbrainz.org/ws/2/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun providePlacesService(retrofit: Retrofit): PlacesService =
        retrofit.create(PlacesService::class.java)

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient, factory: Converter.Factory): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(factory)
            .client(client)
            .build()

    @Singleton
    @Provides
    fun provideHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor {
                val originalRequest = it.request()
                val requestWithUserAgent = originalRequest.newBuilder()
                    .header("User-Agent", "JobApplication/1.0.0 ( pleasedontthrottlemyapp@gmail.com )")
                    .build()
                it.proceed(requestWithUserAgent)
            }
            .build()

    @Singleton
    @Provides
    fun provideGsonFactory(): Converter.Factory =
        GsonConverterFactory.create()

}