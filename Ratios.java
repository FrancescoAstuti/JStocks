package afin.jstocks;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ratios {

    private static final String API_KEY = "eb7366217370656d66a56a057b8511b0";

    public static List<RatioData> fetchHistoricalPE(String ticker) {
        Map<String, List<Double>> yearlyPERatios = new HashMap<>();
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
                String year = date.split("-")[0];  // Extract year from date
                double pe = jsonObject.optDouble("priceEarningsRatio", Double.NaN);  // Use "priceEarningsRatio", handle missing fields
                if (!Double.isNaN(pe) && (2024 - Integer.parseInt(year)) < 20) {  // Filter for the last 20 years
                    System.out.println("Date: " + date + ", Year: " + year + ", PE: " + pe);  // Debugging statement
                    yearlyPERatios.computeIfAbsent(year, k -> new ArrayList<>()).add(pe);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Aggregate quarterly data to yearly data (average)
        List<RatioData> peRatios = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : yearlyPERatios.entrySet()) {
            String year = entry.getKey();
            List<Double> values = entry.getValue();
            double averagePE = values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
            peRatios.add(new RatioData(year, averagePE));
        }

        // Sort the list by year in ascending order
        peRatios.sort((r1, r2) -> r1.getYear().compareTo(r2.getYear()));

        return peRatios;
    }

    public static List<RatioData> fetchHistoricalPB(String ticker) {
        Map<String, List<Double>> yearlyPBRatios = new HashMap<>();
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
                String year = date.split("-")[0];  // Extract year from date
                double pb = jsonObject.optDouble("priceToBookRatio", Double.NaN);  // Use "priceToBookRatio", handle missing fields
                if (!Double.isNaN(pb) && (2024 - Integer.parseInt(year)) < 20) {  // Filter for the last 20 years
                    System.out.println("Date: " + date + ", Year: " + year + ", PB: " + pb);  // Debugging statement
                    yearlyPBRatios.computeIfAbsent(year, k -> new ArrayList<>()).add(pb);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Aggregate quarterly data to yearly data (average)
        List<RatioData> pbRatios = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : yearlyPBRatios.entrySet()) {
            String year = entry.getKey();
            List<Double> values = entry.getValue();
            double averagePB = values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
            pbRatios.add(new RatioData(year, averagePB));
        }

        // Sort the list by year in ascending order
        pbRatios.sort((r1, r2) -> r1.getYear().compareTo(r2.getYear()));

        return pbRatios;
    }
}

class RatioData {
    private String year;
    private double value;

    public RatioData(String year, double value) {
        this.year = year;
        this.value = value;
    }

    public String getYear() {
        return year;
    }

    public double getValue() {
        return value;
    }
}