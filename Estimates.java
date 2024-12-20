package afin.jstocks;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Estimates {
    private static final String API_KEY = "eb7366217370656d66a56a057b8511b0";

    public static JSONObject fetchEpsEstimates(String ticker) {
        String urlString = String.format("https://financialmodelingprep.com/api/v3/analyst-estimates/%s?apikey=%s", ticker, API_KEY);
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Scanner scanner = new Scanner(url.openStream());
                String response = scanner.useDelimiter("\\Z").next();
                scanner.close();

                JSONArray data = new JSONArray(response);
                if (data.length() > 0) {
                    JSONObject result = new JSONObject();
                    JSONObject firstEstimate = data.getJSONObject(0);

                    // Get EPS estimates for the next three years
                    result.put("epsNextYear", firstEstimate.optDouble("estimatedEpsAvg", 0.0));

                    // For year 2 and 3, we need to look at the subsequent entries if available
                    if (data.length() > 1) {
                        result.put("epsYear2", data.getJSONObject(1).optDouble("estimatedEpsAvg", 0.0));
                    } else {
                        result.put("epsYear2", 0.0);
                    }

                    if (data.length() > 2) {
                        result.put("epsYear3", data.getJSONObject(2).optDouble("estimatedEpsAvg", 0.0));
                    } else {
                        result.put("epsYear3", 0.0);
                    }

                    return result;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}