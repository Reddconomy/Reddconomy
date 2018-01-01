package webbit_lite.netty;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import webbit_lite.HttpControl;
import webbit_lite.HttpHandler;
import webbit_lite.HttpRequest;
import webbit_lite.HttpResponse;
import webbit_lite.WebbitException;

import java.util.Iterator;
import java.util.concurrent.Executor;

public class NettyHttpControl implements HttpControl {

    private final Iterator<HttpHandler> handlerIterator;
    private final Executor executor;
    private final ChannelHandlerContext ctx;
    private final NettyHttpRequest webbitHttpRequest;
    private final org.jboss.netty.handler.codec.http.HttpRequest nettyHttpRequest;
    private final org.jboss.netty.handler.codec.http.HttpResponse nettyHttpResponse;
    private final Thread.UncaughtExceptionHandler exceptionHandler;
    private final Thread.UncaughtExceptionHandler ioExceptionHandler;

    private HttpRequest defaultRequest;
    private HttpResponse webbitHttpResponse;
    private HttpControl defaultControl;

    public NettyHttpControl(Iterator<HttpHandler> handlerIterator,
                            Executor executor,
                            ChannelHandlerContext ctx,
                            NettyHttpRequest webbitHttpRequest,
                            NettyHttpResponse webbitHttpResponse,
                            org.jboss.netty.handler.codec.http.HttpRequest nettyHttpRequest,
                            org.jboss.netty.handler.codec.http.HttpResponse nettyHttpResponse,
                            Thread.UncaughtExceptionHandler exceptionHandler,
                            Thread.UncaughtExceptionHandler ioExceptionHandler) {
        this.handlerIterator = handlerIterator;
        this.executor = executor;
        this.ctx = ctx;
        this.webbitHttpRequest = webbitHttpRequest;
        this.webbitHttpResponse = webbitHttpResponse;
        this.nettyHttpRequest = nettyHttpRequest;
        this.nettyHttpResponse = nettyHttpResponse;
        this.ioExceptionHandler = ioExceptionHandler;
        this.exceptionHandler = exceptionHandler;

        defaultRequest = webbitHttpRequest;
        defaultControl = this;
    }

    @Override
    public void nextHandler() {
        nextHandler(defaultRequest, webbitHttpResponse, defaultControl);
    }

    @Override
    public void nextHandler(HttpRequest request, HttpResponse response) {
        nextHandler(request, response, defaultControl);
    }

    @Override
    public void nextHandler(HttpRequest request, HttpResponse response, HttpControl control) {
        this.defaultRequest = request;
        this.webbitHttpResponse = response;
        this.defaultControl = control;
        if (handlerIterator.hasNext()) {
            HttpHandler handler = handlerIterator.next();
            try {
                handler.handleHttpRequest(request, response, control);
            } catch (Throwable e) {
                response.error(e);
            }
        } else {
            response.status(404).end();
        }
    }

   



    @Override
    public Executor handlerExecutor() {
        return executor;
    }

    @Override
    public void execute(Runnable command) {
        handlerExecutor().execute(command);
    }

    private void performEventSourceHandshake(ChannelHandler eventSourceConnectionHandler) {
        nettyHttpResponse.setStatus(HttpResponseStatus.OK);
        nettyHttpResponse.addHeader("Content-Type", "text/event-stream");
        nettyHttpResponse.addHeader("Transfer-Encoding", "identity");
        nettyHttpResponse.addHeader("Connection", "keep-alive");
        nettyHttpResponse.addHeader("Cache-Control", "no-cache");
        nettyHttpResponse.setChunked(false);
        ctx.getChannel().write(nettyHttpResponse);
        getReadyToSendEventSourceMessages(eventSourceConnectionHandler);
    }

    private void getReadyToSendEventSourceMessages(ChannelHandler eventSourceConnectionHandler) {
        ChannelPipeline p = ctx.getChannel().getPipeline();
        StaleConnectionTrackingHandler staleConnectionTracker = (StaleConnectionTrackingHandler) p.remove("staleconnectiontracker");
        staleConnectionTracker.stopTracking(ctx.getChannel());
        p.remove("aggregator");
        p.replace("handler", "ssehandler", eventSourceConnectionHandler);
    }



    private void getReadyToReceiveWebSocketMessages(ChannelHandler webSocketFrameDecoder, ChannelHandler webSocketConnectionHandler, ChannelPipeline p, Channel channel) {
        StaleConnectionTrackingHandler staleConnectionTracker = (StaleConnectionTrackingHandler) p.remove("staleconnectiontracker");
        staleConnectionTracker.stopTracking(channel);
        p.remove("aggregator");
        p.replace("decoder", "wsdecoder", webSocketFrameDecoder);
        p.replace("handler", "wshandler", webSocketConnectionHandler);
    }

    private void getReadyToSendWebSocketMessages(ChannelHandler webSocketFrameEncoder, ChannelPipeline p) {
        p.replace("encoder", "wsencoder", webSocketFrameEncoder);
    }

}
