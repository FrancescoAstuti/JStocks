package afin.jstocks;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.Properties;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;

public class Watchlist {
    private JTable watchlistTable;
    private DefaultTableModel tableModel;
    private static final String API_KEY = "eb7366217370656d66a56a057b8511b0";
    private static final String COLUMN_SETTINGS_FILE = "column_settings.properties";
    private JPanel columnControlPanel;

    private String[] getDynamicColumnNames() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] columnNames = new String[5];
        for (int i = 0; i < 5; i++) {
            columnNames[i] = "EPS " + (currentYear + i);
        }
        return columnNames;
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Watchlist");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        String[] dynamicColumnNames = getDynamicColumnNames();
        tableModel = new DefaultTableModel(new Object[]{
            "Name", "Ticker", "Price", "PE TTM", "PB TTM", "Div. yield", 
            "Payout Ratio", "Graham Number", "PB Avg", "PE Avg", 
            "EPS TTM",
            dynamicColumnNames[0], dynamicColumnNames[1], dynamicColumnNames[2], 
            dynamicColumnNames[3], dynamicColumnNames[4], "Debt to Equity",
            "PEG (3Y)"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8 || column == 9;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2: case 3: case 4: case 5: case 6: case 7: 
                    case 8: case 9: case 10: case 11: case 12: case 13: 
                    case 14: case 15: case 16: case 17:
                        return Double.class;
                    default:
                        return String.class;
                }
            }
        };

        watchlistTable = new JTable(tableModel);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        setupTableSorter(sorter);
        watchlistTable.setRowSorter(sorter);

        watchlistTable.getTableHeader().setReorderingAllowed(true);
        watchlistTable.getColumnModel().getColumn(8).setCellRenderer(new CustomCellRenderer());
        watchlistTable.getColumnModel().getColumn(9).setCellRenderer(new CustomCellRenderer());

        JScrollPane scrollPane = new JScrollPane(watchlistTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setupColumnControlPanel();
        mainPanel.add(columnControlPanel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel();
        setupButtonPanel(buttonPanel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveColumnSettings();
                saveWatchlist();
            }
        });

        // Load settings and data AFTER the table is fully initialized
        SwingUtilities.invokeLater(() -> {
            loadColumnSettings();
            loadWatchlist();
        });

        frame.setVisible(true);
    }
    private void setupTableSorter(TableRowSorter<DefaultTableModel> sorter) {
        for (int i = 2; i <= 17; i++) {
            final int column = i;
            sorter.setComparator(column, Comparator.comparingDouble(o -> (Double) o));
        }
    }

    private void setupColumnControlPanel() {
        columnControlPanel = new JPanel();
        columnControlPanel.setLayout(new BoxLayout(columnControlPanel, BoxLayout.Y_AXIS));

        TableColumnModel columnModel = watchlistTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            JCheckBox checkBox = new JCheckBox(tableModel.getColumnName(i), true);
            final String columnName = tableModel.getColumnName(i);
            checkBox.addActionListener(e -> toggleColumnVisibility(columnName, checkBox.isSelected()));
            columnControlPanel.add(checkBox);
        }
    }

    private void setupButtonPanel(JPanel buttonPanel) {
        JButton addButton = new JButton("Add Stock");
        JButton deleteButton = new JButton("Delete Stock");
        JButton refreshButton = new JButton("Refresh");

        addButton.addActionListener(e -> addStock());
        deleteButton.addActionListener(e -> deleteStock());
        refreshButton.addActionListener(e -> refreshWatchlist());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
    }

    private void toggleColumnVisibility(String columnName, boolean visible) {
        TableColumnModel columnModel = watchlistTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            if (column.getHeaderValue().equals(columnName)) {
                if (visible) {
                    column.setMinWidth(15);
                    column.setMaxWidth(Integer.MAX_VALUE);
                    column.setPreferredWidth(75);
                } else {
                    column.setMinWidth(0);
                    column.setMaxWidth(0);
                    column.setPreferredWidth(0);
                }
                break;
            }
        }
    }

    private void loadWatchlist() {
        File file = new File("watchlist.json");
        System.out.println("Attempting to load watchlist from: " + file.getAbsolutePath());
        
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Scanner scanner = new Scanner(reader);
                String json = scanner.useDelimiter("\\Z").next();
                scanner.close();

                System.out.println("Loading watchlist data...");
                JSONArray jsonArray = new JSONArray(json);
                System.out.println("Found " + jsonArray.length() + " stocks");

                // Clear existing table data
                while (tableModel.getRowCount() > 0) {
                    tableModel.removeRow(0);
                }

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Object[] rowData = new Object[]{
                        jsonObject.optString("name", ""),
                        jsonObject.optString("ticker", "").toUpperCase(),
                        jsonObject.optDouble("price", 0.0),
                        jsonObject.optDouble("peTtm", 0.0),
                        jsonObject.optDouble("pbTtm", 0.0),
                        jsonObject.optDouble("dividendYield", 0.0),
                        jsonObject.optDouble("payoutRatio", 0.0),
                        jsonObject.optDouble("grahamNumber", 0.0),
                        jsonObject.optDouble("pbAvg", 0.0),
                        jsonObject.optDouble("peAvg", 0.0),
                        jsonObject.optDouble("epsTtm", 0.0),
                        jsonObject.optDouble("epsCurrentYear", 0.0),
                        jsonObject.optDouble("epsNextYear", 0.0),
                        jsonObject.optDouble("epsYear3", 0.0),
                        jsonObject.optDouble("epsYear4", 0.0),
                        jsonObject.optDouble("epsYear5", 0.0),
                        jsonObject.optDouble("debtToEquity", 0.0),
                        jsonObject.optDouble("peg3Year", 0.0)
                    };
                    tableModel.addRow(rowData);
                    System.out.println("Added stock: " + jsonObject.optString("ticker", ""));
                }
                System.out.println("Watchlist loading completed");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error loading watchlist: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error parsing watchlist data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("Watchlist file not found: " + file.getAbsolutePath());
        }
    }
    private void saveWatchlist() {
        JSONArray jsonArray = new JSONArray();
        File file = new File("watchlist.json");

        // Add new data to the JSON array
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
            jsonObject.put("pbAvg", tableModel.getValueAt(i, 8));
            jsonObject.put("peAvg", tableModel.getValueAt(i, 9));
            jsonObject.put("epsTtm", tableModel.getValueAt(i, 10));
            jsonObject.put("epsCurrentYear", tableModel.getValueAt(i, 11));
            jsonObject.put("epsNextYear", tableModel.getValueAt(i, 12));
            jsonObject.put("epsYear3", tableModel.getValueAt(i, 13));
            jsonObject.put("epsYear4", tableModel.getValueAt(i, 14));
            jsonObject.put("epsYear5", tableModel.getValueAt(i, 15));
            jsonObject.put("debtToEquity", tableModel.getValueAt(i, 16));
            jsonObject.put("peg3Year", tableModel.getValueAt(i, 17));
            jsonArray.put(jsonObject);
        }

        try (FileWriter fileWriter = new FileWriter("watchlist.json")) {
            fileWriter.write(jsonArray.toString());
            fileWriter.flush();
            System.out.println("Watchlist saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error saving watchlist: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveColumnSettings() {
        Properties properties = new Properties();
        TableColumnModel columnModel = watchlistTable.getColumnModel();

        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            String columnName = column.getHeaderValue().toString();
            properties.setProperty("column" + i + ".name", columnName);
            properties.setProperty("column" + i + ".index", String.valueOf(columnModel.getColumnIndex(columnName)));
            properties.setProperty("column" + i + ".visible", String.valueOf(column.getMaxWidth() != 0));
        }

        try (FileOutputStream out = new FileOutputStream(COLUMN_SETTINGS_FILE)) {
            properties.store(out, "Column Order and Visibility Settings");
            System.out.println("Column settings saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error saving column settings: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadColumnSettings() {
        Properties properties = new Properties();
        File settingsFile = new File(COLUMN_SETTINGS_FILE);

        if (settingsFile.exists()) {
            try (FileInputStream in = new FileInputStream(settingsFile)) {
                properties.load(in);

                TableColumnModel columnModel = watchlistTable.getColumnModel();
                for (int i = 0; i < columnModel.getColumnCount(); i++) {
                    String columnName = properties.getProperty("column" + i + ".name");
                    if (columnName != null) {
                        int columnIndex = Integer.parseInt(properties.getProperty("column" + i + ".index"));
                        boolean isVisible = Boolean.parseBoolean(properties.getProperty("column" + i + ".visible"));

                        TableColumn column = columnModel.getColumn(columnModel.getColumnIndex(columnName));
                        columnModel.moveColumn(columnModel.getColumnIndex(columnName), columnIndex);
                        toggleColumnVisibility(columnName, isVisible);

                        for (Component comp : columnControlPanel.getComponents()) {
                            if (comp instanceof JCheckBox) {
                                JCheckBox checkBox = (JCheckBox) comp;
                                if (checkBox.getText().equals(columnName)) {
                                    checkBox.setSelected(isVisible);
                                    break;
                                }
                            }
                        }
                    }
                }
                System.out.println("Column settings loaded successfully");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error loading column settings: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void addStock() {
        String ticker = JOptionPane.showInputDialog("Enter Stock Ticker:");
        if (ticker != null && !ticker.trim().isEmpty()) {
            ticker = ticker.toUpperCase();
            try {
                JSONObject stockData = fetchStockData(ticker);
                JSONObject epsEstimates = Estimates.fetchEpsEstimates(ticker);

                if (stockData != null) {
                    String name = stockData.getString("name");
                    double price = round(stockData.getDouble("price"), 2);
                    JSONObject ratios = fetchStockRatios(ticker);

                    double peTtm = round(ratios.optDouble("peRatioTTM", 0.0), 2);
                    double pbTtm = round(ratios.optDouble("pbRatioTTM", 0.0), 2);
                    double epsTtm = peTtm != 0 ? round((1 / peTtm) * price, 2) : 0.0;
                    double dividendYield = round(ratios.optDouble("dividendYieldTTM", 0.0), 2);
                    double payoutRatio = round(ratios.optDouble("payoutRatioTTM", 0.0), 2);
                    double grahamNumber = round(ratios.optDouble("grahamNumberTTM", 0.0), 2);
                    double debtToEquity = round(ratios.optDouble("debtToEquityTTM", 0.0), 2);

                    double epsCurrentYear = epsEstimates != null ? round(epsEstimates.optDouble("eps0", 0.0), 2) : 0.0;
                    double epsYear3 = epsEstimates != null ? round(epsEstimates.optDouble("eps2", 0.0), 2) : 0.0;

                    double pbAvg = fetchAveragePB(ticker);
                    double peAvg = fetchAveragePE(ticker);
                    double peg3Year = calculatePEG3Year(epsCurrentYear, epsYear3);

                    tableModel.addRow(new Object[]{
                        name, ticker, price, peTtm, pbTtm, dividendYield, payoutRatio,
                        grahamNumber, pbAvg, peAvg, epsTtm,
                        epsCurrentYear, 0.0, epsYear3, 0.0, 0.0, debtToEquity, peg3Year
                    });

                    saveWatchlist();
                    System.out.println("Stock added successfully: " + ticker);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error adding stock: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshWatchlist() {
        System.out.println("Starting watchlist refresh...");
        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int modelRow = watchlistTable.convertRowIndexToModel(i);
            String ticker = (String) tableModel.getValueAt(modelRow, 1);
            try {
                JSONObject stockData = fetchStockData(ticker);
                JSONObject ratios = fetchStockRatios(ticker);
                JSONObject epsEstimates = Estimates.fetchEpsEstimates(ticker);

                if (stockData != null) {
                    double price = round(stockData.getDouble("price"), 2);
                    double peTtm = round(ratios.optDouble("peRatioTTM", 0.0), 2);
                    double pbTtm = round(ratios.optDouble("pbRatioTTM", 0.0), 2);
                    double epsTtm = peTtm != 0 ? round((1 / peTtm) * price, 2) : 0.0;
                    double dividendYield = round(ratios.optDouble("dividendYieldTTM", 0.0), 2);
                    double payoutRatio = round(ratios.optDouble("payoutRatioTTM", 0.0), 2);
                    double grahamNumber = round(ratios.optDouble("grahamNumberTTM", 0.0), 2);
                    double debtToEquity = round(ratios.optDouble("debtToEquityTTM", 0.0), 2);

                    double epsCurrentYear = epsEstimates != null ? round(epsEstimates.optDouble("eps0", 0.0), 2) : 0.0;
                    double epsYear3 = epsEstimates != null ? round(epsEstimates.optDouble("eps2", 0.0), 2) : 0.0;
                    double peg3Year = calculatePEG3Year(epsCurrentYear, epsYear3);

                    tableModel.setValueAt(price, modelRow, 2);
                    tableModel.setValueAt(peTtm, modelRow, 3);
                    tableModel.setValueAt(pbTtm, modelRow, 4);
                    tableModel.setValueAt(dividendYield, modelRow, 5);
                    tableModel.setValueAt(payoutRatio, modelRow, 6);
                    tableModel.setValueAt(grahamNumber, modelRow, 7);
                    tableModel.setValueAt(epsTtm, modelRow, 10);
                    tableModel.setValueAt(epsCurrentYear, modelRow, 11);
                    tableModel.setValueAt(epsYear3, modelRow, 13);
                    tableModel.setValueAt(debtToEquity, modelRow, 16);
                    tableModel.setValueAt(peg3Year, modelRow, 17);

                    System.out.println("Refreshed stock data: " + ticker);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error refreshing " + ticker + ": " + e.getMessage());
            }
        }
        saveWatchlist();
        System.out.println("Watchlist refresh completed");
    }
    private void deleteStock() {
        int selectedRow = watchlistTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = watchlistTable.convertRowIndexToModel(selectedRow);
            String ticker = (String) tableModel.getValueAt(modelRow, 1);
            tableModel.removeRow(modelRow);
            saveWatchlist();
            System.out.println("Stock deleted successfully: " + ticker);
        }
    }

    private double calculatePEG3Year(double epsCurrentYear, double epsYear3) {
        if (epsCurrentYear == 0) return 0;
        double growthRate = 100 * (epsYear3 - epsCurrentYear) / epsCurrentYear;
        return round(growthRate, 2);
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
    
    private double fetchAveragePB(String ticker) {
    // This method fetches the average P/B ratio for the stock
    String urlString = String.format("https://financialmodelingprep.com/api/v3/key-metrics/%s?period=annual&limit=5&apikey=%s", ticker, API_KEY);
    HttpURLConnection connection = null;
    double sum = 0;
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
            for (int i = 0; i < data.length(); i++) {
                JSONObject metrics = data.getJSONObject(i);
                double pbRatio = metrics.optDouble("pbRatio", 0.0);
                if (pbRatio > 0) {
                    sum += pbRatio;
                    count++;
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (connection != null) {
            connection.disconnect();
        }
    }

    return count > 0 ? round(sum / count, 2) : 0.0;
}

private double fetchAveragePE(String ticker) {
    // This method fetches the average P/E ratio for the stock
    String urlString = String.format("https://financialmodelingprep.com/api/v3/key-metrics/%s?period=annual&limit=5&apikey=%s", ticker, API_KEY);
    HttpURLConnection connection = null;
    double sum = 0;
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
            for (int i = 0; i < data.length(); i++) {
                JSONObject metrics = data.getJSONObject(i);
                double peRatio = metrics.optDouble("peRatio", 0.0);
                if (peRatio > 0) {
                    sum += peRatio;
                    count++;
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (connection != null) {
            connection.disconnect();
        }
    }

    return count > 0 ? round(sum / count, 2) : 0.0;
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Watchlist().createAndShowGUI());
    }

    static class CustomCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            if (value instanceof Double && (Double) value == 0.0) {
                cell.setBackground(Color.YELLOW);
            } else {
                cell.setBackground(Color.WHITE);
            }
            return cell;
        }
    }
}