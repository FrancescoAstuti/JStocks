package afin.jstocks;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Ratios {

    private static final String API_KEY = "eb7366217370656d66a56a057b8511b0";

    public static List<RatioData> fetchHistoricalPE(String ticker) {
        List<RatioData> peRatios = new ArrayList<>();
        try {
            URL url = new URL("https://financialmodelingprep.com/api/v3/ratios/" + ticker + "?period=quarter&apikey=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("PE API Response Code: " + responseCode);  // Debugging statement

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            System.out.println("PE API Response: " + content.toString());  // Debugging statement

            JSONArray jsonArray = new JSONArray(content.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String date = jsonObject.getString("date");
                double pe = jsonObject.optDouble("priceEarningsRatio", Double.NaN);  // Use "priceEarningsRatio", handle missing fields
                if (!Double.isNaN(pe) && (2024 - Integer.parseInt(date.split("-")[0])) < 20) {  // Filter for the last 20 years
                    System.out.println("Date: " + date + ", PE: " + pe);  // Debugging statement
                    peRatios.add(new RatioData(date, pe));
                }
            }

            // Sort the list by date in ascending order
            Collections.sort(peRatios, Comparator.comparing(RatioData::getDate));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return peRatios;
    }

    public static List<RatioData> fetchHistoricalPB(String ticker) {
        List<RatioData> pbRatios = new ArrayList<>();
        try {
            URL url = new URL("https://financialmodelingprep.com/api/v3/ratios/" + ticker + "?period=quarter&apikey=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("PB API Response Code: " + responseCode);  // Debugging statement

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            System.out.println("PB API Response: " + content.toString());  // Debugging statement

            JSONArray jsonArray = new JSONArray(content.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String date = jsonObject.getString("date");
                double pb = jsonObject.optDouble("priceToBookRatio", Double.NaN);  // Use "priceToBookRatio", handle missing fields
                if (!Double.isNaN(pb) && (2024 - Integer.parseInt(date.split("-")[0])) < 20) {  // Filter for the last 20 years
                    System.out.println("Date: " + date + ", PB: " + pb);  // Debugging statement
                    pbRatios.add(new RatioData(date, pb));
                }
            }

            // Sort the list by date in ascending order
            Collections.sort(pbRatios, Comparator.comparing(RatioData::getDate));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return pbRatios;
    }
}

class RatioData {
    private String date;
    private double value;

    public RatioData(String date, double value) {
        this.date = date;
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }
}