package stock.market.core.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stock.market.core.engine.exception.OrderMatchingEngineException;
import stock.market.core.entities.Order;
import stock.market.core.entities.OrderBook;
import stock.market.core.entities.Trade;
import stock.market.core.gateway.MarketDataGateway;
import stock.market.web.SpringBootStarter;
import stock.market.web.rest.data.OrderRequestData;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The OrderMatchingEngine is a manager of two asynchronous tasks MatchingOrdersTask and AcceptingOrdersTask.
 * The AcceptingOrdersTask accepts orders and adds them to the book.
 * The MatchingOrdersTask matches accepted orders and generates Trades events.
 */
public class OrderMatchingEngine {

    private static final long BALANCE_BOOK_INTERVAL = 1000; // crossing buys and sells every 1 second
    private static final long BEST_ORDERS_LIMIT_SIZE = 15; // Size of lists of Best Bids and Best Offers

    private static int tradeId = 0;
    private static int orderId = 0;
    private static int traderClientId = 0;

    private static Logger logger = LoggerFactory.getLogger(SpringBootStarter.class);
    private final LinkedBlockingQueue<OrderRequestData> orderRequests = new LinkedBlockingQueue<>();
    private final Map<String, OrderBook> oderBooks = new HashMap<>();
    private List<MarketDataGateway> marketDataGateways = new ArrayList<>();
    private ScheduledExecutorService scheduler;

    private MatchingOrdersTask matchingOrdersTask;
    private AcceptingOrdersTask acceptingOrdersTask;
    private Thread acceptingOrdersThread;

    {
        // Init some stock symbols
        registerNewOrderBook("FB");
        registerNewOrderBook("AMZN");
        registerNewOrderBook("TSLA");
        registerNewOrderBook("BA");
        registerNewOrderBook("AAPL");

    }

