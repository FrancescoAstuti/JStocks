package afin.jstocks;  // Package declaration, indicating that this class is part of the afin.jstocks package

import org.json.JSONArray;  // Importing the JSONArray class from the org.json package
import org.json.JSONObject;  // Importing the JSONObject class from the org.json package

import java.io.IOException;  // Importing the IOException class from the java.io package
import java.net.HttpURLConnection;  // Importing the HttpURLConnection class from the java.net package
import java.net.URL;  // Importing the URL class from the java.net package
import java.util.Scanner;  // Importing the Scanner class from the java.util package

public class Estimates {  // Declaring the Estimates class
    private static final String API_KEY = "eb7366217370656d66a56a057b8511b0";  // A constant for the API key used to fetch data

    // Method to fetch EPS estimates for a given stock ticker
    public static JSONObject fetchEpsEstimates(String ticker) {
        // Construct the URL string for the API request
        String urlString = String.format("https://financialmodelingprep.com/api/v3/analyst-estimates/%s?apikey=%s", ticker, API_KEY);
        HttpURLConnection connection = null;  // Initialize the connection as null

        try {
            URL url = new URL(urlString);  // Create a URL object from the urlString
            connection = (HttpURLConnection) url.openConnection();  // Open a connection to the URL
            connection.setRequestMethod("GET");  // Set the request method to GET

            int responseCode = connection.getResponseCode();  // Get the response code from the connection
            if (responseCode == 200) {  // If the response code is 200 (OK)
                Scanner scanner = new Scanner(url.openStream());  // Open a stream from the URL and create a Scanner object
                String response = scanner.useDelimiter("\\Z").next();  // Read the entire response into a string
                scanner.close();  // Close the scanner

                JSONArray data = new JSONArray(response);  // Parse the response string into a JSONArray
                if (data.length() > 0) {  // If the array is not empty
                    JSONObject result = new JSONObject();  // Create a new JSONObject to hold the result
                    JSONObject firstEstimate = data.getJSONObject(0);  // Get the first estimate from the array

                    // Get EPS estimates for the next three years
                    result.put("epsNextYear", firstEstimate.optDouble("estimatedEpsAvg", 0.0));  // Add the EPS estimate for next year to the result

                    // For year 2 and 3, we need to look at the subsequent entries if available
                    if (data.length() > 1) {  // If there is more than one estimate
                        result.put("epsYear2", data.getJSONObject(1).optDouble("estimatedEpsAvg", 0.0));  // Add the EPS estimate for the second year to the result
                    } else {
                        result.put("epsYear2", 0.0);  // Otherwise, set the EPS estimate for the second year to 0.0
                    }

                    if (data.length() > 2) {  // If there are more than two estimates
                        result.put("epsYear3", data.getJSONObject(2).optDouble("estimatedEpsAvg", 0.0));  // Add the EPS estimate for the third year to the result
                    } else {
                        result.put("epsYear3", 0.0);  // Otherwise, set the EPS estimate for the third year to 0.0
                    }

                    return result;  // Return the result
                }
            }
        } catch (IOException e) {  // Catch any IOException that occurs
            e.printStackTrace();  // Print the stack trace of the exception
        } finally {
            if (connection != null) {  // If the connection is not null
                connection.disconnect();  // Disconnect the connection
            }
        }
        return null;  // Return null if no valid data was fetched
    }
}