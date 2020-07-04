package stock.market.web.rest.data;

public class EngineStats {
    private final int totalOrders;
    private final int totalTrades;
    private final int ordersPerSecond;
    private final int tradesPerSecond;

    public EngineStats(int totalOrders, int totalTrades, int ordersPerSecond, int tradesPerSecond) {
        this.totalOrders = totalOrders;
        this.totalTrades = totalTrades;
        this.ordersPerSecond = ordersPerSecond;
        this.tradesPerSecond = tradesPerSecond;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public int getTotalTrades() {
        return totalTrades;
    }

    public int getOrdersPerSecond() {
        return ordersPerSecond;
    }

    public int getTradesPerSecond() {
        return tradesPerSecond;
    }
}
