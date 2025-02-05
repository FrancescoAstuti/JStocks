package afin.jstocks;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;
import org.json.JSONArray;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class KeyMetricsTTM {
    private static final String API_KEY = "eb7366217370656d66a56a057b8511b0";
    private static final String API_URL = "https://financialmodelingprep.com/api/v3/key-metrics-ttm/";

    public static String getEPSTTM(String ticker) throws IOException {
        String urlString = API_URL + ticker + "?apikey=" + API_KEY;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        
        if (responseCode == 401) {
            throw new RuntimeException("Unauthorized: Invalid API Key");
        } else if (responseCode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responseCode);
        } else {
            String inline = "";
            Scanner scanner = new Scanner(url.openStream());

            // Write all the JSON data into a string using a scanner
            while (scanner.hasNext()) {
                inline += scanner.nextLine();
            }

            // Close the scanner
            scanner.close();

            // Using the JSON simple library parse the string into a JSON object
            JSONArray jsonArray = new JSONArray(inline);
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            // Get the EPS TTM from the JSON object
            return jsonObject.getString("epsTTM");
        }
    }

    public static String getROETTM(String ticker) throws IOException {
        String urlString = API_URL + ticker + "?apikey=" + API_KEY;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();

        if (responseCode == 401) {
            throw new RuntimeException("Unauthorized: Invalid API Key");
        } else if (responseCode != 200) {
            throw new RuntimeException("HttpResponseCode: " + responseCode);
        } else {
            String inline = "";
            Scanner scanner = new Scanner(url.openStream());

            // Write all the JSON data into a string using a scanner
            while (scanner.hasNext()) {
                inline += scanner.nextLine();
            }

            // Close the scanner
            scanner.close();

            // Using the JSON simple library parse the string into a JSON object
            JSONArray jsonArray = new JSONArray(inline);
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            // Get the ROE TTM from the JSON object
            return jsonObject.getString("roeTTM");
        }
    }

public static double getDividendYieldTTM(String ticker) throws IOException {
    String urlString = API_URL + ticker + "?apikey=" + API_KEY;
    URL url = new URL(urlString);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.connect();

    int responseCode = conn.getResponseCode();
    
    if (responseCode == 401) {
        throw new RuntimeException("Unauthorized: Invalid API Key");
    } else if (responseCode != 200) {
        throw new RuntimeException("HttpResponseCode: " + responseCode);
    } else {
        String inline = "";
        Scanner scanner = new Scanner(url.openStream());

        // Write all the JSON data into a string using a scanner
        while (scanner.hasNext()) {
            inline += scanner.nextLine();
        }

        // Close the scanner
        scanner.close();

        // Using the JSON simple library parse the string into a JSON object
        JSONArray jsonArray = new JSONArray(inline);
        JSONObject jsonObject = jsonArray.getJSONObject(0);

        // Get the dividend yield TTM from the JSON object
        double dividendYieldTTM = jsonObject.getDouble("dividendYieldPercentageTTM");

        // Round to one decimal place using BigDecimal
        dividendYieldTTM = new BigDecimal(dividendYieldTTM)
                               .setScale(1, RoundingMode.HALF_UP)
                               .doubleValue();

        return dividendYieldTTM;
    }
}
}