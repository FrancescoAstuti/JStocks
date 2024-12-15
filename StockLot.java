package afin.jstocks;

import java.util.List;

public class StockLot {
    private String ticker;
    private double quantity; // Changed from int to double
    private double purchasePrice;
    private double currentPrice;

    public StockLot(String ticker, double quantity, double purchasePrice, double currentPrice) { // Changed parameter type to double
        this.ticker = ticker;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.currentPrice = currentPrice;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public double getQuantity() { // Changed return type to double
        return quantity;
    }

    public void setQuantity(double quantity) { // Changed parameter type to double
        this.quantity = quantity;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getProfitLossPercentage() {
        return ((currentPrice - purchasePrice) / purchasePrice) * 100;
    }

    public static double calculateTotalLots(List<StockLot> stockLots) {
        return stockLots.stream().mapToDouble(StockLot::getQuantity).sum();
    }
}