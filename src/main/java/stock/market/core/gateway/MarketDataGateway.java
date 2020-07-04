package stock.market.core.gateway;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import stock.market.core.entities.Order;
import stock.market.core.entities.Trade;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MarketDataGateway {

    private ReadWriteLock lockTrades = new ReentrantReadWriteLock();
    private Map<String, List<Order>> bestBids = new ConcurrentHashMap<>();
    private Map<String, List<Order>> bestOffers = new ConcurrentHashMap<>();

    private Queue<Trade> tradesQueue;

    public void open(int capacity) {
        tradesQueue = new CircularFifoQueue<>(capacity);
    }

    public void registerTrade(Trade offerTrade) {
        if (tradesQueue != null) {
            // TODO: Avoid the blocking of Engine thread.
            lockTrades.writeLock().lock();
            try {
                tradesQueue.add(offerTrade);
            } finally {
                lockTrades.writeLock().unlock();
            }
        }
    }

    public Trade[] getTrades() {
        lockTrades.readLock().lock();
        Trade[] trades = new Trade[tradesQueue.size()];
        try {
            trades = tradesQueue.toArray(trades);
        } finally {
            lockTrades.readLock().unlock();
        }
        return trades;
    }

    public List<Order> getBestBids(String symbol) {
        return bestBids.get(symbol);
    }

    public List<Order> getBestOffers(String symbol) {
        return bestOffers.get(symbol);
    }

    public void registerBestBids(String symbol, List<Order> bestBids) {
        this.bestBids.put(symbol, bestBids);
    }

    public void registerBestOffers(String symbol, List<Order> bestOffers) {
        this.bestOffers.put(symbol, bestOffers);
    }

    public List<Order> getBestOrders(Order.Type type, String symbol) {
        return type == Order.Type.BUY ? getBestBids(symbol) : getBestOffers(symbol);
    }
}
