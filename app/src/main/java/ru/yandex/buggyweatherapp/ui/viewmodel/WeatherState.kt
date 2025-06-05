package ru.yandex.buggyweatherapp.ui.viewmodel

import ru.yandex.buggyweatherapp.data.model.WeatherData

sealed interface WeatherState {

    data object Start : WeatherState

    data object Loading : WeatherState

    data class Content(val weatherData: WeatherData) : WeatherState

    data class Error(val errorMessage: String) : WeatherState
}