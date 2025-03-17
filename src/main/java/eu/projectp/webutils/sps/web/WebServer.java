package eu.projectp.webutils.sps.web;

import com.sun.net.httpserver.HttpServer;
import eu.projectp.webutils.sps.search.Searcher;
import eu.projectp.webutils.sps.util.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;

public final class WebServer {

    private static HttpServer server;

    public static void start() {
        System.out.println("[Server/INFO] Loading...");

        System.out.println("[Server/INFO] Starting...");
        try {
            server = HttpServer.create(new InetSocketAddress(25555), 0);
            server.createContext("/", new DefaultContextHandler());
            server.createContext("/search", new SearchContextHandler());
            server.createContext("/display_all_classes", new SACContextHandler());
            server.createContext("/:)", exchange -> Utils.respond(exchange, 200, Utils.getWebPage("rick")));
            server.start();
            Searcher.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("[Server/INFO] Started!");
    }

    public static void stop() {
        System.out.println("[Server/INFO] Stopping...");
        server.stop(0);
        System.out.println("[Server/INFO] Stopped!");
    }

}
