package com.application.stock.stockservice.resource;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.ribbon.proxy.annotation.Hystrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/stock")
public class StockResource {
     private static final Logger LOGGER= LoggerFactory.getLogger(StockResource.class);
    @Autowired
    RestTemplate restTemplate;

    @GetMapping("/{username}")
    public List<Quote> getStock(@PathVariable("username") final String userName)
    {
      //List<String> quotes= restTemplate.getForObject("http://localhost:8300/rest/db/"+userName, List.class);
      ResponseEntity<List<String>> quoteResponse =restTemplate.exchange("http://db-service/rest/db/" + userName, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>(){});
      List<String> quotes= quoteResponse.getBody();
      return quotes.stream()
              .map(quote-> {Stock stock= getStockPrice(quote);
                            return new Quote(quote, stock.getQuote().getPrice());
                           })
              .collect(Collectors.toList());
    }
    @HystrixCommand(fallbackMethod = "fallback")
    @GetMapping
    public String response() {
        LOGGER.info("Logs logged before calling DB-Service");
        String res= restTemplate.getForObject("http://db-service/rest/db/hello", String.class);
        LOGGER.info("Logs logged after calling DB-Service");
        return res;
    }
    public String fallback()
    {
        return "fallback mechanism started";
    }

    public Stock getStockPrice(String quote)
    { try {
        return YahooFinance.get(quote);
    } catch (IOException e) {
        e.printStackTrace();
        return new Stock(quote);
    }
    }

    private class Quote {
        private String quote;
        private BigDecimal price;
        public Quote(String quote, BigDecimal price) {
            this.quote= quote;
            this.price= price;
        }

        public String getQuote() {
            return quote;
        }

        public void setQuote(String quote) {
            this.quote = quote;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}