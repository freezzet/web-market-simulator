package stock.market.web.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import stock.market.core.gateway.exception.TradingGatewayNotOpenException;
import stock.market.core.gateway.exception.TradingGatewayTimeoutException;
import stock.market.web.rest.data.OrderRequestData;
import stock.market.web.service.ExchangeService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/gateway/trading")
public class TradingRestController {

    ExchangeService exchangeService;

    @Autowired
    TradingRestController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @PostMapping("/order")
    @ResponseStatus(HttpStatus.CREATED)
    void addOrder(@RequestBody @Valid OrderRequestData order) throws TradingGatewayTimeoutException, TradingGatewayNotOpenException {
        exchangeService.offerOrder(order);
    }

}
