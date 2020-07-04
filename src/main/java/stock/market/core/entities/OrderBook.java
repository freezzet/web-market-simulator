package stock.market.core.entities;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

public class OrderBook {

    private final NavigableSet<Order> offers;
    private final NavigableSet<Order> bids;

    private OrderBook(NavigableSet<Order> bids, NavigableSet<Order> offerOrders) {
        this.bids = bids;
        this.offers = offerOrders;
    }

    public static OrderBook createOrderBook() {
        return new OrderBook(createOrderSet(), createOrderSet());
    }

    private static NavigableSet<Order> createOrderSet() {
        NavigableSet<Order> list = new TreeSet<>(Comparator.comparing(Order::getPrice).thenComparing(Order::getDate).thenComparing(Order::getId));
        return list;
    }

    public NavigableSet<Order> getOrders(Order.Type type) {
        return Order.Type.BUY == type ? getBids() : getOffers();
    }

    public NavigableSet<Order> getBids() {
        return bids;
    }

    public NavigableSet<Order> getOffers() {
        return offers;
    }
}
