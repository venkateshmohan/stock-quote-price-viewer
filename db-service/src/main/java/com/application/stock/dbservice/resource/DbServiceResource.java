package com.application.stock.dbservice.resource;

import com.application.stock.dbservice.model.Quote;
import com.application.stock.dbservice.model.Quotes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.application.stock.dbservice.repository.QuotesRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/rest/db")
public class DbServiceResource {
     private static final Logger LOGGER= LoggerFactory.getLogger(DbServiceResource.class);
    private QuotesRepository quotesRepository;

    public DbServiceResource(QuotesRepository quotesRepository) {
        this.quotesRepository = quotesRepository;
    }

    @GetMapping(value= "/hello")
    public String hello()
    {   LOGGER.info("Reached the Server");
        return "Welcome to DB Service";
    }
    @GetMapping(value = "/{userName}")
    public List<String> getQuotes(@PathVariable("userName") final String username)
    {
        return getQuotesByUserName(username);
    }

    @PostMapping(value= "/add")
    public List<String> add(@RequestBody final Quotes quotes)
    {
        quotes.getQuotes()
                .stream()
                .forEach(quote->{
                    quotesRepository.save(new Quote(quotes.getUserName(),quote));
                });
        return getQuotesByUserName(quotes.getUserName());
    }
    @PostMapping("/delete/{username}")
    public List<String> delete(@PathVariable("username") final String username)
    {
        List<Quote> quotes= quotesRepository.findByUserName(username);
        quotes.stream()
                .forEach(quote -> {
                    quotesRepository.delete(quote);
                });
        return getQuotesByUserName(username);
    }
    private List<String> getQuotesByUserName(@PathVariable("userName") String username) {
        return quotesRepository.findByUserName(username)
                .stream()
                .map(Quote::getQuote)
                .collect(Collectors.toList());
    }
}


