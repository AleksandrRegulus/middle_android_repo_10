package ru.yandex.buggyweatherapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.repository.LocationRepository
import ru.yandex.buggyweatherapp.repository.WeatherRepository
import ru.yandex.buggyweatherapp.utils.ImageLoader

class WeatherViewModel : ViewModel() {


    private lateinit var activityContext: Context


    private val weatherRepository = WeatherRepository()
    private val locationRepository by lazy {
        LocationRepository(activityContext)
    }

    private val _weatherScreenState = MutableStateFlow<WeatherState>(WeatherState.Start)
    val weatherScreenState: StateFlow<WeatherState> = _weatherScreenState

    private var currentLocation: Location? = null

    fun initialize(context: Context) {
        this.activityContext = context
        fetchCurrentLocationWeather()
        startAutoRefresh()
    }

    private fun fetchCurrentLocationWeather() {
        renderState(WeatherState.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            locationRepository.getCurrentLocation { location ->
                if (location != null) {
                    currentLocation = location
                    getWeatherForLocation(location)
                } else {
                    renderState(WeatherState.Error("Unable to get current location"))
                }
            }
        }
    }

    private fun getWeatherForLocation(location: Location) {
        renderState(WeatherState.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.getWeatherData(location) { data, exception ->
                if (data != null) {
                    renderState(WeatherState.Content(data))
                } else {
                    renderState(WeatherState.Error(exception?.message ?: "Unknown error"))
                }
            }
        }
    }

    fun searchWeatherByCity(city: String) {
        if (city.isBlank()) {
            currentLocation = null
            renderState(WeatherState.Error("City name cannot be empty"))
            return
        }
        renderState(WeatherState.Loading)

        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.getWeatherByCity(city) { data, exception ->
                if (data != null) {
                    renderState(WeatherState.Content(data))
                    currentLocation = Location(0.0, 0.0, data.cityName)
                } else {
                    renderState(WeatherState.Error(exception?.message ?: "Unknown error"))
                }
            }
        }
    }

    fun refreshWeather() {
        currentLocation?.let { location ->
            location.name?.let { searchWeatherByCity(it) } ?: getWeatherForLocation(location)
        }
    }


    fun formatTemperature(temp: Double): String {
        return "${temp.toInt()}°C"
    }


    fun loadWeatherIcon(iconCode: String) {
        viewModelScope.launch {
            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
            ImageLoader.loadImage(iconUrl)
        }
    }


    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(TIMER_REFRESH_WEATHER)
                refreshWeather()
            }
        }
    }


    fun toggleFavorite() {
        if (_weatherScreenState.value is WeatherState.Content) {
            val state = _weatherScreenState.value as WeatherState.Content
            renderState(
                WeatherState.Content(
                    state.weatherData.copy(
                        isFavorite = !state.weatherData.isFavorite
                    )
                )
            )
        }
    }

    private fun renderState(state: WeatherState) {
        _weatherScreenState.update { state }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelScope.coroutineContext.cancelChildren()
    }

    companion object {
        private const val TIMER_REFRESH_WEATHER = 60000L
    }
}