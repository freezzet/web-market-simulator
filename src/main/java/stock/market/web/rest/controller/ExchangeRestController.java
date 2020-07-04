package stock.market.web.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stock.market.web.rest.data.EngineStats;
import stock.market.web.service.ExchangeService;

@RestController
@RequestMapping("/api/v1/engine")
public class ExchangeRestController {

    private ExchangeService exchangeService;

    @Autowired
    ExchangeRestController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/stats")
    EngineStats getEngineStats() {
        return exchangeService.getEngineStats();
    }

}
