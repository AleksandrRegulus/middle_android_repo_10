package ru.yandex.buggyweatherapp.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.yandex.buggyweatherapp.WeatherApplication
import ru.yandex.buggyweatherapp.model.Location
import ru.yandex.buggyweatherapp.model.WeatherData
import ru.yandex.buggyweatherapp.repository.LocationRepository
import ru.yandex.buggyweatherapp.repository.WeatherRepository
import ru.yandex.buggyweatherapp.utils.ImageLoader
import java.util.Timer
import java.util.TimerTask

class WeatherViewModel : ViewModel() {
    
    
    private lateinit var activityContext: Context
    
    
    private val weatherRepository = WeatherRepository()
    private val locationRepository by lazy { 
        LocationRepository(activityContext)
    }


    val weatherData = MutableLiveData<WeatherData>()
    private val currentLocation = MutableLiveData<Location>(null)
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()
    val cityName = MutableLiveData<String>()
    

    fun initialize(context: Context) {
        this.activityContext = context
        fetchCurrentLocationWeather()

        startAutoRefresh()
    }
    
    
    fun fetchCurrentLocationWeather() {
        isLoading.value = true
        error.value = null
        
        locationRepository.getCurrentLocation { location ->
            if (location != null) {
                currentLocation.value = location
                
                
                val cityNameFromLocation = locationRepository.getCityNameFromLocation(location)
                cityName.value = cityNameFromLocation
                
                getWeatherForLocation(location)
            } else {
                isLoading.value = false
                error.value = "Unable to get current location"
            }
        }
    }
    
    fun getWeatherForLocation(location: Location) {
        isLoading.value = true
        error.value = null
        
        weatherRepository.getWeatherData(location) { data, exception ->
            
            Handler(Looper.getMainLooper()).post {
                isLoading.value = false
                
                if (data != null) {
                    weatherData.value = data
                } else {
                    error.value = exception?.message ?: "Unknown error"
                }
            }
        }
    }
    
    fun searchWeatherByCity(city: String) {
        if (city.isBlank()) {
            error.value = "City name cannot be empty"
            return
        }
        
        isLoading.value = true
        error.value = null
        
        
        weatherRepository.getWeatherByCity(city) { data, exception ->
            
            isLoading.value = false
            
            if (data != null) {
                weatherData.value = data
                cityName.value = data.cityName
                currentLocation.value = Location(0.0, 0.0, data.cityName)
            } else {
                error.value = exception?.message ?: "Unknown error"
            }
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
                currentLocation.value?.let { location ->
                    getWeatherForLocation(location)
                }
            }
        }
    }
    
    
    fun toggleFavorite() {
        weatherData.value?.let {
            it.isFavorite = !it.isFavorite
            
            weatherData.value = it
        }
    }
    
    
    override fun onCleared() {
        super.onCleared()
        
    }

    companion object {
        private const val TIMER_REFRESH_WEATHER = 60000L
    }
}