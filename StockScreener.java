package afin.jstocks;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
    private JTextField peRatioMinField;
    private JTextField peRatioMaxField;
    private JTextField pbRatioMinField;
    private JTextField pbRatioMaxField;
    private JTextField payoutRatioMinField;
    private JTextField payoutRatioMaxField;
    private JTextField marketCountryField;
    private JTable resultTable;
    private DefaultTableModel tableModel;

    private static final String API_KEY = "eb7366217370656d66a56a057b8511b0";

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Stock Screener");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 600); // Adjust the frame size

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        int row = 0;

        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("Market Cap Min:"), constraints);
        marketCapMinField = new JTextField();
        constraints.gridx = 1;
        panel.add(marketCapMinField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("Market Cap Max:"), constraints);
        marketCapMaxField = new JTextField();
        constraints.gridx = 1;
        panel.add(marketCapMaxField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("Dividend Yield Min:"), constraints);
        dividendYieldMinField = new JTextField();
        constraints.gridx = 1;
        panel.add(dividendYieldMinField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("Dividend Yield Max:"), constraints);
        dividendYieldMaxField = new JTextField();
        constraints.gridx = 1;
        panel.add(dividendYieldMaxField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("PEG Ratio Min:"), constraints);
        pegRatioMinField = new JTextField();
        constraints.gridx = 1;
        panel.add(pegRatioMinField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("PEG Ratio Max:"), constraints);
        pegRatioMaxField = new JTextField();
        constraints.gridx = 1;
        panel.add(pegRatioMaxField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("P/E Ratio Min:"), constraints);
        peRatioMinField = new JTextField();
        constraints.gridx = 1;
        panel.add(peRatioMinField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("P/E Ratio Max:"), constraints);
        peRatioMaxField = new JTextField();
        constraints.gridx = 1;
        panel.add(peRatioMaxField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("P/B Ratio Min:"), constraints);
        pbRatioMinField = new JTextField();
        constraints.gridx = 1;
        panel.add(pbRatioMinField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("P/B Ratio Max:"), constraints);
        pbRatioMaxField = new JTextField();
        constraints.gridx = 1;
        panel.add(pbRatioMaxField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("Payout Ratio Min:"), constraints);
        payoutRatioMinField = new JTextField();
        constraints.gridx = 1;
        panel.add(payoutRatioMinField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("Payout Ratio Max:"), constraints);
        payoutRatioMaxField = new JTextField();
        constraints.gridx = 1;
        panel.add(payoutRatioMaxField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        panel.add(new JLabel("Market Country:"), constraints);
        marketCountryField = new JTextField();
        constraints.gridx = 1;
        panel.add(marketCountryField, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchStocks();
            }
        });
        panel.add(searchButton, constraints);

        row++;
        tableModel = new DefaultTableModel(new String[]{"Symbol", "Company Name", "PE", "PB", "PEG", "Dividend Yield"}, 0);
        resultTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, constraints);

        row++;
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weighty = 0;
        JButton overviewButton = new JButton("Return to Overview");
        overviewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                Main.showOverview();
            }
        });
        panel.add(overviewButton, constraints);

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
        String peRatioMin = peRatioMinField.getText();
        String peRatioMax = peRatioMaxField.getText();
        String pbRatioMin = pbRatioMinField.getText();
        String pbRatioMax = pbRatioMaxField.getText();
        String payoutRatioMin = payoutRatioMinField.getText();
        String payoutRatioMax = payoutRatioMaxField.getText();
        String marketCountry = marketCountryField.getText();

        List<JSONObject> filteredStocks = fetchFilteredStocks(marketCapMin, marketCapMax, dividendYieldMin, dividendYieldMax, pegRatioMin, pegRatioMax, peRatioMin, peRatioMax, pbRatioMin, pbRatioMax, payoutRatioMin, payoutRatioMax, marketCountry);

        tableModel.setRowCount(0); // Clear existing rows
        for (JSONObject stock : filteredStocks) {
            tableModel.addRow(new Object[]{
                stock.getString("symbol"),
                stock.getString("companyName"),
                stock.getDouble("peRatio"),
                stock.getDouble("pbRatio"),
                stock.getDouble("pegRatio"),
                stock.getDouble("dividendYield")
            });
        }
    }

    private List<JSONObject> fetchFilteredStocks(String marketCapMin, String marketCapMax, String dividendYieldMin, String dividendYieldMax, String pegRatioMin, String pegRatioMax, String peRatioMin, String peRatioMax, String pbRatioMin, String pbRatioMax, String payoutRatioMin, String payoutRatioMax, String marketCountry) {
        List<JSONObject> stocks = new ArrayList<>();
        try {
            String urlString = String.format("https://financialmodelingprep.com/api/v3/stock-screener?marketCapMoreThan=%s&marketCapLessThan=%s&dividendMoreThan=%s&dividendLessThan=%s&pegMoreThan=%s&pegLessThan=%s&priceEarningsMoreThan=%s&priceEarningsLessThan=%s&priceToBookMoreThan=%s&priceToBookLessThan=%s&payoutMoreThan=%s&payoutLessThan=%s&country=%s&apikey=%s",
                    marketCapMin, marketCapMax, dividendYieldMin, dividendYieldMax, pegRatioMin, pegRatioMax, peRatioMin, peRatioMax, pbRatioMin, pbRatioMax, payoutRatioMin, payoutRatioMax, marketCountry, API_KEY);
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
                stocks.add(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stocks;
    }
}