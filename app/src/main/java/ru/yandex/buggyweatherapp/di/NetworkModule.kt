package ru.yandex.buggyweatherapp.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.yandex.buggyweatherapp.data.api.WeatherApiService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NetworkModule {

    companion object {

        @Singleton
        @Provides
        fun provideWeatherApiService(): WeatherApiService {

            return Retrofit
                .Builder()
                .baseUrl(WeatherApiService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .build()
                .create(WeatherApiService::class.java)
        }
    }
}