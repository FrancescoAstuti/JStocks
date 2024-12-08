package afin.jstocks;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class StockScreener {
    private JTextField marketCapMinField;
    private JTextField marketCapMaxField;
    private JTextField dividendYieldMinField;
    private JTextField dividendYieldMaxField;
    private JTextField pegRatioMinField;
    private JTextField pegRatioMaxField;
    private JTextArea resultArea;

    private static final String API_KEY = "eb7366217370656d66a56a057b8511b0";

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Stock Screener");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel panel = new JPanel(new GridLayout(8, 2));

        panel.add(new JLabel("Market Cap Min:"));
        marketCapMinField = new JTextField();
        panel.add(marketCapMinField);

        panel.add(new JLabel("Market Cap Max:"));
        marketCapMaxField = new JTextField();
        panel.add(marketCapMaxField);

        panel.add(new JLabel("Dividend Yield Min:"));
        dividendYieldMinField = new JTextField();
        panel.add(dividendYieldMinField);

        panel.add(new JLabel("Dividend Yield Max:"));
        dividendYieldMaxField = new JTextField();
        panel.add(dividendYieldMaxField);

        panel.add(new JLabel("PEG Ratio Min:"));
        pegRatioMinField = new JTextField();
        panel.add(pegRatioMinField);

        panel.add(new JLabel("PEG Ratio Max:"));
        pegRatioMaxField = new JTextField();
        panel.add(pegRatioMaxField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchStocks();
            }
        });
        panel.add(searchButton);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        panel.add(scrollPane);

        JButton overviewButton = new JButton("Return to Overview");
        overviewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                Main.showOverview();
            }
        });
        panel.add(overviewButton);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void searchStocks() {
        String marketCapMin = marketCapMinField.getText();
        String marketCapMax = marketCapMaxField.getText();
        String dividendYieldMin = dividendYieldMinField.getText();
        String dividendYieldMax = dividendYieldMaxField.getText();
        String pegRatioMin = pegRatioMinField.getText();
        String pegRatioMax = pegRatioMaxField.getText();

        List<String> filteredStocks = fetchFilteredStocks(marketCapMin, marketCapMax, dividendYieldMin, dividendYieldMax, pegRatioMin, pegRatioMax);

        resultArea.setText("");
        for (String stock : filteredStocks) {
            resultArea.append(stock + "\n");
        }
    }

    private List<String> fetchFilteredStocks(String marketCapMin, String marketCapMax, String dividendYieldMin, String dividendYieldMax, String pegRatioMin, String pegRatioMax) {
        List<String> stocks = new ArrayList<>();
        try {
            String urlString = String.format("https://financialmodelingprep.com/api/v3/stock-screener?marketCapMoreThan=%s&marketCapLessThan=%s&dividendMoreThan=%s&dividendLessThan=%s&pegMoreThan=%s&pegLessThan=%s&apikey=%s",
                    marketCapMin, marketCapMax, dividendYieldMin, dividendYieldMax, pegRatioMin, pegRatioMax, API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(content.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                stocks.add(jsonObject.getString("symbol"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stocks;
    }
}