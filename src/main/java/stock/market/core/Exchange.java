package stock.market.core;

import stock.market.core.engine.OrderMatchingEngine;
import stock.market.core.gateway.MarketDataGateway;
import stock.market.core.gateway.TradingGateway;

public class Exchange {
    private final OrderMatchingEngine engine;
    private final TradingGateway tradingGateway;
    private final MarketDataGateway marketDataGateway;

    private Exchange() {
        engine = new OrderMatchingEngine();
        tradingGateway = new TradingGateway();
        marketDataGateway = new MarketDataGateway();
    }

    public static Exchange getInstance() {
        return Holder.INSTANCE;
    }

    public boolean open() {
        boolean result = true;

        try {
            marketDataGateway.open(16);
            engine.registerMarkerDataGateway(marketDataGateway);
            engine.start();
            tradingGateway.open(engine);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    public TradingGateway getTradingGateway() {
        return tradingGateway;
    }

    public MarketDataGateway getMarketDataGateway() {
        return marketDataGateway;
    }

    public OrderMatchingEngine getEngine() {
        return engine;
    }

    private static class Holder {
        static final Exchange INSTANCE = new Exchange();
    }

}
