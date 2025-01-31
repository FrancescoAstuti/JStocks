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
                    String date = jsonObject.getString("date");
                    double pe = jsonObject.getDouble("priceEarningsRatio");
                    peRatios.add(new RatioData(date, pe));
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
                    String date = jsonObject.getString("date");
                    double pb = jsonObject.getDouble("priceToBookRatio");
                    pbRatios.add(new RatioData(date, pb));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pbRatios;
    }

    public static List<RatioData> fetchQuarterlyEPS(String ticker) {
        List<RatioData> epsRatios = new ArrayList<>();
        try {
            URL url = new URL("https://financialmodelingprep.com/api/v3/income-statement/" + ticker + "?period=annual&apikey=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("EPS API Response Code: " + responseCode);  // Debugging statement

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            System.out.println("EPS API Response: " + content.toString());  // Debugging statement

            JSONArray jsonArray = new JSONArray(content.toString());
            if (jsonArray.length() == 0) {
                System.out.println("No quarterly EPS data available for ticker: " + ticker);
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String date = jsonObject.getString("date");
                    double eps = jsonObject.getDouble("eps");
                    epsRatios.add(new RatioData(date, eps));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return epsRatios;
    }

    // New method to fetch historical priceToFreeCashFlowsRatio
    public static List<RatioData> fetchHistoricalPFCF(String ticker) {
        List<RatioData> pfcfRatios = new ArrayList<>();
        try {
            URL url = new URL("https://financialmodelingprep.com/api/v3/ratios/" + ticker + "?period=annual&apikey=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("PFCF API Response Code: " + responseCode);  // Debugging statement

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            System.out.println("PFCF API Response: " + content.toString());  // Debugging statement

            JSONArray jsonArray = new JSONArray(content.toString());
            if (jsonArray.length() == 0) {
                System.out.println("No historical PFCF data available for ticker: " + ticker);
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String date = jsonObject.getString("date");
                    double pfcf = jsonObject.getDouble("priceToFreeCashFlowsRatio");
                    pfcfRatios.add(new RatioData(date, pfcf));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pfcfRatios;
    }

    // New method to fetch historical debtToEquityRatio
    public static List<RatioData> fetchHistoricalDebtToEquity(String ticker) {
        List<RatioData> debtToEquityRatios = new ArrayList<>();
        try {
            URL url = new URL("https://financialmodelingprep.com/api/v3/ratios/" + ticker + "?period=annual&apikey=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("Debt to Equity API Response Code: " + responseCode);  // Debugging statement

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            conn.disconnect();

            System.out.println("Debt to Equity API Response: " + content.toString());  // Debugging statement

            JSONArray jsonArray = new JSONArray(content.toString());
            if (jsonArray.length() == 0) {
                System.out.println("No historical Debt to Equity data available for ticker: " + ticker);
            } else {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String date = jsonObject.getString("date");
                    double debtToEquity = jsonObject.getDouble("debtEquityRatio");
                    debtToEquityRatios.add(new RatioData(date, debtToEquity));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return debtToEquityRatios;
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