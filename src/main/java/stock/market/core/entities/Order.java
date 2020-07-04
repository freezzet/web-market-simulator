package stock.market.core.entities;

import stock.market.web.rest.data.OrderRequestData;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class Order {

    private Integer id;
    private BigDecimal price;
    private BigDecimal quantity;
    private Type type;
    private String symbol;
    private Date date;
    public Order(OrderRequestData orderRequest) {
        this(orderRequest.getPrice(), orderRequest.getQuantity(), orderRequest.getType(), orderRequest.getSymbol());
    }

    public Order(BigDecimal price, BigDecimal quantity, Type type, String symbol) {
        this.price = price;
        this.quantity = quantity;
        this.type = type;
        this.symbol = symbol;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public enum Type {
        BUY, SELL
    }

}