    public void start() throws OrderMatchingEngineException {
        if (scheduler != null && !scheduler.isTerminated()) {
            throw new OrderMatchingEngineException("Order Matching Engine is already running");
        }
        scheduler = Executors.newScheduledThreadPool(1);

        AtomicBoolean isMatching = new AtomicBoolean(false);

        acceptingOrdersTask = new AcceptingOrdersTask(isMatching);
        acceptingOrdersThread = new Thread(acceptingOrdersTask);
        acceptingOrdersThread.setName("Accepting Orders Job");
        acceptingOrdersThread.start();

        matchingOrdersTask = new MatchingOrdersTask(isMatching);
        scheduler.scheduleAtFixedRate(matchingOrdersTask, BALANCE_BOOK_INTERVAL, BALANCE_BOOK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (acceptingOrdersThread != null) {
            acceptingOrdersThread.interrupt();
        }
    }

    private void registerOrder(OrderRequestData orderRequest) {
        Order newOrder = new Order(orderRequest);
        OrderBook orderBook = oderBooks.get(newOrder.getSymbol());
        if (orderBook == null) {
            orderBook = registerNewOrderBook(newOrder.getSymbol());
        }
        newOrder.setId(orderId++);
        newOrder.setDate(new Date());

        Set<Order> orderSet = orderBook.getOrders(newOrder.getType());
        orderSet.add(newOrder);
    }

    public OrderBook registerNewOrderBook(String symbol) {
        OrderBook orderBook = OrderBook.createOrderBook();
        oderBooks.put(symbol, orderBook);
        return orderBook;
    }

    public int getTotalOrders() {
        return acceptingOrdersTask.totalOrders;
    }

    public int getTotalTrades() {
        return matchingOrdersTask.totalTrades;
    }

    public int getOrdersPerSecond() {
        return acceptingOrdersTask.ordersPerSecond;
    }

    public int getTradesPerSecond() {
        return matchingOrdersTask.tradesPerSecond;
    }

    private void doTrade(Order offer, Order bid, OrderBook orderBook) {
        BigDecimal sellQ = offer.getQuantity();
        BigDecimal buyQ = bid.getQuantity();
        BigDecimal minQ = sellQ.min(buyQ);

        // set left quantity
        BigDecimal left = sellQ.subtract(buyQ);
        offer.setQuantity(left);
        bid.setQuantity(left.negate());

        Date time = new Date();

        BigDecimal tradePrice = (offer.getDate().compareTo(bid.getDate()) > 0) ? bid.getPrice() : offer.getPrice();

        Trade offerTrade = new Trade(tradeId++, offer, tradePrice, minQ, time);
        Trade bidTrade = new Trade(tradeId++, bid, tradePrice, minQ, time);

        for (MarketDataGateway gateway : marketDataGateways) {
            gateway.registerTrade(offerTrade);
            gateway.registerTrade(bidTrade);
        }
    }

    public LinkedBlockingQueue<OrderRequestData> getOrderRequests() {
        return orderRequests;
    }

    public void registerMarkerDataGateway(MarketDataGateway marketDataGateway) {
        marketDataGateways.add(marketDataGateway);
    }

    class AcceptingOrdersTask implements Runnable {

        private final AtomicBoolean isMatching;
        private Instant lastTaskTime = Instant.now();
        private volatile int totalOrders;
        private volatile int ordersPerSecond;

        AcceptingOrdersTask(AtomicBoolean isMatching) {
            this.isMatching = isMatching;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    synchronized (isMatching) {
                        registerOrders();
                        isMatching.wait();
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        public void registerOrders() {
            int lastTotalOrders = totalOrders;

            OrderRequestData orderRequest = orderRequests.poll();

            while (orderRequest != null && !isMatching.get()) {
                registerOrder(orderRequest);
                orderRequest = orderRequests.poll();
            }
            Instant currentTime = Instant.now();

            long period = ChronoUnit.MILLIS.between(lastTaskTime, currentTime);
            if (period > 0) {
                totalOrders = orderId;
                int newOrders = totalOrders - lastTotalOrders;
                ordersPerSecond = (int) (newOrders * 1000 / period);
            }
            lastTaskTime = currentTime;
        }
    }

    class MatchingOrdersTask implements Runnable {
        private final AtomicBoolean isMatching;
        private volatile int totalTrades = 0;
        private volatile int tradesPerSecond = 0;

        private Instant lastTaskTime = Instant.now();

        public MatchingOrdersTask(AtomicBoolean isMatching) {
            this.isMatching = isMatching;
        }

        @Override
        public void run() {

            try {
                isMatching.set(true);
                int lastTotalTrades = totalTrades;

                synchronized (isMatching) {
                    balanceBook();
                    isMatching.set(false);
                    isMatching.notify();
                }
                Instant currentTime = Instant.now();
                long period = ChronoUnit.MILLIS.between(lastTaskTime, currentTime);

                int newTrades = totalTrades - lastTotalTrades;
                if (period > 0) {
                    tradesPerSecond = (int) (newTrades * 1000 / period);
                }
                lastTaskTime = currentTime;
            } catch (Error e) {
                logger.error(e.getMessage(), e);
            }
        }


        private void balanceBook() {
            oderBooks.forEach((key, value) -> completeTrades(value, key));
        }

        private void completeTrades(OrderBook orderBook, String symbol) {
            NavigableSet<Order> offers = orderBook.getOffers();
            NavigableSet<Order> bids = orderBook.getBids();

            Iterator<Order> bidsIter = bids.descendingIterator();
            Iterator<Order> offerIter = offers.iterator();
            Order bid = bidsIter.hasNext() ? bidsIter.next() : null;
            Order offer = offerIter.hasNext() ? offerIter.next() : null;

            boolean needBalance = bid != null && offer != null;

            while (needBalance) {
                // All orders are already sorted by Price and Date so just need to match all suitable
                if (offer.getPrice().compareTo(bid.getPrice()) <= 0) {
                    doTrade(offer, bid, orderBook);
                    if (offer.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                        offerIter.remove();
                        if (offerIter.hasNext()) {
                            offer = offerIter.next();
                        } else {
                            needBalance = false;
                        }
                    }
                    if (bid.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                        bidsIter.remove();
                        if (bidsIter.hasNext()) {
                            bid = bidsIter.next();
                        } else {
                            needBalance = false;
                        }
                    }
                } else {
                    needBalance = false;
                }
            }
            Iterable<Order> iterableBids = bids::descendingIterator;
            List<Order> bestBids = StreamSupport.stream(iterableBids.spliterator(), false).limit(BEST_ORDERS_LIMIT_SIZE).collect(Collectors.toList());

            Iterable<Order> iterableOffers = offers::iterator;
            List<Order> bestOffers = StreamSupport.stream(iterableOffers.spliterator(), false).limit(BEST_ORDERS_LIMIT_SIZE).collect(Collectors.toList());

            for (MarketDataGateway gateway : marketDataGateways) {
                gateway.registerBestBids(symbol, bestBids);
                gateway.registerBestOffers(symbol, bestOffers);
            }
            totalTrades = tradeId;
        }
    }
}
