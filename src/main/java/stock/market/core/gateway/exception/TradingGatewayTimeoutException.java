package stock.market.core.gateway.exception;

public class TradingGatewayTimeoutException extends Exception {
    public TradingGatewayTimeoutException(String message) {
        super(message);
    }
}
