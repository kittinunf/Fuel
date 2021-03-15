package fuel.samples

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Types
import fuel.Fuel
import fuel.moshi.toMoshi
import fuel.request
import fuel.toCoroutines
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

@JsonClass(generateAdapter = true)
data class Location(
    val title: String,
    val latt_long: String,
    val woeid: Int
)

@JsonClass(generateAdapter = true)
data class ConsolidatedWeather(
    val consolidated_weather: List<ConsolidatedWeatherEntry>
)

@JsonClass(generateAdapter = true)
data class ConsolidatedWeatherEntry(
    val applicable_date: String,
    val weather_state_name: String,
    val the_temp: Float
)

fun main() {
    runBlocking {
        val locations = listOf("London", "Tokyo")
        locations.forEach { location ->
            val locationsList = Fuel.request(WeatherApi.WeatherFor(location))
                .toCoroutines()
                .toMoshi<List<Location>>(Types.newParameterizedType(List::class.java, Location::class.java))!!
            println("Weather for $locationsList : ${locationsList.first().latt_long}")

            val weathers = Fuel.request(WeatherApi.ConsolidatedWeatherFor(locationsList.first().woeid))
                .toCoroutines()
                .toMoshi<ConsolidatedWeather>()
            println("Date           Weather         Temperature(°C) ")
            println("-----------------------------------------------")
            weathers?.consolidated_weather?.forEach { println(it.applicable_date + "     " + it.weather_state_name + "     " + it.the_temp) }
            println("-----------------------------------------------")
        }
    }
    exitProcess(0)
}
