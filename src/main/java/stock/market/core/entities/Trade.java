package stock.market.core.entities;

import java.math.BigDecimal;
import java.util.Date;

public class Trade extends Order {
    public enum Status {
        Partial, Full
    }

    private Integer id;
    private Status status;

    public Trade(int id, Order order, BigDecimal price, BigDecimal quantity, Date time) {
        super(price, quantity, order.getType(), order.getSymbol());
        super.setId(order.getId());
        super.setDate(time);
        this.id = id;
        this.status = order.getQuantity().compareTo(BigDecimal.ZERO) > 0 ? Status.Partial : Status.Full;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderId() {
        return super.getId();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


}
