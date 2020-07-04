package stock.market.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import stock.market.core.entities.Order;
import stock.market.web.rest.data.EngineStats;
import stock.market.web.rest.data.OrderData;
import stock.market.web.rest.data.OrderRequestData;
import stock.market.web.rest.data.TradeData;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RestAPITest extends AbstractTest {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    public void getEngineStats() throws Exception {
        String uri = "/api/v1/engine/stats";

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON);
        ResultActions result = mvc.perform(requestBuilder);
        MvcResult mvcResult = result.andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
        String content = mvcResult.getResponse().getContentAsString();
        mapFromJson(content, EngineStats.class);
    }

    @Test
    public void getTradeTest() throws Exception {
        this.sendOrderRequest(new BigDecimal(10), new BigDecimal(10), Order.Type.BUY, "get-trade-test");
        this.sendOrderRequest(new BigDecimal(10), new BigDecimal(10), Order.Type.SELL, "get-trade-test");
        int attempts = 5;
        TradeData[] trades = new TradeData[0];
        String targetUri = "/api/v1/gateway/data/trade";
        while (trades.length == 0 && attempts-- > 0) {
            // since there is a server delay before the orders are considered accepted and matched, we should wait
            Thread.sleep(500);
            ResultActions result = mvc.perform(MockMvcRequestBuilders.get(targetUri).param("symbol", "get-order-test").param("type", "BUY"));
            MvcResult mvcResult = result.andReturn();

            int status = mvcResult.getResponse().getStatus();
            assertEquals(200, status);
            String content = mvcResult.getResponse().getContentAsString();
            trades = mapFromJson(content, TradeData[].class);
        }

        assertTrue("The response doesn't contain the entity", trades.length > 0);
    }

    @Test
    public void addOrders() throws Exception {
        int status = sendOrderRequest(new BigDecimal(5), new BigDecimal(10), Order.Type.BUY, "add-order-test");
        assertEquals(201, status);
    }

    @Test
    public void getOrders() throws Exception {

        this.sendOrderRequest(new BigDecimal(5), new BigDecimal(10), Order.Type.BUY, "get-order-test");

        String targetUri = "/api/v1/gateway/data/order";

        int attempts = 5;
        OrderData[] orders = new OrderData[0];
        while (orders.length == 0 && attempts-- > 0) {
            // since there is a server delay before the order is considered accepted, we should wait
            Thread.sleep(500);
            ResultActions result = mvc.perform(MockMvcRequestBuilders.get(targetUri).param("symbol", "get-order-test").param("type", "BUY"));
            MvcResult mvcResult = result.andReturn();

            int status = mvcResult.getResponse().getStatus();
            assertEquals(200, status);
            String content = mvcResult.getResponse().getContentAsString();
            orders = mapFromJson(content, OrderData[].class);
        }

        assertTrue("The response doesn't contain the entity", orders.length > 0);
    }

    private int sendOrderRequest(BigDecimal price, BigDecimal quantity, Order.Type type, String symbol) throws Exception {
        String uri = "/api/v1/gateway/trading/order";

        OrderRequestData orderRequest = new OrderRequestData(price, quantity, type, symbol);

        MvcResult result = mvc.perform(MockMvcRequestBuilders.post(uri).contentType(MediaType.APPLICATION_JSON).content(super.mapToJson(orderRequest))
                .accept(MediaType.APPLICATION_JSON)).andReturn();
        return result.getResponse().getStatus();
    }
}
