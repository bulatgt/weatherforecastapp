package com.example.weatherforecastapp

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.weatherforecastapp.DTO.CityResponse
import com.example.weatherforecastapp.DTO.WeatherData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Call
import java.io.IOException

class MainActivity : ComponentActivity() {

    private lateinit var cityListLayout: LinearLayout
    private lateinit var weatherLayout: LinearLayout
    private lateinit var cityListView: ListView
    private lateinit var weatherListContainer: LinearLayout
    private lateinit var backButton: Button

    private lateinit var header: TextView
    private lateinit var weatherHeader: TextView

    private var ninjaAPIKey: String = "UwLfwCtq4HQc2s21T2K5Cg==6XkjTYGERgpbZlir"

    private val client = OkHttpClient()

    // список городов
    private val cities = listOf("Владивосток", "Хабаровск", "Якутск", "Иркутск", "Красноярск")

    private val citiesNamesForAPI = listOf("Vladivostok","Khabarovsk","Yakutsk","Irkutsk","Krasnoyarsk")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // инициализация UI компонентов
        cityListLayout = findViewById(R.id.cityListLayout)
        weatherLayout = findViewById(R.id.weatherLayout)
        cityListView = findViewById(R.id.cityListView)
        weatherListContainer = findViewById(R.id.weatherListContainer)
        backButton = findViewById(R.id.backButton)

        // установка адаптера для списка городов
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, cities)
        cityListView.adapter = adapter

        header = findViewById(R.id.main_heading)
        setDynamicHeaderPadding(header)

        weatherHeader = findViewById(R.id.weatherHeading)
        setDynamicHeaderPadding(weatherHeader)

        // обработчик нажатий на элементы списка городов
        cityListView.setOnItemClickListener { _, _, position, _ ->
            val selectedCity = citiesNamesForAPI[position]//cities[position]
            showWeatherForCity(selectedCity)
        }

        // обработчик нажатия на кнопку "Назад"
        backButton.setOnClickListener {
            showCityList()
        }

        // состояние
        if (savedInstanceState != null) {
            val isWeatherVisible = savedInstanceState.getBoolean("isWeatherVisible", false)
            if (isWeatherVisible) {
                val selectedCity = savedInstanceState.getString("selectedCity", "")
                showWeatherForCity(selectedCity)
            } else {
                showCityList()
            }
        }


    }
    suspend fun fetchData(cityName: String): String? {
        return withContext(Dispatchers.IO) { // Выполняем запрос на фоне
            try {
                // Подготовка запроса
                val request = Request.Builder()
                    .url("https://api.api-ninjas.com/v1/city?name=$cityName")
                    .addHeader("X-Api-Key", "UwLfwCtq4HQc2s21T2K5Cg==6XkjTYGERgpbZlir")
                    .build()

                // Выполнение запроса
                val response: Response = client.newCall(request).execute()

                // Проверка успешности ответа
                if (response.isSuccessful) {
                    response.body?.string() // Возвращаем JSON как строку
                } else {
                    null // Если произошла ошибка, возвращаем null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null // В случае ошибки возвращаем null
            }
        }
    }
    suspend fun fetchWeatherData(latitude: String, longitude: String): String? {
        return withContext(Dispatchers.IO) { // Выполняем запрос на фоне
            try {
                // Подготовка запроса
                val request = Request.Builder()
                    .url("https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&hourly=temperature_2m&forecast_days=1")
                    .build()

                // Выполнение запроса
                val response: Response = client.newCall(request).execute()

                // Проверка успешности ответа
                if (response.isSuccessful) {
                    response.body?.string() // Возвращаем JSON как строку
                } else {
                    null // Если произошла ошибка, возвращаем null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null // В случае ошибки возвращаем null
            }
        }
    }
    private fun setDynamicHeaderPadding(header: TextView) {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        // padding с учётом статус-бара
        header.setPadding(0, statusBarHeight, 0, 0)
    }
    // прогноз погоды для выбранного города
    private fun showWeatherForCity(nameOfCity: String) {

        lifecycleScope.launch {
            val response = fetchData(nameOfCity)

            // Обработка результата
            if (response != null) {

            } else {
                println("Ошибка при выполнении запроса")
            }

            val gson = Gson()

            val cityListType = object : TypeToken<List<CityResponse>>() {}.type

            val cities: List<CityResponse> = gson.fromJson(response, cityListType)


            val city = cities[0]
            println("Name: ${city.name}")
            println("Latitude: ${city.latitude}")
            println("Longitude: ${city.longitude}")

            val weatherResponse = fetchWeatherData(city.latitude.toString(),city.longitude.toString())

            val weatherListType = object :TypeToken<List<WeatherData>>() {}.type
            val weather: WeatherData = gson.fromJson(weatherResponse, WeatherData::class.java)

            val temperatureData = weather.hourly.temperature_2m

            temperatureData.forEach { println(it) }
            cityListLayout.visibility = LinearLayout.GONE
            weatherLayout.visibility = LinearLayout.VISIBLE

            val weatherHeading: TextView = findViewById(R.id.weatherHeading)
            weatherHeading.text = "Прогноз погоды для $nameOfCity"

            // очистить контейнер и сгенерировать данные
            weatherListContainer.removeAllViews()
            for (hour in 0..23) {
                val temperature = temperatureData[hour]
                val weatherItem = createWeatherItem(hour, temperature)
                weatherListContainer.addView(weatherItem)
            }
        }


    }

    // вернуться к списку городов
    private fun showCityList() {
        weatherLayout.visibility = LinearLayout.GONE
        cityListLayout.visibility = LinearLayout.VISIBLE
    }

    // создать элемент прогноза для конкретного часа
    private fun createWeatherItem(hour: Int, temperature: Double): LinearLayout {
        val itemLayout = LinearLayout(this)
        itemLayout.orientation = LinearLayout.HORIZONTAL
        itemLayout.setPadding(16, 16, 16, 16)
        itemLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)

        val timeText = TextView(this)
        timeText.text = String.format("%02d:00", hour)
        timeText.textSize = 16f
        timeText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        val tempText = TextView(this)
        tempText.text = "$temperature°С"
        tempText.textSize = 16f
        tempText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

        itemLayout.addView(timeText)
        itemLayout.addView(tempText)

        return itemLayout
    }

    // сохранение состояния
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val isWeatherVisible = weatherLayout.visibility == LinearLayout.VISIBLE
        outState.putBoolean("isWeatherVisible", isWeatherVisible)

        if (isWeatherVisible) {
            val heading: TextView = findViewById(R.id.weatherHeading)
            val selectedCity = heading.text.toString().removePrefix("Прогноз погоды для ")
            outState.putString("selectedCity", selectedCity)
        }
    }
}