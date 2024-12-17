package afin.jstocks;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
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
    private static final String COLUMN_ORDER_FILE = "column_order.dat";
    private static final String WATCHLIST_FILE = "watchlist.json";

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Watchlist");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{
            "Name", "Ticker", "Price", "PE TTM", "PB TTM", "Dividend Yield", "Payout Ratio", "Graham Number",
            "Analyst EPS Forecast", "Analyst Mean Price Target", "PEG Ratio"
        }, 0);

        watchlistTable = new JTable(tableModel);
        watchlistTable.setRowHeight(25); // Adjust row height
        watchlistTable.setAutoCreateRowSorter(true); // Enable column sorting
        watchlistTable.getTableHeader().setReorderingAllowed(true); // Enable column reordering

        loadColumnOrder(); // Load saved column order

        JScrollPane scrollPane = new JScrollPane(watchlistTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Stock");
        JButton deleteButton = new JButton("Delete Stock");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> addStock());
        deleteButton.addActionListener(e -> deleteStock());
        refreshButton.addActionListener(e -> refreshWatchlist());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveColumnOrder(); // Save column order on close
            }
        });

        loadWatchlist(); // Load watchlist data
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
                    double analystEpsForecast = round(ratios.optDouble("analystEpsForecast", 0.0), 2);
                    double analystMeanPriceTarget = round(ratios.optDouble("analystMeanPriceTarget", 0.0), 2);
                    double pegRatio = round(ratios.optDouble("pegRatioTTM", 0.0), 2);

                    tableModel.addRow(new Object[]{
                        name, ticker, price, peTtm, pbTtm, dividendYield, payoutRatio, grahamNumber,
                        analystEpsForecast, analystMeanPriceTarget, pegRatio
                    });
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
            String ticker = (String) tableModel.getValueAt(i, 1); // Ticker is in column 1
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
                    double analystEpsForecast = round(ratios.optDouble("analystEpsForecast", 0.0), 2);
                    double analystMeanPriceTarget = round(ratios.optDouble("analystMeanPriceTarget", 0.0), 2);
                    double pegRatio = round(ratios.optDouble("pegRatioTTM", 0.0), 2);

                    tableModel.setValueAt(name, i, 0);
                    tableModel.setValueAt(price, i, 2);
                    tableModel.setValueAt(peTtm, i, 3);
                    tableModel.setValueAt(pbTtm, i, 4);
                    tableModel.setValueAt(dividendYield, i, 5);
                    tableModel.setValueAt(payoutRatio, i, 6);
                    tableModel.setValueAt(grahamNumber, i, 7);
                    tableModel.setValueAt(analystEpsForecast, i, 8);
                    tableModel.setValueAt(analystMeanPriceTarget, i, 9);
                    tableModel.setValueAt(pegRatio, i, 10);
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
            jsonObject.put("analystEpsForecast", tableModel.getValueAt(i, 8));
            jsonObject.put("analystMeanPriceTarget", tableModel.getValueAt(i, 9));
            jsonObject.put("pegRatio", tableModel.getValueAt(i, 10));
            jsonArray.put(jsonObject);
        }

        try (FileWriter file = new FileWriter(WATCHLIST_FILE)) {
            file.write(jsonArray.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadWatchlist() {
        File file = new File(WATCHLIST_FILE);
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
                        jsonObject.optDouble("analystEpsForecast", 0.0),
                        jsonObject.optDouble("analystMeanPriceTarget", 0.0),
                        jsonObject.optDouble("pegRatio", 0.0)
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveColumnOrder() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(COLUMN_ORDER_FILE))) {
            TableColumnModel columnModel = watchlistTable.getColumnModel();
            int columnCount = columnModel.getColumnCount();
            int[] columnOrder = new int[columnCount];
            int[] columnWidths = new int[columnCount];

            for (int i = 0; i < columnCount; i++) {
                columnOrder[i] = columnModel.getColumn(i).getModelIndex();
                columnWidths[i] = columnModel.getColumn(i).getWidth();
            }

            out.writeObject(columnOrder);
            out.writeObject(columnWidths);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadColumnOrder() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(COLUMN_ORDER_FILE))) {
            int[] columnOrder = (int[]) in.readObject();
            int[] columnWidths = (int[]) in.readObject();

            TableColumnModel columnModel = watchlistTable.getColumnModel();
            int columnCount = columnModel.getColumnCount();
            
            // Adjust column order if saved order does not match current column count
            if (columnOrder.length != columnCount) {
                columnOrder = new int[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    columnOrder[i] = i;
                }
            }

            TableColumn[] columns = new TableColumn[columnCount];
            for (int i = 0; i < columnCount; i++) {
                columns[i] = columnModel.getColumn(i);
            }

            for (int i = 0; i < columnCount; i++) {
                columnModel.moveColumn(columnModel.getColumnIndex(columns[columnOrder[i]].getHeaderValue()), i);
                columnModel.getColumn(i).setPreferredWidth(columnWidths[i]);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}