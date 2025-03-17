package eu.projectp.webutils.sps.scrape;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import eu.projectp.webutils.sps.search.Searcher;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

public class Scraper {

    private boolean failed = false;

    public Document scrape() throws IOException, ParserConfigurationException, SAXException {
        failed = false;
        try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {
                @Override
                public void scriptException(HtmlPage htmlPage, ScriptException e) {
                    Searcher.webUntisOffline = true;
                    failed = true;
                }

                @Override
                public void timeoutError(HtmlPage htmlPage, long l, long l1) {
                    Searcher.webUntisOffline = true;
                    failed = true;
                }

                @Override
                public void malformedScriptURL(HtmlPage htmlPage, String s, MalformedURLException e) {
                    Searcher.webUntisOffline = true;
                    failed = true;
                }

                @Override
                public void loadScriptError(HtmlPage htmlPage, URL url, Exception e) {
                    Searcher.webUntisOffline = true;
                    failed = true;
                }

                @Override
                public void warn(String s, String s1, int i, String s2, int i1) {
                }
            });
            webClient.setCssErrorHandler(new SilentCssErrorHandler());
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

            java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
            java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

            String url = "https://nessa.webuntis.com/WebUntis/monitor?school=IGS-Buxtehude&monitorType=subst&format=Pausenhalle";
            HtmlPage page = webClient.getPage(url);

            webClient.waitForBackgroundJavaScript(10000);

            if (!failed) {
                Searcher.webUntisOffline = false;
            }

            return DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().parse(new InputSource(new StringReader(page.asXml())));
        }
    }

}
