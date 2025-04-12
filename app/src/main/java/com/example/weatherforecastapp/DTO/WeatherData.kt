package com.example.weatherforecastapp.DTO

data class HourlyUnits(
    val time: String,
    val temperature_2m: String
)

data class Hourly(
    val time: List<String>,
    val temperature_2m: List<Double>
)

data class WeatherData(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val elevation: Double,
    val hourly_units: HourlyUnits,
    val hourly: Hourly
)