package eu.projectp.webutils.sps.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eu.projectp.webutils.sps.search.Searcher;
import eu.projectp.webutils.sps.util.Result;
import eu.projectp.webutils.sps.util.Utils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchContextHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            //System.out.println("[SearchContextHandler/INFO] Request received from: " + exchange.getRemoteAddress());
            String[] split = exchange.getRequestURI().getQuery().split("=");
            if (split.length < 2) {
                Utils.respond(exchange, 200, Utils.getWebPage("empty_text_field"));
                return;
            }

            Pattern pattern = Pattern.compile("_(.*?)(?:\\?.*|$)");
            Matcher matcher = pattern.matcher(exchange.getRequestURI().getPath().substring(1));

            String day = "today";
            if (matcher.find()) {
                day = matcher.group().substring(1);
            }

            Result result = Searcher.search(split[1], day);
            String resultStr = result.success() ? result.result() : result.error();

            Utils.respond(exchange, 200, resultStr);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.respond(exchange, 500, Result.getErrorFromException(e).error());
        }
    }

}
