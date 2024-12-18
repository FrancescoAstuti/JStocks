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

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Watchlist");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(900, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{
            "Name", "Ticker", "Price", "PE TTM", "PB TTM", "Dividend Yield", "Payout Ratio", "Graham Number", "Avg PB Ratio (20 Years)", "Avg PE Ratio (20 Years)"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Allow editing of the "Avg PB Ratio (20 Years)" and "Avg PE Ratio (20 Years)" columns
                return column == 8 || column == 9;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Specify the data type for each column
                switch (columnIndex) {
                    case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
                        return Double.class;
                    default:
                        return String.class;
                }
            }
        };

        watchlistTable = new JTable(tableModel);

        // Enable sorting with custom comparators for numerical columns
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setComparator(2, Comparator.comparingDouble(o -> (Double) o));
        sorter.setComparator(3, Comparator.comparingDouble(o -> (Double) o));
        sorter.setComparator(4, Comparator.comparingDouble(o -> (Double) o));
        sorter.setComparator(5, Comparator.comparingDouble(o -> (Double) o));
        sorter.setComparator(6, Comparator.comparingDouble(o -> (Double) o));
        sorter.setComparator(7, Comparator.comparingDouble(o -> (Double) o));
        sorter.setComparator(8, Comparator.comparingDouble(o -> (Double) o));
        sorter.setComparator(9, Comparator.comparingDouble(o -> (Double) o));
        watchlistTable.setRowSorter(sorter);

        // Enable column reordering
        watchlistTable.getTableHeader().setReorderingAllowed(true);

        // Set custom cell renderer for Avg PB Ratio and Avg PE Ratio columns
        watchlistTable.getColumnModel().getColumn(8).setCellRenderer(new CustomCellRenderer());
        watchlistTable.getColumnModel().getColumn(9).setCellRenderer(new CustomCellRenderer());

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

        // Load column settings after adding the table to the frame
        loadColumnSettings();
        loadWatchlist();

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                saveColumnSettings();
            }
        });
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

                    tableModel.addRow(new Object[]{name, ticker, price, peTtm, pbTtm, dividendYield, payoutRatio, grahamNumber, 0.0, 0.0});
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
                    tableModel.setValueAt(name, i, 0);
                    tableModel.setValueAt(price, i, 2);
                    tableModel.setValueAt(peTtm, i, 3);
                    tableModel.setValueAt(pbTtm, i, 4);
                    tableModel.setValueAt(dividendYield, i, 5);
                    tableModel.setValueAt(payoutRatio, i, 6);
                    tableModel.setValueAt(grahamNumber, i, 7);
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
            jsonObject.put("avgPbRatio", tableModel.getValueAt(i, 8));
            jsonObject.put("avgPeRatio", tableModel.getValueAt(i, 9));
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
                        jsonObject.optDouble("avgPeRatio", 0.0)
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveColumnSettings() {
        Properties props = new Properties();
        TableColumnModel columnModel = watchlistTable.getColumnModel();
        
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            props.setProperty("column" + column.getModelIndex() + ".width", Integer.toString(column.getWidth()));
            props.setProperty("column" + column.getModelIndex() + ".index", Integer.toString(columnModel.getColumnIndex(column.getIdentifier())));
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
                int width = Integer.parseInt(props.getProperty("column" + column.getModelIndex() + ".width", Integer.toString(column.getWidth())));
                int index = Integer.parseInt(props.getProperty("column" + column.getModelIndex() + ".index", Integer.toString(i)));

                column.setPreferredWidth(width);
                columnModel.moveColumn(columnModel.getColumnIndex(column.getIdentifier()), index);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Double && (Double) value == 0.0) {
                cell.setBackground(Color.YELLOW);
            } else {
                cell.setBackground(Color.WHITE);
            }
            return cell;
        }
    }
}