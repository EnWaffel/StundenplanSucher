package eu.projectp.webutils.sps.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eu.projectp.webutils.sps.search.Searcher;
import eu.projectp.webutils.sps.util.Result;
import eu.projectp.webutils.sps.util.Utils;

import java.io.IOException;

// Show All Classes Context Handler
public class SACContextHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            //System.out.println("[SACContextHandler/INFO] Request received from: " + exchange.getRemoteAddress());
            String day = exchange.getRequestURI().getPath().substring(1).split("_")[3];

            Result result = Searcher.searchClasses(day);
            String resultStr = result.success() ? result.result() : result.error();

            Utils.respond(exchange, 200, resultStr);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.respond(exchange, 500, Result.getErrorFromException(e).error());
        }
    }

}
