import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class KeyMetricsTTM {

    private static final String API_URL = "https://financialmodelingprep.com/api/v3/key-metrics-ttm/";

    public static String getEPSTTM(String ticker) throws IOException {
        String urlString = API_URL + ticker;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        
        if (responseCode != 200) {
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
            JSONObject jsonObject = new JSONObject(inline);

            // Get the EPS TTM from the JSON object
            return jsonObject.getJSONArray("metrics").getJSONObject(0).getString("epsTTM");
        }
    }

    public static String getROETTM(String ticker) throws IOException {
        String urlString = API_URL + ticker;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
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
            JSONObject jsonObject = new JSONObject(inline);

            // Get the ROE TTM from the JSON object
            return jsonObject.getJSONArray("metrics").getJSONObject(0).getString("roeTTM");
        }
    }
}