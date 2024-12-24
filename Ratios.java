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

    public static List<RatioData> fetchHistoricalPE(String ticker) {
        List<RatioData> peRatios = new ArrayList<>();
        try {
            // Replace with the actual URL to fetch historical PE ratios from Financial Modeling Prep
            URL url = new URL("https://financialmodelingprep.com/api/v3/historical-pe-ratio/" + ticker + "?apikey=your_api_key");
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
                String date = jsonObject.getString("date");
                double pe = jsonObject.getDouble("peRatio");
                peRatios.add(new RatioData(date, pe));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return peRatios;
    }

    public static List<RatioData> fetchHistoricalPB(String ticker) {
        List<RatioData> pbRatios = new ArrayList<>();
        try {
            // Replace with the actual URL to fetch historical PB ratios from Financial Modeling Prep
            URL url = new URL("https://financialmodelingprep.com/api/v3/historical-pb-ratio/" + ticker + "?apikey=your_api_key");
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
                String date = jsonObject.getString("date");
                double pb = jsonObject.getDouble("pbRatio");
                pbRatios.add(new RatioData(date, pb));
            }
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