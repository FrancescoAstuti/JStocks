package afin.jstocks; // Package name

import javax.swing.*; // Importing Swing library
import javax.swing.table.DefaultTableModel; // Importing DefaultTableModel class
import java.awt.*; // Importing AWT library
import java.awt.event.ActionEvent; // Importing ActionEvent class
import java.awt.event.ActionListener; // Importing ActionListener interface
import java.io.File; // Importing File class
import java.io.FileReader; // Importing FileReader class
import java.io.FileWriter; // Importing FileWriter class
import java.io.IOException; // Importing IOException class
import java.net.HttpURLConnection; // Importing HttpURLConnection class
import java.net.URL; // Importing URL class
import java.util.Scanner; // Importing Scanner class
import org.json.JSONArray; // Importing JSONArray class
import org.json.JSONObject; // Importing JSONObject class

public class Watchlist { // Class definition
    private JTable watchlistTable; // JTable object to display watchlist
    private DefaultTableModel tableModel; // Table model for JTable
    private static final String API_KEY = "eb7366217370656d66a56a057b8511b0"; // API key for fetching stock data

    public void createAndShowGUI() { // Method to create and show GUI
        JFrame frame = new JFrame("Watchlist"); // Creating JFrame object
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Setting default close operation
        frame.setSize(800, 600); // Setting frame size

        JPanel mainPanel = new JPanel(new BorderLayout()); // Creating main panel with BorderLayout

        tableModel = new DefaultTableModel(new Object[]{"Name", "Ticker", "Price", "Change", "Volume", "PE TTM", "PB TTM"}, 0); // Initializing table model with column names
        watchlistTable = new JTable(tableModel); // Creating JTable with table model
        JScrollPane scrollPane = new JScrollPane(watchlistTable); // Adding JTable to JScrollPane
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Adding scroll pane to main panel

        JPanel buttonPanel = new JPanel(); // Creating button panel
        JButton addButton = new JButton("Add Stock"); // Creating Add Stock button
        JButton deleteButton = new JButton("Delete Stock"); // Creating Delete Stock button
        JButton refreshButton = new JButton("Refresh"); // Creating Refresh button

        addButton.addActionListener(new ActionListener() { // Adding action listener to Add Stock button
            @Override
            public void actionPerformed(ActionEvent e) { // Action performed method
                addStock(); // Call addStock method
            }
        });

        deleteButton.addActionListener(new ActionListener() { // Adding action listener to Delete Stock button
            @Override
            public void actionPerformed(ActionEvent e) { // Action performed method
                deleteStock(); // Call deleteStock method
            }
        });

        refreshButton.addActionListener(new ActionListener() { // Adding action listener to Refresh button
            @Override
            public void actionPerformed(ActionEvent e) { // Action performed method
                refreshWatchlist(); // Call refreshWatchlist method
            }
        });

        buttonPanel.add(addButton); // Adding Add Stock button to button panel
        buttonPanel.add(deleteButton); // Adding Delete Stock button to button panel
        buttonPanel.add(refreshButton); // Adding Refresh button to button panel
        mainPanel.add(buttonPanel, BorderLayout.SOUTH); // Adding button panel to main panel

        frame.add(mainPanel); // Adding main panel to frame
        frame.setLocationRelativeTo(null); // Centering frame
        frame.setVisible(true); // Making frame visible

        loadWatchlist(); // Load watchlist data
    }

    private void addStock() { // Method to add stock
        String ticker = JOptionPane.showInputDialog("Enter Stock Ticker:"); // Show input dialog to enter stock ticker

        if (ticker != null) { // If ticker is not null
            try { // Try block
                JSONObject stockData = fetchStockData(ticker); // Fetch stock data
                if (stockData != null) { // If stock data is not null
                    String name = stockData.getString("name"); // Get name from stock data
                    double price = stockData.getDouble("price"); // Get price from stock data
                    double change = stockData.getDouble("change"); // Get change from stock data
                    double volume = stockData.getDouble("volume"); // Get volume from stock data
                    JSONObject ratios = fetchStockRatios(ticker); // Fetch stock ratios
                    double peTtm = ratios.optDouble("peRatioTTM", 0.0); // Get PE TTM from ratios
                    double pbTtm = ratios.optDouble("pbRatioTTM", 0.0); // Get PB TTM from ratios
                    tableModel.addRow(new Object[]{name, ticker, price, change, volume, peTtm, pbTtm}); // Add row to table model
                    saveWatchlist(); // Save watchlist data
                } else { // If stock data is null
                    JOptionPane.showMessageDialog(null, "Failed to fetch stock data.", "Error", JOptionPane.ERROR_MESSAGE); // Show error message
                }
            } catch (Exception e) { // Catch block
                e.printStackTrace(); // Print stack trace
                JOptionPane.showMessageDialog(null, "Error fetching stock data.", "Error", JOptionPane.ERROR_MESSAGE); // Show error message
            }
        }
    }

    private void deleteStock() { // Method to delete stock
        int selectedRow = watchlistTable.getSelectedRow(); // Get selected row
        if (selectedRow != -1) { // If selected row is not -1
            tableModel.removeRow(selectedRow); // Remove row from table model
            saveWatchlist(); // Save watchlist data
        } else { // If selected row is -1
            JOptionPane.showMessageDialog(null, "No stock selected. Please select a stock to delete.", "Error", JOptionPane.ERROR_MESSAGE); // Show error message
        }
    }

    private void refreshWatchlist() { // Method to refresh watchlist
        saveWatchlist(); // Save watchlist data
    }

