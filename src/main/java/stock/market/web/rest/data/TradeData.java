package stock.market.web.rest.data;

import stock.market.core.entities.Order;
import stock.market.core.entities.Trade;

import java.math.BigDecimal;

public class TradeData {
    private final Integer id;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final String date;
    private final String symbol;
    private final Order.Type type;
    private final Trade.Status status;

    public TradeData(Integer id, BigDecimal quantity, BigDecimal price, String date, String symbol, Order.Type type, Trade.Status status) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.date = date;
        this.symbol = symbol;
        this.type = type;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }

    public String getSymbol() {
        return symbol;
    }

    public Order.Type getType() {
        return type;
    }

    public Trade.Status getStatus() {
        return status;
    }
}
