package eu.projectp.webutils.sps.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eu.projectp.webutils.sps.Main;
import eu.projectp.webutils.sps.search.Searcher;
import eu.projectp.webutils.sps.util.Utils;

import java.io.IOException;

public class DefaultContextHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //System.out.println("[DefaultContextHandler/INFO] Request received from: " + exchange.getRemoteAddress());

        String site = Main.TIMETABLE_UPLOAD ? Utils.getWebPage("index0") : Utils.getWebPage("index");

        if (Searcher.webUntisOffline) {
            site = site.replaceAll("%info%", """
                    <h3 style="text-align: center; color: crimson; margin: 0px 0px;">WebUntis ist momentan nicht errichbar!</h3>
                    <p style="text-align: center; color: gray; margin: 0px 0px;">Daten können deswegen nicht <u>aktualisiert</u> werden.</p>
                    """);
        }
        site = site.replaceAll("%info%", "");

        Utils.respond(exchange, 200, site);
        //System.out.println("[DefaultContextHandler/INFO] Successfully sent response to: " + exchange.getRemoteAddress());
    }

}
