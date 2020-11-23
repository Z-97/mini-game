package org.chl.gateway.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.adapter.ReactorNettyWebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.websocket.WebsocketInbound;

import java.net.URI;

public class MyReactorNettyWebSocketClient extends ReactorNettyWebSocketClient {

    private static final Log logger = LogFactory.getLog(MyReactorNettyWebSocketClient.class);

    private final HttpClient httpClient;

    public MyReactorNettyWebSocketClient() {
        this(HttpClient.create());
    }

    public MyReactorNettyWebSocketClient(HttpClient httpClient) {
        Assert.notNull(httpClient, "HttpClient is required");
        this.httpClient = httpClient;
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public Mono<Void> execute(URI url, WebSocketHandler handler) {
        return this.execute(url, new HttpHeaders(), handler);
    }

    public Mono<Void> execute(URI url, HttpHeaders requestHeaders, WebSocketHandler handler) {
        return ((HttpClient.WebsocketSender)this.getHttpClient().headers((nettyHeaders) -> {
            this.setNettyHeaders(requestHeaders, nettyHeaders);
        }).websocket(StringUtils.collectionToCommaDelimitedString(handler.getSubProtocols()),524288).uri(url.toString())).handle((inbound, outbound) -> {
            HttpHeaders responseHeaders = this.toHttpHeaders(inbound);
            String protocol = responseHeaders.getFirst("Sec-WebSocket-Protocol");
            HandshakeInfo info = new HandshakeInfo(url, responseHeaders, Mono.empty(), protocol);
            NettyDataBufferFactory factory = new NettyDataBufferFactory(outbound.alloc());
            WebSocketSession session = new ReactorNettyWebSocketSession(inbound, outbound, info, factory);
            if (logger.isDebugEnabled()) {
                logger.debug("Started session '" + session.getId() + "' for " + url);
            }

            return handler.handle(session);
        }).doOnRequest((n) -> {
            if (logger.isDebugEnabled()) {
                logger.debug("Connecting to " + url);
            }

        }).next();
    }

    private void setNettyHeaders(HttpHeaders httpHeaders, io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        httpHeaders.forEach(nettyHeaders::set);
    }

    private HttpHeaders toHttpHeaders(WebsocketInbound inbound) {
        HttpHeaders headers = new HttpHeaders();
        io.netty.handler.codec.http.HttpHeaders nettyHeaders = inbound.headers();
        nettyHeaders.forEach((entry) -> {
            String name = (String)entry.getKey();
            headers.put(name, nettyHeaders.getAll(name));
        });
        return headers;
    }
}