    private JSONObject fetchStockData(String ticker) { // Method to fetch stock data
        String urlString = String.format("https://financialmodelingprep.com/api/v3/quote/%s?apikey=%s", ticker, API_KEY); // Format URL string
        HttpURLConnection connection = null; // Initialize HttpURLConnection

        try { // Try block
            URL url = new URL(urlString); // Create URL object
            connection = (HttpURLConnection) url.openConnection(); // Open connection
            connection.setRequestMethod("GET"); // Set request method to GET

            int responseCode = connection.getResponseCode(); // Get response code
            if (responseCode == 200) { // If response code is 200
                Scanner scanner = new Scanner(url.openStream()); // Create scanner to read response
                String response = scanner.useDelimiter("\\Z").next(); // Read response
                scanner.close(); // Close scanner

                JSONArray data = new JSONArray(response); // Create JSONArray from response
                if (data.length() > 0) { // If data length is greater than 0
                    return data.getJSONObject(0); // Return first JSONObject
                }
            } else { // If response code is not 200
                throw new IOException("Failed to get valid response from API. Response Code: " + responseCode); // Throw IOException
            }
        } catch (IOException e) { // Catch block
            e.printStackTrace(); // Print stack trace
        } finally { // Finally block
            if (connection != null) { // If connection is not null
                connection.disconnect(); // Disconnect connection
            }
        }
        return null; // Return null
    }

    private JSONObject fetchStockRatios(String ticker) { // Method to fetch stock ratios
        String urlString = String.format("https://financialmodelingprep.com/api/v3/key-metrics-ttm/%s?apikey=%s", ticker, API_KEY); // Format URL string for ratios API
        HttpURLConnection connection = null; // Initialize HttpURLConnection

        try { // Try block
            URL url = new URL(urlString); // Create URL object
            connection = (HttpURLConnection) url.openConnection(); // Open connection
            connection.setRequestMethod("GET"); // Set request method to GET

            int responseCode = connection.getResponseCode(); // Get response code
            if (responseCode == 200) { // If response code is 200
                Scanner scanner = new Scanner(url.openStream()); // Create scanner to read response
                String response = scanner.useDelimiter("\\Z").next(); // Read response
                scanner.close(); // Close scanner

                JSONArray data = new JSONArray(response); // Create JSONArray from response
                if (data.length() > 0) { // If data length is greater than 0
                    return data.getJSONObject(0); // Return first JSONObject
                }
            } else { // If response code is not 200
                throw new IOException("Failed to get valid response from API. Response Code: " + responseCode); // Throw IOException
            }
        } catch (IOException e) { // Catch block
            e.printStackTrace(); // Print stack trace
        } finally { // Finally block
            if (connection != null) { // If connection is not null
                connection.disconnect(); // Disconnect connection
            }
        }
        return null; // Return null
    }

    private void saveWatchlist() { // Method to save watchlist data
        JSONArray jsonArray = new JSONArray(); // Create JSONArray
        for (int i = 0; i < tableModel.getRowCount(); i++) { // Loop through table model rows
            JSONObject jsonObject = new JSONObject(); // Create JSONObject
            jsonObject.put("name", tableModel.getValueAt(i, 0)); // Put name in JSONObject
            jsonObject.put("ticker", tableModel.getValueAt(i, 1)); // Put ticker in JSONObject
            jsonObject.put("price", tableModel.getValueAt(i, 2)); // Put price in JSONObject
            jsonObject.put("change", tableModel.getValueAt(i, 3)); // Put change in JSONObject
            jsonObject.put("volume", tableModel.getValueAt(i, 4)); // Put volume in JSONObject
            jsonObject.put("peTtm", tableModel.getValueAt(i, 5)); // Put PE TTM in JSONObject
            jsonObject.put("pbTtm", tableModel.getValueAt(i, 6)); // Put PB TTM in JSONObject
            jsonArray.put(jsonObject); // Add JSONObject to JSONArray
        }

        try (FileWriter file = new FileWriter("watchlist.json")) { // Try block with FileWriter
            file.write(jsonArray.toString()); // Write JSONArray to file
            file.flush(); // Flush FileWriter
        } catch (IOException e) { // Catch block
            e.printStackTrace(); // Print stack trace
        }
    }

    private void loadWatchlist() { // Method to load watchlist data
        File file = new File("watchlist.json"); // Create File object
        if (file.exists()) { // If file exists
            try (FileReader reader = new FileReader(file)) { // Try block with FileReader
                Scanner scanner = new Scanner(reader); // Create scanner to read file
                String json = scanner.useDelimiter("\\Z").next(); // Read file
                scanner.close(); // Close scanner

                JSONArray jsonArray = new JSONArray(json); // Create JSONArray from file content
                for (int i = 0; i < jsonArray.length(); i++) { // Loop through JSONArray
                    JSONObject jsonObject = jsonArray.getJSONObject(i); // Get JSONObject from JSONArray
                    tableModel.addRow(new Object[]{ // Add row to table model
                        jsonObject.getString("name"), // Get name from JSONObject
                        jsonObject.getString("ticker"), // Get ticker from JSONObject
                        jsonObject.getDouble("price"), // Get price from JSONObject
                        jsonObject.getDouble("change"), // Get change from JSONObject
                        jsonObject.getDouble("volume"), // Get volume from JSONObject
                        jsonObject.getDouble("peTtm"), // Get PE TTM from JSONObject
                        jsonObject.getDouble("pbTtm") // Get PB TTM from JSONObject
                    });
                }
            } catch (IOException e) { // Catch block
                e.printStackTrace(); // Print stack trace
            }
        }
    }
}