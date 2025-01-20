package afin.jstocks;

import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
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
        String[] columnNames = new String[3];
        for (int i = 0; i < 3; i++) {
            columnNames[i] = "EPS " + (currentYear + i);
        }
        return columnNames;
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Watchlist");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(1000, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        String[] dynamicColumnNames = getDynamicColumnNames();
        tableModel = new DefaultTableModel(new Object[]{
            "Name", "Ticker", "Price", "PE TTM", "PB TTM", "Div. yield",
            "Payout Ratio", "Graham Number", "PB Avg", "PE Avg",
            "EPS TTM", "ROE TTM", "A-Score",
            dynamicColumnNames[0], dynamicColumnNames[1], dynamicColumnNames[2],
            "Debt to Equity", "PEG TTM", "Current Ratio", "Quick Ratio"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Allow editing certain columns if desired (8, 9 for example).
                // Return false if no columns should be editable.
                return column == 8 || column == 9;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
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

        // Handle window closing
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int confirm = JOptionPane.showOptionDialog(
                    frame,
                    "Are you sure you want to close this window?",
                    "Close Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, null, null);
                if (confirm == JOptionPane.YES_OPTION) {
                    saveColumnSettings();
                    saveWatchlist();
                    frame.dispose();
                }
            }
        });

        // After creating the UI, load settings asynchronously
        SwingUtilities.invokeLater(() -> {
            loadColumnSettings();
            loadWatchlist();
            loadSortOrder(sorter);
        });

        frame.setVisible(true);
    }

    private void setupTableSorter(TableRowSorter<DefaultTableModel> sorter) {
        // Set comparators for numeric columns if needed
        for (int i = 2; i <= 16; i++) {
            final int column = i;
            sorter.setComparator(column, Comparator.comparingDouble(o -> (Double) o));
        }

        // Use RowSorterListener instead of propertyChangeListener
        sorter.addRowSorterListener(new RowSorterListener() {
            @Override
            public void sorterChanged(RowSorterEvent e) {
                if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                    saveSortOrder(sorter.getSortKeys());
                }
            }
        });
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

                while (tableModel.getRowCount() > 0) {
                    tableModel.removeRow(0);
                }

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Object debtToEquity;
                    if (jsonObject.opt("debtToEquity") instanceof String &&
                        jsonObject.optString("debtToEquity").equals("n/a")) {
                        debtToEquity = "n/a";
                    } else {
                        debtToEquity = jsonObject.optDouble("debtToEquity", 0.0);
                    }

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
                        jsonObject.optDouble("roeTtm", 0.0),
                        jsonObject.optDouble("aScore", 0.0),
                        jsonObject.optDouble("epsCurrentYear", 0.0),
                        jsonObject.optDouble("epsNextYear", 0.0),
                        jsonObject.optDouble("epsYear3", 0.0),
                        debtToEquity,
                        jsonObject.optDouble("pegTTM", 0.0),
                            
                        jsonObject.optDouble("currentRatio", 0.0),
                        jsonObject.optDouble("quickRatio",   0.0),
                        
                    };
                    tableModel.addRow(rowData);
                    System.out.println("Added stock: " + jsonObject.optString("ticker", "") +
                                       " with price: " + jsonObject.optDouble("price", 0.0));
                }
                System.out.println("Watchlist loading completed");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error loading watchlist: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("Watchlist file does not exist.");
        }
    }

    private void saveWatchlist() {
        System.out.println("Saving watchlist...");
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
            jsonObject.put("pbAvg", tableModel.getValueAt(i, 8));
            jsonObject.put("peAvg", tableModel.getValueAt(i, 9));
            jsonObject.put("epsTtm", tableModel.getValueAt(i, 10));
            jsonObject.put("roeTtm", tableModel.getValueAt(i, 11));
            jsonObject.put("aScore", tableModel.getValueAt(i, 12));
            jsonObject.put("epsCurrentYear", tableModel.getValueAt(i, 13));
            jsonObject.put("epsNextYear", tableModel.getValueAt(i, 14));
            jsonObject.put("epsYear3", tableModel.getValueAt(i, 15));
            jsonObject.put("debtToEquity", tableModel.getValueAt(i, 16).equals("n/a") ? "n/a" : tableModel.getValueAt(i, 16));
            jsonObject.put("pegTTM", tableModel.getValueAt(i, 17));
            jsonObject.put("currentRatio", tableModel.getValueAt(i, 18));
            jsonObject.put("quickRatio",   tableModel.getValueAt(i, 19));
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

    private void saveSortOrder(java.util.List<? extends RowSorter.SortKey> sortKeys) {
        Properties properties = new Properties();
        for (int i = 0; i < sortKeys.size(); i++) {
            RowSorter.SortKey sortKey = sortKeys.get(i);
            properties.setProperty("sortKey" + i + ".column", String.valueOf(sortKey.getColumn()));
            properties.setProperty("sortKey" + i + ".order", sortKey.getSortOrder().toString());
        }

        try (FileOutputStream out = new FileOutputStream("sort_order.properties")) {
            properties.store(out, "Table Sort Order");
            System.out.println("Sort order saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error saving sort order: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSortOrder(TableRowSorter<DefaultTableModel> sorter) {
        Properties properties = new Properties();
        File file = new File("sort_order.properties");

        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                properties.load(in);

                List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                for (int i = 0; ; i++) {
                    String columnStr = properties.getProperty("sortKey" + i + ".column");
                    String orderStr = properties.getProperty("sortKey" + i + ".order");
                    if (columnStr == null || orderStr == null) break;

                    int column = Integer.parseInt(columnStr);
                    SortOrder order = SortOrder.valueOf(orderStr);
                    sortKeys.add(new RowSorter.SortKey(column, order));
                }

                sorter.setSortKeys(sortKeys);
                System.out.println("Sort order loaded successfully");
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Error loading sort order: " + e.getMessage(),
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
                    double roeTtm = round(ratios.optDouble("roeTtm", 0.0), 2);
                    double dividendYield = round(ratios.optDouble("dividendYieldTTM", 0.0), 2);
                    double payoutRatio = round(ratios.optDouble("payoutRatioTTM", 0.0), 2);
                    double grahamNumber = round(ratios.optDouble("grahamNumberTTM", 0.0), 2);
                    Object debtToEquity = ratios.has("debtToEquityTTM") ? round(ratios.optDouble("debtToEquityTTM", 0.0), 2) : "n/a";

                    double epsCurrentYear = epsEstimates != null ? round(epsEstimates.optDouble("eps0", 0.0), 2) : 0.0;
                    double epsNextYear = epsEstimates != null ? round(epsEstimates.optDouble("eps1", 0.0), 2) : 0.0;
                    double epsYear3 = epsEstimates != null ? round(epsEstimates.optDouble("eps2", 0.0), 2) : 0.0;
                    double currentRatio = ratios != null ? round(ratios.optDouble("currentRatioTTM", 0.0), 2) : 0.0;
                    double quickRatio = ratios != null ? round(ratios.optDouble("quickRatioTTM", 0.0), 2) : 0.0;

                    double pbAvg = fetchAveragePB(ticker);
                    double peAvg = fetchAveragePE(ticker);
                    double pegTtm = calculatePEGTTM(peTtm,epsCurrentYear, epsTtm);
                    double aScore = calculateAScore(pbAvg, pbTtm, peAvg, peTtm, payoutRatio, debtToEquity, roeTtm, dividendYield, pegTtm, currentRatio, quickRatio);

                    tableModel.addRow(new Object[]{
                        name, ticker, price, peTtm, pbTtm, dividendYield, payoutRatio,
                        grahamNumber, pbAvg, peAvg, epsTtm, roeTtm, aScore,
                        epsCurrentYear, epsNextYear, epsYear3, debtToEquity, pegTtm, currentRatio, quickRatio    // Add this
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
    private double calculatePEGTTM(double peTtm, double epsCurrentYear, double epsTtm) {
        if (epsCurrentYear == 0) return 0;
        else {
        double growthRate = 100 * (epsCurrentYear - epsTtm) / epsTtm;
        
        return round(peTtm/growthRate, 2);
        }
    }
    private void refreshWatchlist() {
        System.out.println("Starting watchlist refresh...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
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
                            double roeTtm = round(ratios.optDouble("roeTTM", 0.0), 2);
                            double dividendYield = round(ratios.optDouble("dividendYieldTTM", 0.0), 2);
                            double payoutRatio = round(ratios.optDouble("payoutRatioTTM", 0.0), 2);
                            double grahamNumber = round(ratios.optDouble("grahamNumberTTM", 0.0), 2);
                            Object debtToEquity = ratios.has("debtToEquityTTM") 
                                ? round(ratios.optDouble("debtToEquityTTM", 0.0), 2) 
                                : "n/a";

                            double epsCurrentYear = epsEstimates != null 
                                ? round(epsEstimates.optDouble("eps0", 0.0), 2) 
                                : 0.0;
                            double epsNextYear = epsEstimates != null 
                                ? round(epsEstimates.optDouble("eps1", 0.0), 2) 
                                : 0.0;
                            double epsYear3 = epsEstimates != null 
                                ? round(epsEstimates.optDouble("eps2", 0.0), 2) 
                                : 0.0;
                            double currentRatio = ratios != null
                                ? round(ratios.optDouble("currentRatioTTM", 0.0), 2)
                                : 0.0;
                            double quickRatio = ratios != null
                                ? round(ratios.optDouble("quickRatioTTM", 0.0), 2)
                                : 0.0;
                            
                                                       
                            double pegTtm = calculatePEGTTM(peTtm, epsCurrentYear,epsTtm);
                            double pbAvg = fetchAveragePB(ticker);
                            double peAvg = fetchAveragePE(ticker);
                            double aScore = calculateAScore(pbAvg, pbTtm, peAvg, peTtm, payoutRatio, debtToEquity, roeTtm, dividendYield, pegTtm, currentRatio, quickRatio);
                            
                            System.out.printf("Ticker: %s, DebtToEquity: %s, A-Score: %f%n", ticker, debtToEquity, aScore);

                            SwingUtilities.invokeLater(() -> {
                                tableModel.setValueAt(price,         modelRow,  2);
                                tableModel.setValueAt(peTtm,         modelRow,  3);
                                tableModel.setValueAt(pbTtm,         modelRow,  4);
                                tableModel.setValueAt(dividendYield, modelRow,  5);
                                tableModel.setValueAt(payoutRatio,   modelRow,  6);
                                tableModel.setValueAt(grahamNumber,  modelRow,  7);
                                tableModel.setValueAt(pbAvg,         modelRow,  8); // PB Avg
                                tableModel.setValueAt(peAvg,         modelRow,  9); // PE Avg
                                tableModel.setValueAt(epsTtm,        modelRow, 10);
                                tableModel.setValueAt(roeTtm,        modelRow, 11);
                                tableModel.setValueAt(aScore,        modelRow, 12);
                                tableModel.setValueAt(epsCurrentYear,modelRow, 13);
                                tableModel.setValueAt(epsNextYear,   modelRow, 14);
                                tableModel.setValueAt(epsYear3,      modelRow, 15);
                                tableModel.setValueAt(debtToEquity,  modelRow, 16);
                                tableModel.setValueAt(pegTtm,      modelRow, 17);
                                tableModel.setValueAt(currentRatio, modelRow, 18); // Index of the new "Current Ratio" column
                                tableModel.setValueAt(quickRatio,  modelRow, 19); // Index of the new "Quick Ratio" column
                            });

                            System.out.println("Refreshed stock data: " + ticker);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Error refreshing " + ticker + ": " + e.getMessage());
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                saveWatchlist();
                System.out.println("Watchlist refresh completed");
            }
        };

        worker.execute();
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
    // Create a combined JSONObject to store results from both endpoints
    JSONObject combinedRatios = new JSONObject();
    
    // First endpoint (key-metrics-ttm)
    String urlMetrics = String.format("https://financialmodelingprep.com/api/v3/key-metrics-ttm/%s?apikey=%s", ticker, API_KEY);
    // Second endpoint (ratios-ttm)
    String urlRatios = String.format("https://financialmodelingprep.com/api/v3/ratios-ttm/%s?apikey=%s", ticker, API_KEY);
    
    try {
        // Fetch data from first endpoint (key-metrics-ttm)
        URL url1 = new URL(urlMetrics);
        HttpURLConnection conn1 = (HttpURLConnection) url1.openConnection();
        conn1.setRequestMethod("GET");
        
        if (conn1.getResponseCode() == 200) {
            Scanner scanner1 = new Scanner(url1.openStream());
            String response1 = scanner1.useDelimiter("\\Z").next();
            scanner1.close();
            
            JSONArray data1 = new JSONArray(response1);
            if (data1.length() > 0) {
                // Copy all properties from first endpoint
                JSONObject metricsData = data1.getJSONObject(0);
                for (String key : metricsData.keySet()) {
                    combinedRatios.put(key, metricsData.get(key));
                }
            }
        }
        conn1.disconnect();
        
        // Fetch data from second endpoint (ratios-ttm)
        URL url2 = new URL(urlRatios);
        HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
        conn2.setRequestMethod("GET");
        
        if (conn2.getResponseCode() == 200) {
            Scanner scanner2 = new Scanner(url2.openStream());
            String response2 = scanner2.useDelimiter("\\Z").next();
            scanner2.close();
            
            JSONArray data2 = new JSONArray(response2);
            if (data2.length() > 0) {
                // Copy all properties from second endpoint
                JSONObject ratiosData = data2.getJSONObject(0);
                for (String key : ratiosData.keySet()) {
                    // Only overwrite if the value doesn't exist or is 0
                    if (!combinedRatios.has(key) || combinedRatios.getDouble(key) == 0) {
                        combinedRatios.put(key, ratiosData.get(key));
                    }
                }
            }
        }
        conn2.disconnect();
        
        return combinedRatios;
        
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
}

    private double fetchAveragePB(String ticker) {
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

    private double calculateAScore(double pbAvg, double pbTtm, double peAvg, double peTtm,
                               double payoutRatio, Object debtToEquity, double roe, double dividendYield, double pegTtm, double currentRatio, double quickRatio) {
    double peRatioTerm = 0;
    double pbRatioTerm = 0;
    double payoutRatioTerm = 0;
    double dividendYieldTerm = 0;
    double debtToEquityTerm = 0;
    double roeTerm = 0;
    double pegTtmTerm =0;
    double currentRatioTerm = 0;
    double quickRatioTerm = 0;

    // Conditions for peRatioTerm
    if (peTtm == 0) {
        peRatioTerm = 0;
    } else if (peAvg / peTtm < 1) {
        peRatioTerm = 0;
    } else if (peAvg / peTtm >= 1 && peAvg / peTtm < 2) {
        peRatioTerm = 1;
    } else if (peAvg / peTtm >= 2) {
        peRatioTerm = 2;
    }

    // Conditions for pbRatioTerm
    if (pbTtm <= 0 || pbAvg / pbTtm < 1) {
        pbRatioTerm = 0;
    } else if (pbAvg / pbTtm >= 1 && pbAvg / pbTtm < 2) {
        pbRatioTerm = 1;
    } else if (pbAvg / pbTtm >= 2) {
        pbRatioTerm = 2;
    }

    // Conditions for dividendYielsTerm
    if (dividendYield < 0.03) {
        dividendYieldTerm = 0;
    } else if ((dividendYield >= 0.03)&& (dividendYield < 0.06)){
        dividendYieldTerm = 1;
    } else if (dividendYield >= 0.06) {
        dividendYieldTerm = 2;
    }
    
    // Conditions for payoutRatioTerm
    if (payoutRatio <= 0 || payoutRatio >= 1 || dividendYield < 0.03 ) {
        payoutRatioTerm = 0;
    } else if (payoutRatio >= 0.5 && payoutRatio < 1) {
        payoutRatioTerm = 1;
    } else {
        payoutRatioTerm = 2;
    }

    // Conditions for debtToEquityTerm
    if (debtToEquity.equals("n/a") || (double) debtToEquity == 0 || (double) debtToEquity > 1) {
        debtToEquityTerm = 0;
    } else if ((double) debtToEquity >= 0.5 && (double) debtToEquity <= 1) {
        debtToEquityTerm = 1;
    } else {
        debtToEquityTerm = 2;
    }
    
    // Conditions for roe
    if (roe <= 0) {
        roeTerm = -1;
    } else if (roe > 0 && roe < 0.1) {
        roeTerm = 0;
    } else if (roe >= 0.1 && roe < 0.2) {
        roeTerm = 1;
    } else if (roe >= 0.2) {
        roeTerm = 2;
    }
    
    // Conditions for pegTtm
    if (pegTtm <= 0) {
        pegTtmTerm = 0;
    } else if (pegTtm > 0 && pegTtm < 0.5) {
        pegTtmTerm = 2;
    } else if (pegTtm >= 0.5 && pegTtm <= 1)  {
        pegTtmTerm = 1;
    } else if (pegTtm > 1) {
        pegTtmTerm = 0;
    }
    
    // Conditions for current ratio
    if (currentRatio < 1) {
        currentRatioTerm = 0;
    } else if (currentRatio >= 1 && currentRatio < 2) {
        currentRatioTerm = 1;
    } else if (currentRatio >= 2) {
        currentRatioTerm  = 2;
    }
    
    // Conditions for quick ratio
    if (quickRatio < 1) {
        quickRatioTerm = 0;
    } else if (quickRatio >= 1 && quickRatio < 2) {
        quickRatioTerm = 1;
    } else if (quickRatio >= 2) {
        quickRatioTerm  = 2;
    }

    return peRatioTerm + pbRatioTerm + payoutRatioTerm + debtToEquityTerm + roeTerm + dividendYieldTerm + pegTtmTerm + currentRatioTerm + quickRatioTerm;
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
            } else if ("n/a".equals(value)) {
                cell.setBackground(Color.LIGHT_GRAY);
            } else {
                cell.setBackground(Color.WHITE);
            }
            return cell;
        }
    }
}
