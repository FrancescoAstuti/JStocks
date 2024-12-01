import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Main {
    private static ArrayList<StockLot> stockLots = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI(stockLots);
            gui.createAndShowGUI();
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
}