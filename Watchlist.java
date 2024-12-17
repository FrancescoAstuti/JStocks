package afin.jstocks;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class Watchlist {
    private JTable watchlistTable;
    private DefaultTableModel tableModel;
    private static final String API_KEY = "eb7366217370656d66a56a057b8511b0";

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Watchlist");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{
            "Name", "Ticker", "Price", "PE TTM", "PB TTM", "Dividend Yield", "Payout Ratio", "Graham Number", "Avg PB Ratio (20 Years)"
        }, 0);
        watchlistTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(watchlistTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Stock");
        JButton deleteButton = new JButton("Delete Stock");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addStock();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteStock();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshWatchlist();
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        loadWatchlist();
    }

    private void addStock() {
        String ticker = JOptionPane.showInputDialog("Enter Stock Ticker:");
        if (ticker != null) {
            try {
                JSONObject stockData = fetchStockData(ticker);
                if (stockData != null) {
                    String name = stockData.getString("name");
                    double price = round(stockData.getDouble("price"), 2);
                    JSONObject ratios = fetchStockRatios(ticker);
                    double peTtm = round(ratios.optDouble("peRatioTTM", 0.0), 2);
                    double pbTtm = round(ratios.optDouble("pbRatioTTM", 0.0), 2);
                    double dividendYield = round(ratios.optDouble("dividendYieldTTM", 0.0), 2);
                    double payoutRatio = round(ratios.optDouble("payoutRatioTTM", 0.0), 2);
                    double grahamNumber = round(ratios.optDouble("grahamNumberTTM", 0.0), 2);
                    double avgPbRatio = fetchAvgPbRatio(ticker);
                    tableModel.addRow(new Object[]{name, ticker, price, peTtm, pbTtm, dividendYield, payoutRatio, grahamNumber, avgPbRatio});
                    saveWatchlist();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to fetch stock data.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error fetching stock data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteStock() {
        int selectedRow = watchlistTable.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
            saveWatchlist();
        } else {
            JOptionPane.showMessageDialog(null, "No stock selected. Please select a stock to delete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshWatchlist() {
        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String ticker = (String) tableModel.getValueAt(i, 1);
            try {
                JSONObject stockData = fetchStockData(ticker);
                if (stockData != null) {
                    String name = stockData.getString("name");
                    double price = round(stockData.getDouble("price"), 2);
                    JSONObject ratios = fetchStockRatios(ticker);
                    double peTtm = round(ratios.optDouble("peRatioTTM", 0.0), 2);
                    double pbTtm = round(ratios.optDouble("pbRatioTTM", 0.0), 2);
                    double dividendYield = round(ratios.optDouble("dividendYieldTTM", 0.0), 2);
                    double payoutRatio = round(ratios.optDouble("payoutRatioTTM", 0.0), 2);
                    double grahamNumber = round(ratios.optDouble("grahamNumberTTM", 0.0), 2);
                    double avgPbRatio = fetchAvgPbRatio(ticker);
                    tableModel.setValueAt(name, i, 0);
                    tableModel.setValueAt(price, i, 2);
                    tableModel.setValueAt(peTtm, i, 3);
                    tableModel.setValueAt(pbTtm, i, 4);
                    tableModel.setValueAt(dividendYield, i, 5);
                    tableModel.setValueAt(payoutRatio, i, 6);
                    tableModel.setValueAt(grahamNumber, i, 7);
                    tableModel.setValueAt(avgPbRatio, i, 8);
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to fetch stock data for " + ticker, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error fetching stock data for " + ticker, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        saveWatchlist();
    }

    private JSONObject fetchStockData(String ticker) {
        String urlString = String.format("https://financialmodelingprep.com/api/v3/quote/%s?apikey=%s", ticker, API_KEY);
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Scanner scanner = new Scanner(url.openStream());
                String response = scanner.useDelimiter("\\Z").next();
                scanner.close();

                JSONArray data = new JSONArray(response);
                if (data.length() > 0) {
                    return data.getJSONObject(0);
                }
            } else {
                throw new IOException("Failed to get valid response from API. Response Code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private JSONObject fetchStockRatios(String ticker) {
        String urlString = String.format("https://financialmodelingprep.com/api/v3/key-metrics-ttm/%s?apikey=%s", ticker, API_KEY);
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Scanner scanner = new Scanner(url.openStream());
                String response = scanner.useDelimiter("\\Z").next();
                scanner.close();

                JSONArray data = new JSONArray(response);
                if (data.length() > 0) {
                    return data.getJSONObject(0);
                }
            } else {
                throw new IOException("Failed to get valid response from API. Response Code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private double fetchAvgPbRatio(String ticker) {
        String urlString = String.format("https://financialmodelingprep.com/api/v3/historical-key-metrics/%s?apikey=%s", ticker, API_KEY);
        HttpURLConnection connection = null;
        double sumPbRatio = 0.0;
        int count = 0;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                Scanner scanner = new Scanner(url.openStream());
                String response = scanner.useDelimiter("\\Z").next();
                scanner.close();

                JSONArray data = new JSONArray(response);
                for (int i = 0; i < data.length() && i < 20; i++) { // Fetch last 20 years data
                    JSONObject yearData = data.getJSONObject(i);
                    if (yearData.has("pbRatio")) {
                        sumPbRatio += yearData.getDouble("pbRatio");
                        count++;
                    }
                }
            } else {
                throw new IOException("Failed to get valid response from API. Response Code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return count > 0 ? round(sumPbRatio / count, 2) : 0.0;
    }

    private void saveWatchlist() {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", tableModel.getValueAt(i, 0));
            jsonObject.put("ticker", tableModel.getValueAt(i, 1));
            jsonObject.put("price", tableModel.getValueAt(i, 2));
            jsonObject.put("peTtm", tableModel.getValueAt(i, 3));
            jsonObject.put("pbTtm", tableModel.getValueAt(i, 4));
            jsonObject.put("dividendYield", tableModel.getValueAt(i, 5));
            jsonObject.put("payoutRatio", tableModel.getValueAt(i, 6));
            jsonObject.put("grahamNumber", tableModel.getValueAt(i, 7));
            jsonObject.put("avgPbRatio", tableModel.getValueAt(i, 8));
            jsonArray.put(jsonObject);
        }

        try (FileWriter file = new FileWriter("watchlist.json")) {
            file.write(jsonArray.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadWatchlist() {
        File file = new File("watchlist.json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Scanner scanner = new Scanner(reader);
                String json = scanner.useDelimiter("\\Z").next();
                scanner.close();

                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    tableModel.addRow(new Object[]{
                        jsonObject.optString("name", ""),
                        jsonObject.optString("ticker", ""),
                        jsonObject.optDouble("price", 0.0),
                        jsonObject.optDouble("peTtm", 0.0),
                        jsonObject.optDouble("pbTtm", 0.0),
                        jsonObject.optDouble("dividendYield", 0.0),
                        jsonObject.optDouble("payoutRatio", 0.0),
                        jsonObject.optDouble("grahamNumber", 0.0),
                        jsonObject.optDouble("avgPbRatio", 0.0)
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Watchlist().createAndShowGUI();
            }
        });
    }
}
