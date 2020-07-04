package stock.market.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import stock.market.core.Exchange;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class SpringBootStarter {

    private static Logger logger = LoggerFactory.getLogger(SpringBootStarter.class);

    private static ConfigurableApplicationContext ctx;

    public static void main(String[] args) {
        ctx = SpringApplication.run(SpringBootStarter.class, args);
    }

    @PostConstruct
    private void init() {
        if (!Exchange.getInstance().open()) {
            logger.error("The Exchange is not initialized");
            SpringApplication.exit(ctx, () -> 0);
        }
    }
}
