package org.zoooooway.spikedog;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author zoooooway
 */
public abstract class HttpConnector implements HttpHandler {


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpExchangeAdapter exchangeAdapter = new HttpExchangeAdapter(exchange);
        process(new HttpExchangeRequestImpl(exchangeAdapter), new HttpExchangeResponseImpl(exchangeAdapter));
    }


    public abstract void process(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
