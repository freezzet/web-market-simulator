package stock.market.core.gateway;

import stock.market.core.engine.OrderMatchingEngine;
import stock.market.core.gateway.exception.TradingGatewayNotOpenException;
import stock.market.core.gateway.exception.TradingGatewayTimeoutException;
import stock.market.web.rest.data.OrderRequestData;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class TradingGateway {

    private static long NEW_ORDER_OFFER_WAIT_TIME = 1000; // ms

    private OrderMatchingEngine engine;

    public void open(OrderMatchingEngine engine) {
        this.engine = engine;
    }

    public void close() {
        this.engine = null;
    }

    public boolean offerOrder(OrderRequestData newOrder) throws TradingGatewayTimeoutException, TradingGatewayNotOpenException {

        if (engine == null) {
            throw new TradingGatewayNotOpenException();
        }

        if (newOrder.getQuantity().compareTo(BigDecimal.ZERO) <= 0 || newOrder.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        try {
            return engine.getOrderRequests().offer(newOrder, NEW_ORDER_OFFER_WAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new TradingGatewayTimeoutException(e.getMessage());
        }
    }
}
