package org.dice_research.ldcbench.util.uri;


import java.net.InetAddress;
import java.net.URI;

public class CrawleableUriFactory4Tests extends CrawleableUriFactoryImpl {

    public CrawleableUri create(URI uri, InetAddress ipAddress, UriType type) {
        return new CrawleableUri(uri, ipAddress, type);
    }

}
