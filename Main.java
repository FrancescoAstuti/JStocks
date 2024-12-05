package afin.jstocks;

import javax.swing.*;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileReader;
import org.json.JSONTokener;

public class Main {
    private static ArrayList<StockLot> stockLots = new ArrayList<>();

    public static void main(String[] args) {
        loadStockLots();
        Runtime.getRuntime().addShutdownHook(new Thread(Main::saveStockLots));

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Overview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 200);
            JButton portfolioButton = new JButton("Portfolio");

            portfolioButton.addActionListener(e -> {
                frame.dispose();
                GUI gui = new GUI(stockLots);
                gui.createAndShowGUI();
            });

            JPanel panel = new JPanel();
            panel.add(portfolioButton);
            frame.add(panel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static void addStockLot(String ticker, int quantity, double purchasePrice, double currentPrice) {
        StockLot stockLot = new StockLot(ticker, quantity, purchasePrice, currentPrice);
        stockLots.add(stockLot);
    }

    public static void modifyStockLot(int index, String ticker, int quantity, double purchasePrice, double currentPrice) {
        if (index >= 0 && index < stockLots.size()) {
            StockLot stockLot = stockLots.get(index);
            stockLot.setTicker(ticker);
            stockLot.setQuantity(quantity);
            stockLot.setPurchasePrice(purchasePrice);
            stockLot.setCurrentPrice(currentPrice);
        }
    }

    public static void deleteStockLot(int index) {
        if (index >= 0 && index < stockLots.size()) {
            stockLots.remove(index);
        }
    }

    public static void saveStockLots() {
        JSONArray jsonArray = new JSONArray();
        for (StockLot stockLot : stockLots) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ticker", stockLot.getTicker());
            jsonObject.put("quantity", stockLot.getQuantity());
            jsonObject.put("purchasePrice", stockLot.getPurchasePrice());
            jsonObject.put("currentPrice", stockLot.getCurrentPrice());
            jsonArray.put(jsonObject);
        }

        try (FileWriter file = new FileWriter("stockLots.json")) {
            file.write(jsonArray.toString());
            System.out.println("Stock lots saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to save stock lots.");
        }
    }

    public static void loadStockLots() {
        try (FileReader reader = new FileReader("stockLots.json")) {
            JSONArray jsonArray = new JSONArray(new JSONTokener(reader));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String ticker = jsonObject.getString("ticker");
                int quantity = jsonObject.getInt("quantity");
                double purchasePrice = jsonObject.getDouble("purchasePrice");
                double currentPrice = jsonObject.getDouble("currentPrice");
                stockLots.add(new StockLot(ticker, quantity, purchasePrice, currentPrice));
            }
            System.out.println("Stock lots loaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load stock lots.");
        }
    }
}