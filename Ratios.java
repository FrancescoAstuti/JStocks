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

    private static final String API_KEY = "eb7366217370656d66a56a057b8511b0";

    public static List<RatioData> fetchHistoricalPE(String ticker) {
        List<RatioData> peRatios = new ArrayList<>();
        try {
            URL url = new URL("https://financialmodelingprep.com/api/v3/ratios/" + ticker + "?period=annual&apikey=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("PE API Response Code: " + responseCode);  // Debugging statement

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            System.out.println("PE API Response: " + content.toString());  // Debugging statement

            JSONArray jsonArray = new JSONArray(content.toString());
            if (jsonArray.length() == 0) {
                System.out.println("No historical PE data available for ticker: " + ticker);
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String year = jsonObject.getString("calendarYear");
                    double pe = jsonObject.getDouble("priceEarningsRatio");
                    peRatios.add(new RatioData(year, pe));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return peRatios;
    }

    public static List<RatioData> fetchHistoricalPB(String ticker) {
        List<RatioData> pbRatios = new ArrayList<>();
        try {
            URL url = new URL("https://financialmodelingprep.com/api/v3/ratios/" + ticker + "?period=annual&apikey=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("PB API Response Code: " + responseCode);  // Debugging statement

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            System.out.println("PB API Response: " + content.toString());  // Debugging statement

            JSONArray jsonArray = new JSONArray(content.toString());
            if (jsonArray.length() == 0) {
                System.out.println("No historical PB data available for ticker: " + ticker);
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String year = jsonObject.getString("calendarYear");
                    double pb = jsonObject.getDouble("priceToBookRatio");
                    pbRatios.add(new RatioData(year, pb));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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