package ru.yandex.buggyweatherapp.di

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.yandex.buggyweatherapp.data.repository.LocationRepositoryImpl
import ru.yandex.buggyweatherapp.data.repository.WeatherRepositoryImpl
import ru.yandex.buggyweatherapp.ui.api.LocationRepository
import ru.yandex.buggyweatherapp.ui.api.WeatherRepository
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    companion object {

        @Singleton
        @Provides
        fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
            return LocationServices.getFusedLocationProviderClient(context)
        }

        @Singleton
        @Provides
        fun provideGeocoder(@ApplicationContext context: Context): Geocoder {
            return Geocoder(context, Locale.getDefault())
        }
    }

}