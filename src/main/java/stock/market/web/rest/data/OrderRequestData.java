package stock.market.web.rest.data;

import stock.market.core.entities.Order;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class OrderRequestData {

    @NotNull(message = "Please provide a price")
    @DecimalMin("0.00")
    private final BigDecimal price;

    @NotNull(message = "Please provide a quantity")
    @DecimalMin("0.00")
    private final BigDecimal quantity;
    private final Order.Type type;
    private final String symbol;

    public OrderRequestData(BigDecimal price, BigDecimal quantity, Order.Type type, String symbol) {
        this.price = price;
        this.quantity = quantity;
        this.type = type;
        this.symbol = symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public Order.Type getType() {
        return type;
    }

    public String getSymbol() {
        return symbol;
    }
}
