package afin.jstocks;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Ratios {

    public static List<Double> fetchHistoricalPE(String ticker) {
        List<Double> peRatios = new ArrayList<>();
        try {
            // Replace with the actual URL to fetch historical PE ratios
            URL url = new URL("https://api.example.com/historical/pe/" + ticker);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            JSONArray jsonArray = new JSONArray(content.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                peRatios.add(jsonObject.getDouble("pe"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return peRatios;
    }

    public static List<Double> fetchHistoricalPB(String ticker) {
        List<Double> pbRatios = new ArrayList<>();
        try {
            // Replace with the actual URL to fetch historical PB ratios
            URL url = new URL("https://api.example.com/historical/pb/" + ticker);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            JSONArray jsonArray = new JSONArray(content.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                pbRatios.add(jsonObject.getDouble("pb"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pbRatios;
    }
}