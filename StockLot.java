package afin.jstocks;

public class StockLot {
    private String ticker;
    private int quantity;
    private double purchasePrice;
    private double currentPrice;

    public StockLot(String ticker, int quantity, double purchasePrice, double currentPrice) {
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
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
}