package eu.projectp.webutils.sps;

import eu.projectp.webutils.sps.web.WebServer;

public class Main {

    public static final boolean SYSTEM_RESOURCES = false;
    public static final boolean TIMETABLE_UPLOAD = false;

    public static void main(String[] args) {
        WebServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(WebServer::stop));
    }

}
