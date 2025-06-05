package ru.yandex.buggyweatherapp.ui.api

import ru.yandex.buggyweatherapp.data.model.Location

interface LocationRepository {

    fun getCurrentLocation(callback: (Location?) -> Unit)
    fun getCityNameFromLocation(location: Location): String?
    fun startLocationTracking()
}