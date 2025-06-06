package ru.yandex.buggyweatherapp.ui.api

import ru.yandex.buggyweatherapp.data.model.Location
import ru.yandex.buggyweatherapp.data.model.WeatherData

interface WeatherRepository {

    fun getWeatherData(location: Location, callback: (WeatherData?, Exception?) -> Unit)
    fun getWeatherByCity(cityName: String, callback: (WeatherData?, Exception?) -> Unit)
}