package stock.market.web.service;

import org.springframework.stereotype.Component;
import stock.market.core.Exchange;
import stock.market.core.engine.OrderMatchingEngine;
import stock.market.core.entities.Order;
import stock.market.core.entities.Trade;
import stock.market.core.gateway.exception.TradingGatewayNotOpenException;
import stock.market.core.gateway.exception.TradingGatewayTimeoutException;
import stock.market.web.rest.data.EngineStats;
import stock.market.web.rest.data.OrderRequestData;

import java.util.List;

/**
 * This class just serves as a mapping between web components and Java API provided provided by Exchange
 */

@Component
public class ExchangeService {

    public void offerOrder(OrderRequestData order) throws TradingGatewayTimeoutException, TradingGatewayNotOpenException {
        Exchange.getInstance().getTradingGateway().offerOrder(order);
    }

    public Trade[] getTrades() {
        return Exchange.getInstance().getMarketDataGateway().getTrades();
    }

    public List<Order> getBestOrders(Order.Type type, String symbol) {
        return Exchange.getInstance().getMarketDataGateway().getBestOrders(type, symbol);
    }

    public EngineStats getEngineStats() {
        OrderMatchingEngine engine = Exchange.getInstance().getEngine();
        return new EngineStats(engine.getTotalOrders(), engine.getTotalTrades(), engine.getOrdersPerSecond(), engine.getTradesPerSecond());
    }
}
