package stock.market.web.rest.data;

import java.math.BigDecimal;

public class OrderData {

    private final BigDecimal price;
    private final BigDecimal quantity;

    public OrderData(BigDecimal price, BigDecimal quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
}
