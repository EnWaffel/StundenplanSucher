package eu.projectp.webutils.sps.util;

import com.sun.net.httpserver.HttpExchange;
import eu.projectp.webutils.sps.Main;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;

public final class Utils {

    private static final HashMap<String, String> CACHED_PAGES = new HashMap<>();

    public static String getWebPage(String name) {
        String site = getErrorPage("ERROR (1): Failed to read file: " + name + ".html");
        try {
            if (CACHED_PAGES.containsKey(name)) {
                return CACHED_PAGES.get(name);
            }

            if (Main.SYSTEM_RESOURCES) {
                site = IOUtils.toString(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(name + ".html")), StandardCharsets.UTF_8);
            } else {
                site = IOUtils.toString(new BufferedInputStream(Files.newInputStream(Path.of(name + ".html"))), StandardCharsets.UTF_8);
            }

            CACHED_PAGES.put(name, site);
        } catch (IOException e) {
            System.err.println("[Utils/ERROR] Failed to read " + name + ".html!");
        }
        return site;
    }

    public static String getErrorPage(String message) {
        return """
                <!DOCTYPE html>
                <html lang="de">
                <head>
                    <meta charset="UTF-8">
                    <meta http-equiv="X-UA-Compatible" content="IE=edge">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>SPS Error</title>
                </head>
                <body>
                    <h3><b style="color: red;">Ein unerwateter Fehler ist aufgetreten!</b></h3>
                    <h5>%msg%</h5>
                </body>
                """.replaceAll("%msg%", message);
    }

    public static void respond(HttpExchange exchange, int code, String response) throws IOException {
        byte[] responseBuffer = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, responseBuffer.length);
        exchange.getResponseBody().write(responseBuffer);
        exchange.getResponseBody().flush();
        exchange.close();
    }

    //<p>Wenn dieser Felher öfter auftreten sollte, melde bitte diese Fehler-Nachricht.</p>

}
