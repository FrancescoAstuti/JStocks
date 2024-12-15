package afin.jstocks;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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

        tableModel = new DefaultTableModel(new Object[]{"Name", "Ticker", "Price", "Change", "Volume"}, 0);
        watchlistTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(watchlistTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Stock");
        JButton deleteButton = new JButton("Delete Stock");

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

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void addStock() {
        String ticker = JOptionPane.showInputDialog("Enter Stock Ticker:");

        if (ticker != null) {
            try {
                JSONObject stockData = fetchStockData(ticker);
                if (stockData != null) {
                    String name = stockData.getString("name");
                    double price = stockData.getDouble("price");
                    double change = stockData.getDouble("change");
                    double volume = stockData.getDouble("volume");
                    tableModel.addRow(new Object[]{name, ticker, price, change, volume});
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
        } else {
            JOptionPane.showMessageDialog(null, "No stock selected. Please select a stock to delete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
}