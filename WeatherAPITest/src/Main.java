import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.WeatherData;
import org.json.simple.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        WeatherData weatherData = new WeatherData();
        JSONObject response = weatherData.getWeatherData();

        //System.out.println(response);

        // Create an ObjectMapper instance
        var objectMapper = new ObjectMapper();

        // Deserialize the JSON string into an array of User objects
        try {

            var responseMap = new HashMap<String, Object>();

            JSONObject hourlyUnits = (JSONObject) response.get("hourly_units");
            //System.out.println("hourlyUnits : "+hourlyUnits);
            var hourlyUnitsMap = new HashMap<String, Object>();
            hourlyUnitsMap.put("time", hourlyUnits.get("time"));
            hourlyUnitsMap.put("temperature_2m", hourlyUnits.get("temperature_2m"));


            JSONObject hourly = (JSONObject) response.get("hourly");
            //System.out.println("hourly : "+hourly);

            var hourlyMap = new HashMap<String, Object>();
            hourlyMap.put("time", hourly.get("time"));
            hourlyMap.put("temperature_2m", hourly.get("temperature_2m"));


            responseMap.put("elevation", response.get("elevation"));
            responseMap.put("generationtime_ms", response.get("generationtime_ms"));
            responseMap.put("timezone_abbreviation", response.get("timezone_abbreviation"));
            responseMap.put("timezone", response.get("timezone"));
            responseMap.put("latitude", response.get("latitude"));
            responseMap.put("longitude", response.get("longitude"));
            responseMap.put("hourly_units", hourlyUnitsMap);
            responseMap.put("hourly", hourlyMap);
            var finalResponse = objectMapper.writeValueAsString(responseMap);
            System.out.println(finalResponse);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}