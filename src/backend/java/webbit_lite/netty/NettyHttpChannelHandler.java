package webbit_lite.netty;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;

import webbit_lite.HttpControl;
import webbit_lite.HttpHandler;
import webbit_lite.WebbitException;

public class NettyHttpChannelHandler extends SimpleChannelUpstreamHandler {
    private static final Object IGNORE_REQUEST = new Object();

    private final Executor executor;
    private final List<HttpHandler> httpHandlers;
    private final Object id;
    private final long timestamp;
    private final Thread.UncaughtExceptionHandler exceptionHandler;
    private final Thread.UncaughtExceptionHandler ioExceptionHandler;
    private final ConnectionHelper connectionHelper;

    public NettyHttpChannelHandler(Executor executor,
                                   List<HttpHandler> httpHandlers,
                                   Object id,
                                   long timestamp,
                                   Thread.UncaughtExceptionHandler exceptionHandler,
                                   Thread.UncaughtExceptionHandler ioExceptionHandler) {
        this.executor = executor;
        this.httpHandlers = httpHandlers;
        this.id = id;
        this.timestamp = timestamp;
        this.exceptionHandler = exceptionHandler;
        this.ioExceptionHandler = ioExceptionHandler;

        connectionHelper = new ConnectionHelper(executor, exceptionHandler, ioExceptionHandler) {
            @Override
            protected void fireOnClose() throws Exception {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {
        if (messageEvent.getMessage() instanceof HttpRequest && ctx.getAttachment() != IGNORE_REQUEST) {
            handleHttpRequest(ctx, messageEvent, (HttpRequest) messageEvent.getMessage());
        } else {
            super.messageReceived(ctx, messageEvent);
        }
    }

    private void handleHttpRequest(final ChannelHandlerContext ctx, MessageEvent messageEvent, HttpRequest httpRequest) {
        final NettyHttpRequest nettyHttpRequest = new NettyHttpRequest(messageEvent, httpRequest, id, timestamp);
        DefaultHttpResponse ok_200 = new DefaultHttpResponse(HTTP_1_1, OK);
        final NettyHttpResponse nettyHttpResponse = new NettyHttpResponse(
                ctx, ok_200, isKeepAlive(httpRequest), exceptionHandler);
        Iterator<HttpHandler> httpHandlers = this.httpHandlers.iterator();
        final HttpControl control = new NettyHttpControl(httpHandlers, executor, ctx,
                nettyHttpRequest, nettyHttpResponse, httpRequest, ok_200,
                exceptionHandler, ioExceptionHandler);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    control.nextHandler(nettyHttpRequest, nettyHttpResponse);
                } catch (Exception exception) {
                    exceptionHandler.uncaughtException(Thread.currentThread(), WebbitException.fromException(exception, ctx.getChannel()));
                }
            }
        });
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) {
        connectionHelper.fireConnectionException(e);
        final Channel channel = ctx.getChannel();
        final Throwable cause = e.getCause();
        if ( ! (cause instanceof IOException) ) {
            if ( channel.isOpen() ) {
                final NettyHttpResponse nettyHttpResponse = new NettyHttpResponse(ctx, new DefaultHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR), true, exceptionHandler);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ctx.setAttachment(IGNORE_REQUEST);
                            nettyHttpResponse.error(e.getCause());
                        } catch (Exception exception) {
                            exceptionHandler.uncaughtException(Thread.currentThread(), WebbitException.fromException(exception, channel));
                        }
                    }
                });
            } else {
                exceptionHandler.uncaughtException(
                    Thread.currentThread(),
                    WebbitException.fromException(cause, channel)
                );
            }
        }
    }

}
