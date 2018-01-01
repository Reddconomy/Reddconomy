package webbit_lite.handler;

import org.jboss.netty.handler.codec.http.HttpHeaders;

import webbit_lite.HttpControl;
import webbit_lite.HttpHandler;
import webbit_lite.HttpRequest;
import webbit_lite.HttpResponse;

import java.util.Date;

/**
 * Handler that sets the HTTP 'Server' response header.
 */
public class DateHeaderHandler implements HttpHandler {

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        if (!response.containsHeader(HttpHeaders.Names.DATE)) {
            response.header(HttpHeaders.Names.DATE, new Date());
        }
        control.nextHandler();
    }
}
