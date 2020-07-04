package stock.market.web.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stock.market.core.entities.Order;
import stock.market.web.rest.data.OrderData;
import stock.market.web.rest.data.TradeData;
import stock.market.web.service.ExchangeService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/gateway/data")
public class MarketDataRestController {

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    private ExchangeService exchangeService;

    @Autowired
    MarketDataRestController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/trade")
    List<TradeData> getTrades() {
        return Stream.of(exchangeService.getTrades()).map(t -> new TradeData(t.getId(), t.getQuantity(), t.getPrice(), dateFormat.format(t.getDate()), t.getSymbol(), t.getType(), t.getStatus())).collect(Collectors.toList());
    }

    @GetMapping("/order")
    List<OrderData> getOrders(@RequestParam String symbol, @RequestParam Order.Type type) {
        List<OrderData> bestOrdersData = Collections.emptyList();
        List<Order> bestOrders = exchangeService.getBestOrders(type, symbol);

        if (bestOrders != null) {
            bestOrdersData = bestOrders.stream().map(order -> new OrderData(order.getPrice(), order.getQuantity())).collect(Collectors.toList());
        }
        return bestOrdersData;
    }
}
