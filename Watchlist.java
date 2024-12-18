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

public class Watchlist {
    private JTable watchlistTable;
    private DefaultTableModel tableModel;
    private static final String API_KEY = "eb7366217370656d66a56a057b8511b0";
    private static final String COLUMN_SETTINGS_FILE = "column_settings.properties";
    private JPanel columnControlPanel;

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Watchlist");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Initialize table model with columns
        tableModel = new DefaultTableModel(new Object[]{
            "Name", "Ticker", "Price", "PE TTM", "PB TTM", "Div. yield", 
            "Payout Ratio", "Graham Number", "PB Avg", "PE Avg", 
            "EPS Next Year", "EPS Year 2", "EPS Year 3"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8 || column == 9;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2: case 3: case 4: case 5: case 6: case 7: 
                    case 8: case 9: case 10: case 11: case 12:
                        return Double.class;
                    default:
                        return String.class;
                }
            }
        };

        watchlistTable = new JTable(tableModel);

        // Set up the table sorter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        setupTableSorter(sorter);
        watchlistTable.setRowSorter(sorter);

        // Configure table properties
        watchlistTable.getTableHeader().setReorderingAllowed(true);
        watchlistTable.getColumnModel().getColumn(8).setCellRenderer(new CustomCellRenderer());
        watchlistTable.getColumnModel().getColumn(9).setCellRenderer(new CustomCellRenderer());

        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(watchlistTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Setup column control panel
        setupColumnControlPanel();
        mainPanel.add(columnControlPanel, BorderLayout.WEST);

        // Setup button panel
        JPanel buttonPanel = new JPanel();
        setupButtonPanel(buttonPanel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Load saved settings and watchlist
        loadColumnSettings();
        loadWatchlist();

        // Add window listener for saving on close
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveColumnSettings();
                saveWatchlist();
            }
        });
    }

    private void setupTableSorter(TableRowSorter<DefaultTableModel> sorter) {
        for (int i = 2; i <= 12; i++) {
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
            final int columnIndex = i;
            checkBox.addActionListener(e -> 
                toggleColumnVisibility(columnIndex, checkBox.isSelected()));
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
    
private void toggleColumnVisibility(int columnIndex, boolean visible) {
        TableColumnModel columnModel = watchlistTable.getColumnModel();
        TableColumn column = columnModel.getColumn(columnIndex);
        if (visible) {
            column.setMinWidth(15);
            column.setMaxWidth(Integer.MAX_VALUE);
            column.setPreferredWidth(75);
        } else {
            column.setMinWidth(0);
            column.setMaxWidth(0);
            column.setPreferredWidth(0);
        }
    }

    private void addStock() {
        String ticker = JOptionPane.showInputDialog("Enter Stock Ticker:");
        if (ticker != null && !ticker.trim().isEmpty()) {
            try {
                JSONObject stockData = fetchStockData(ticker);
                JSONObject epsEstimates = fetchEpsEstimates(ticker);
                
                if (stockData != null) {
                    String name = stockData.getString("name");
                    double price = round(stockData.getDouble("price"), 2);
                    JSONObject ratios = fetchStockRatios(ticker);
                    
                    double peTtm = round(ratios.optDouble("peRatioTTM", 0.0), 2);
                    double pbTtm = round(ratios.optDouble("pbRatioTTM", 0.0), 2);
                    double dividendYield = round(ratios.optDouble("dividendYieldTTM", 0.0), 2);
                    double payoutRatio = round(ratios.optDouble("payoutRatioTTM", 0.0), 2);
                    double grahamNumber = round(ratios.optDouble("grahamNumberTTM", 0.0), 2);
                    
                    // Handle EPS estimates with proper null checking
                    double epsNextYear = epsEstimates != null ? round(epsEstimates.optDouble("epsNextYear", 0.0), 2) : 0.0;
                    double epsYear2 = epsEstimates != null ? round(epsEstimates.optDouble("epsYear2", 0.0), 2) : 0.0;
                    double epsYear3 = epsEstimates != null ? round(epsEstimates.optDouble("epsYear3", 0.0), 2) : 0.0;

                    tableModel.addRow(new Object[]{
                        name, ticker, price, peTtm, pbTtm, dividendYield, payoutRatio, 
                        grahamNumber, 0.0, 0.0, epsNextYear, epsYear2, epsYear3
                    });
                    
                    saveWatchlist();
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "Failed to fetch stock data for " + ticker, 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error fetching stock data: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteStock() {
        int selectedRow = watchlistTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = watchlistTable.convertRowIndexToModel(selectedRow);
            String stockName = (String) tableModel.getValueAt(modelRow, 0);
            int response = JOptionPane.showConfirmDialog(null, 
                "Are you sure you want to delete " + stockName + "?", 
                "Confirm Deletion", 
                JOptionPane.YES_NO_OPTION);
            
            if (response == JOptionPane.YES_OPTION) {
                tableModel.removeRow(modelRow);
                saveWatchlist();
            }
        } else {
            JOptionPane.showMessageDialog(null, 
                "No stock selected. Please select a stock to delete.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshWatchlist() {
        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            int modelRow = watchlistTable.convertRowIndexToModel(i);
            String ticker = (String) tableModel.getValueAt(modelRow, 1);
            try {
                JSONObject stockData = fetchStockData(ticker);
                JSONObject epsEstimates = fetchEpsEstimates(ticker);
                
                if (stockData != null) {
                    String name = stockData.getString("name");
                    double price = round(stockData.getDouble("price"), 2);
                    JSONObject ratios = fetchStockRatios(ticker);
                    
                    double peTtm = round(ratios.optDouble("peRatioTTM", 0.0), 2);
                    double pbTtm = round(ratios.optDouble("pbRatioTTM", 0.0), 2);
                    double dividendYield = round(ratios.optDouble("dividendYieldTTM", 0.0), 2);
                    double payoutRatio = round(ratios.optDouble("payoutRatioTTM", 0.0), 2);
                    double grahamNumber = round(ratios.optDouble("grahamNumberTTM", 0.0), 2);
                    // Continue with refreshWatchlist method
                    double epsNextYear = epsEstimates != null ? round(epsEstimates.optDouble("epsNextYear", 0.0), 2) : 0.0;
                    double epsYear2 = epsEstimates != null ? round(epsEstimates.optDouble("epsYear2", 0.0), 2) : 0.0;
                    double epsYear3 = epsEstimates != null ? round(epsEstimates.optDouble("epsYear3", 0.0), 2) : 0.0;

                    tableModel.setValueAt(name, modelRow, 0);
                    tableModel.setValueAt(price, modelRow, 2);
                    tableModel.setValueAt(peTtm, modelRow, 3);
                    tableModel.setValueAt(pbTtm, modelRow, 4);
                    tableModel.setValueAt(dividendYield, modelRow, 5);
                    tableModel.setValueAt(payoutRatio, modelRow, 6);
                    tableModel.setValueAt(grahamNumber, modelRow, 7);
                    tableModel.setValueAt(epsNextYear, modelRow, 10);
                    tableModel.setValueAt(epsYear2, modelRow, 11);
                    tableModel.setValueAt(epsYear3, modelRow, 12);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Error refreshing data for " + ticker + ": " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
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

    private JSONObject fetchEpsEstimates(String ticker) {
        String urlString = String.format("https://financialmodelingprep.com/api/v3/analyst-estimates/%s?apikey=%s", ticker, API_KEY);
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
                    JSONObject result = new JSONObject();
                    JSONObject firstEstimate = data.getJSONObject(0);
                    
                    // Get EPS estimates for the next three years
                    result.put("epsNextYear", firstEstimate.optDouble("estimatedEpsAvg", 0.0));
                    
                    // For year 2 and 3, we need to look at the subsequent entries if available
                    if (data.length() > 1) {
                        result.put("epsYear2", data.getJSONObject(1).optDouble("estimatedEpsAvg", 0.0));
                    } else {
                        result.put("epsYear2", 0.0);
                    }
                    
                    if (data.length() > 2) {
                        result.put("epsYear3", data.getJSONObject(2).optDouble("estimatedEpsAvg", 0.0));
                    } else {
                        result.put("epsYear3", 0.0);
                    }
                    
                    return result;
                }
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
    
private void saveColumnSettings() {
        Properties props = new Properties();
        TableColumnModel columnModel = watchlistTable.getColumnModel();

        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            props.setProperty("column" + column.getModelIndex() + ".width", 
                Integer.toString(column.getWidth()));
            props.setProperty("column" + column.getModelIndex() + ".index", 
                Integer.toString(columnModel.getColumnIndex(column.getIdentifier())));
        }

        try (FileOutputStream out = new FileOutputStream(COLUMN_SETTINGS_FILE)) {
            props.store(out, "Column settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadColumnSettings() {
        Properties props = new Properties();
        TableColumnModel columnModel = watchlistTable.getColumnModel();

        try (FileInputStream in = new FileInputStream(COLUMN_SETTINGS_FILE)) {
            props.load(in);

            for (int i = 0; i < columnModel.getColumnCount(); i++) {
                TableColumn column = columnModel.getColumn(i);
                int width = Integer.parseInt(props.getProperty(
                    "column" + column.getModelIndex() + ".width", 
                    Integer.toString(column.getWidth())));
                int index = Integer.parseInt(props.getProperty(
                    "column" + column.getModelIndex() + ".index", 
                    Integer.toString(i)));

                column.setPreferredWidth(width);
                columnModel.moveColumn(
                    columnModel.getColumnIndex(column.getIdentifier()), 
                    index);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            jsonObject.put("avgPeRatio", tableModel.getValueAt(i, 9));
            jsonObject.put("epsNextYear", tableModel.getValueAt(i, 10));
            jsonObject.put("epsYear2", tableModel.getValueAt(i, 11));
            jsonObject.put("epsYear3", tableModel.getValueAt(i, 12));
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
                        jsonObject.optDouble("avgPbRatio", 0.0),
                        jsonObject.optDouble("avgPeRatio", 0.0),
                        jsonObject.optDouble("epsNextYear", 0.0),
                        jsonObject.optDouble("epsYear2", 0.0),
                        jsonObject.optDouble("epsYear3", 0.0)
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
