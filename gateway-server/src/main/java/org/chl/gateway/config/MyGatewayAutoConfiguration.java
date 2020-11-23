package org.chl.gateway.config;

import com.netflix.hystrix.HystrixObservableCommand;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.cloud.gateway.actuate.GatewayControllerEndpoint;
import org.springframework.cloud.gateway.config.*;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.cloud.gateway.filter.factory.*;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.headers.ForwardedHeadersFilter;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.cloud.gateway.filter.headers.RemoveHopByHopHeadersFilter;
import org.springframework.cloud.gateway.filter.headers.XForwardedHeadersFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.PrincipalNameKeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.gateway.handler.predicate.*;
import org.springframework.cloud.gateway.route.*;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.StringToZonedDateTimeConverter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.ProxyProvider;
import rx.RxReactiveStreams;

import java.security.cert.X509Certificate;
import java.util.List;

@Configuration
@ConditionalOnProperty(
        name = {"spring.cloud.gateway.enabled"},
        matchIfMissing = true
)
@EnableConfigurationProperties
@AutoConfigureBefore({HttpHandlerAutoConfiguration.class, WebFluxAutoConfiguration.class})
@AutoConfigureAfter({GatewayLoadBalancerClientAutoConfiguration.class, GatewayClassPathWarningAutoConfiguration.class})
@ConditionalOnClass({DispatcherHandler.class})
public class MyGatewayAutoConfiguration {
    public MyGatewayAutoConfiguration() {
    }

    @Bean
    public StringToZonedDateTimeConverter stringToZonedDateTimeConverter() {
        return new StringToZonedDateTimeConverter();
    }

    @Bean
    public RouteLocatorBuilder routeLocatorBuilder(ConfigurableApplicationContext context) {
        return new RouteLocatorBuilder(context);
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertiesRouteDefinitionLocator propertiesRouteDefinitionLocator(GatewayProperties properties) {
        return new PropertiesRouteDefinitionLocator(properties);
    }

    @Bean
    @ConditionalOnMissingBean({RouteDefinitionRepository.class})
    public InMemoryRouteDefinitionRepository inMemoryRouteDefinitionRepository() {
        return new InMemoryRouteDefinitionRepository();
    }

    @Bean
    @Primary
    public RouteDefinitionLocator routeDefinitionLocator(List<RouteDefinitionLocator> routeDefinitionLocators) {
        return new CompositeRouteDefinitionLocator(Flux.fromIterable(routeDefinitionLocators));
    }

    @Bean
    public RouteLocator routeDefinitionRouteLocator(GatewayProperties properties, List<GatewayFilterFactory> GatewayFilters, List<RoutePredicateFactory> predicates, RouteDefinitionLocator routeDefinitionLocator, @Qualifier("webFluxConversionService") ConversionService conversionService) {
        return new RouteDefinitionRouteLocator(routeDefinitionLocator, predicates, GatewayFilters, properties, conversionService);
    }

    @Bean
    @Primary
    public RouteLocator cachedCompositeRouteLocator(List<RouteLocator> routeLocators) {
        return new CachingRouteLocator(new CompositeRouteLocator(Flux.fromIterable(routeLocators)));
    }

    @Bean
    public RouteRefreshListener routeRefreshListener(ApplicationEventPublisher publisher) {
        return new RouteRefreshListener(publisher);
    }

    @Bean
    public FilteringWebHandler filteringWebHandler(List<GlobalFilter> globalFilters) {
        return new FilteringWebHandler(globalFilters);
    }

    @Bean
    public GlobalCorsProperties globalCorsProperties() {
        return new GlobalCorsProperties();
    }

    @Bean
    public RoutePredicateHandlerMapping routePredicateHandlerMapping(FilteringWebHandler webHandler, RouteLocator routeLocator, GlobalCorsProperties globalCorsProperties, Environment environment) {
        return new RoutePredicateHandlerMapping(webHandler, routeLocator, globalCorsProperties, environment);
    }

    @Bean
    public GatewayProperties gatewayProperties() {
        return new GatewayProperties();
    }

    @Bean
    public SecureHeadersProperties secureHeadersProperties() {
        return new SecureHeadersProperties();
    }

    @Bean
    @ConditionalOnProperty(
            name = {"spring.cloud.gateway.forwarded.enabled"},
            matchIfMissing = true
    )
    public ForwardedHeadersFilter forwardedHeadersFilter() {
        return new ForwardedHeadersFilter();
    }

    @Bean
    public RemoveHopByHopHeadersFilter removeHopByHopHeadersFilter() {
        return new RemoveHopByHopHeadersFilter();
    }

    @Bean
    @ConditionalOnProperty(
            name = {"spring.cloud.gateway.x-forwarded.enabled"},
            matchIfMissing = true
    )
    public XForwardedHeadersFilter xForwardedHeadersFilter() {
        return new XForwardedHeadersFilter();
    }

    @Bean
    public AdaptCachedBodyGlobalFilter adaptCachedBodyGlobalFilter() {
        return new AdaptCachedBodyGlobalFilter();
    }

    @Bean
    public RemoveCachedBodyFilter removeCachedBodyFilter() {
        return new RemoveCachedBodyFilter();
    }

    @Bean
    public RouteToRequestUrlFilter routeToRequestUrlFilter() {
        return new RouteToRequestUrlFilter();
    }

    @Bean
    public ForwardRoutingFilter forwardRoutingFilter(ObjectProvider<DispatcherHandler> dispatcherHandler) {
        return new ForwardRoutingFilter(dispatcherHandler);
    }

    @Bean
    public ForwardPathFilter forwardPathFilter() {
        return new ForwardPathFilter();
    }

    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService();
    }

    @Bean
    public WebsocketRoutingFilter websocketRoutingFilter(WebSocketClient webSocketClient, WebSocketService webSocketService, ObjectProvider<List<HttpHeadersFilter>> headersFilters) {
        return new WebsocketRoutingFilter(webSocketClient, webSocketService, headersFilters);
    }

    @Bean
    public WeightCalculatorWebFilter weightCalculatorWebFilter(@Qualifier("webFluxValidator") Validator validator, ObjectProvider<RouteLocator> routeLocator) {
        return new WeightCalculatorWebFilter(validator, routeLocator);
    }

    @Bean
    public AfterRoutePredicateFactory afterRoutePredicateFactory() {
        return new AfterRoutePredicateFactory();
    }

    @Bean
    public BeforeRoutePredicateFactory beforeRoutePredicateFactory() {
        return new BeforeRoutePredicateFactory();
    }

    @Bean
    public BetweenRoutePredicateFactory betweenRoutePredicateFactory() {
        return new BetweenRoutePredicateFactory();
    }

    @Bean
    public CookieRoutePredicateFactory cookieRoutePredicateFactory() {
        return new CookieRoutePredicateFactory();
    }

    @Bean
    public HeaderRoutePredicateFactory headerRoutePredicateFactory() {
        return new HeaderRoutePredicateFactory();
    }

    @Bean
    public HostRoutePredicateFactory hostRoutePredicateFactory() {
        return new HostRoutePredicateFactory();
    }

    @Bean
    public MethodRoutePredicateFactory methodRoutePredicateFactory() {
        return new MethodRoutePredicateFactory();
    }

    @Bean
    public PathRoutePredicateFactory pathRoutePredicateFactory() {
        return new PathRoutePredicateFactory();
    }

    @Bean
    public QueryRoutePredicateFactory queryRoutePredicateFactory() {
        return new QueryRoutePredicateFactory();
    }

    @Bean
    public ReadBodyPredicateFactory readBodyPredicateFactory() {
        return new ReadBodyPredicateFactory();
    }

    @Bean
    public RemoteAddrRoutePredicateFactory remoteAddrRoutePredicateFactory() {
        return new RemoteAddrRoutePredicateFactory();
    }

    @Bean
    @DependsOn({"weightCalculatorWebFilter"})
    public WeightRoutePredicateFactory weightRoutePredicateFactory() {
        return new WeightRoutePredicateFactory();
    }

    @Bean
    public CloudFoundryRouteServiceRoutePredicateFactory cloudFoundryRouteServiceRoutePredicateFactory() {
        return new CloudFoundryRouteServiceRoutePredicateFactory();
    }

    @Bean
    public AddRequestHeaderGatewayFilterFactory addRequestHeaderGatewayFilterFactory() {
        return new AddRequestHeaderGatewayFilterFactory();
    }

    @Bean
    public AddRequestParameterGatewayFilterFactory addRequestParameterGatewayFilterFactory() {
        return new AddRequestParameterGatewayFilterFactory();
    }

    @Bean
    public AddResponseHeaderGatewayFilterFactory addResponseHeaderGatewayFilterFactory() {
        return new AddResponseHeaderGatewayFilterFactory();
    }

    @Bean
    public ModifyRequestBodyGatewayFilterFactory modifyRequestBodyGatewayFilterFactory() {
        return new ModifyRequestBodyGatewayFilterFactory();
    }

    @Bean
    public DedupeResponseHeaderGatewayFilterFactory dedupeResponseHeaderGatewayFilterFactory() {
        return new DedupeResponseHeaderGatewayFilterFactory();
    }

    @Bean
    public ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory() {
        return new ModifyResponseBodyGatewayFilterFactory();
    }

    @Bean
    public PrefixPathGatewayFilterFactory prefixPathGatewayFilterFactory() {
        return new PrefixPathGatewayFilterFactory();
    }

    @Bean
    public PreserveHostHeaderGatewayFilterFactory preserveHostHeaderGatewayFilterFactory() {
        return new PreserveHostHeaderGatewayFilterFactory();
    }

    @Bean
    public RedirectToGatewayFilterFactory redirectToGatewayFilterFactory() {
        return new RedirectToGatewayFilterFactory();
    }

    @Bean
    public RemoveRequestHeaderGatewayFilterFactory removeRequestHeaderGatewayFilterFactory() {
        return new RemoveRequestHeaderGatewayFilterFactory();
    }

    @Bean
    public RemoveResponseHeaderGatewayFilterFactory removeResponseHeaderGatewayFilterFactory() {
        return new RemoveResponseHeaderGatewayFilterFactory();
    }

    @Bean(
            name = {"principalNameKeyResolver"}
    )
    @ConditionalOnBean({RateLimiter.class})
    @ConditionalOnMissingBean({KeyResolver.class})
    public PrincipalNameKeyResolver principalNameKeyResolver() {
        return new PrincipalNameKeyResolver();
    }

    @Bean
    @ConditionalOnBean({RateLimiter.class, KeyResolver.class})
    public RequestRateLimiterGatewayFilterFactory requestRateLimiterGatewayFilterFactory(RateLimiter rateLimiter, KeyResolver resolver) {
        return new RequestRateLimiterGatewayFilterFactory(rateLimiter, resolver);
    }

    @Bean
    public RewritePathGatewayFilterFactory rewritePathGatewayFilterFactory() {
        return new RewritePathGatewayFilterFactory();
    }

    @Bean
    public RetryGatewayFilterFactory retryGatewayFilterFactory() {
        return new RetryGatewayFilterFactory();
    }

    @Bean
    public SetPathGatewayFilterFactory setPathGatewayFilterFactory() {
        return new SetPathGatewayFilterFactory();
    }

    @Bean
    public SecureHeadersGatewayFilterFactory secureHeadersGatewayFilterFactory(SecureHeadersProperties properties) {
        return new SecureHeadersGatewayFilterFactory(properties);
    }

    @Bean
    public SetRequestHeaderGatewayFilterFactory setRequestHeaderGatewayFilterFactory() {
        return new SetRequestHeaderGatewayFilterFactory();
    }

    @Bean
    public SetResponseHeaderGatewayFilterFactory setResponseHeaderGatewayFilterFactory() {
        return new SetResponseHeaderGatewayFilterFactory();
    }

    @Bean
    public RewriteResponseHeaderGatewayFilterFactory rewriteResponseHeaderGatewayFilterFactory() {
        return new RewriteResponseHeaderGatewayFilterFactory();
    }

    @Bean
    public SetStatusGatewayFilterFactory setStatusGatewayFilterFactory() {
        return new SetStatusGatewayFilterFactory();
    }

    @Bean
    public SaveSessionGatewayFilterFactory saveSessionGatewayFilterFactory() {
        return new SaveSessionGatewayFilterFactory();
    }

    @Bean
    public StripPrefixGatewayFilterFactory stripPrefixGatewayFilterFactory() {
        return new StripPrefixGatewayFilterFactory();
    }

    @Bean
    public RequestHeaderToRequestUriGatewayFilterFactory requestHeaderToRequestUriGatewayFilterFactory() {
        return new RequestHeaderToRequestUriGatewayFilterFactory();
    }

    @Bean
    public RequestSizeGatewayFilterFactory requestSizeGatewayFilterFactory() {
        return new RequestSizeGatewayFilterFactory();
    }

    @Configuration
    @ConditionalOnClass({Health.class})
    protected static class GatewayActuatorConfiguration {
        protected GatewayActuatorConfiguration() {
        }

        @Bean
        @ConditionalOnEnabledEndpoint
        public GatewayControllerEndpoint gatewayControllerEndpoint(RouteDefinitionLocator routeDefinitionLocator, List<GlobalFilter> globalFilters, List<GatewayFilterFactory> GatewayFilters, RouteDefinitionWriter routeDefinitionWriter, RouteLocator routeLocator) {
            return new GatewayControllerEndpoint(routeDefinitionLocator, globalFilters, GatewayFilters, routeDefinitionWriter, routeLocator);
        }
    }

    @Configuration
    @ConditionalOnClass({HystrixObservableCommand.class, RxReactiveStreams.class})
    protected static class HystrixConfiguration {
        protected HystrixConfiguration() {
        }

        @Bean
        public HystrixGatewayFilterFactory hystrixGatewayFilterFactory(ObjectProvider<DispatcherHandler> dispatcherHandler) {
            return new HystrixGatewayFilterFactory(dispatcherHandler);
        }

        @Bean
        public FallbackHeadersGatewayFilterFactory fallbackHeadersGatewayFilterFactory() {
            return new FallbackHeadersGatewayFilterFactory();
        }
    }

    @Configuration
    @ConditionalOnClass({HttpClient.class})
    protected static class NettyConfiguration {
        protected NettyConfiguration() {
        }

        @Bean
        @ConditionalOnMissingBean
        public HttpClient httpClient(HttpClientProperties properties) {
            HttpClientProperties.Pool pool = properties.getPool();
            ConnectionProvider connectionProvider;
            if (pool.getType() == HttpClientProperties.Pool.PoolType.DISABLED) {
                connectionProvider = ConnectionProvider.newConnection();
            } else if (pool.getType() == HttpClientProperties.Pool.PoolType.FIXED) {
                connectionProvider = ConnectionProvider.fixed(pool.getName(), pool.getMaxConnections(), pool.getAcquireTimeout());
            } else {
                connectionProvider = ConnectionProvider.elastic(pool.getName());
            }

            HttpClient httpClient = HttpClient.create(connectionProvider).tcpConfiguration((tcpClient) -> {
                if (properties.getConnectTimeout() != null) {
                    tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeout());
                }

                HttpClientProperties.Proxy proxy = properties.getProxy();
                if (StringUtils.hasText(proxy.getHost())) {
                    tcpClient = tcpClient.proxy((proxySpec) -> {
                        ProxyProvider.Builder builder = proxySpec.type(ProxyProvider.Proxy.HTTP).host(proxy.getHost());
                        PropertyMapper map = PropertyMapper.get();
                        proxy.getClass();
                        map.from(proxy::getPort).whenNonNull().to(builder::port);
                        proxy.getClass();
                        map.from(proxy::getUsername).whenHasText().to(builder::username);
                        proxy.getClass();
                        map.from(proxy::getPassword).whenHasText().to((password) -> {
                            builder.password((s) -> {
                                return password;
                            });
                        });
                        proxy.getClass();
                        map.from(proxy::getNonProxyHostsPattern).whenHasText().to(builder::nonProxyHosts);
                    });
                }

                return tcpClient;
            });
            HttpClientProperties.Ssl ssl = properties.getSsl();
            if (ssl.getTrustedX509CertificatesForTrustManager().length > 0 || ssl.isUseInsecureTrustManager()) {
                httpClient = httpClient.secure((sslContextSpec) -> {
                    SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
                    X509Certificate[] trustedX509Certificates = ssl.getTrustedX509CertificatesForTrustManager();
                    if (trustedX509Certificates.length > 0) {
                        sslContextBuilder.trustManager(trustedX509Certificates);
                    } else if (ssl.isUseInsecureTrustManager()) {
                        sslContextBuilder.trustManager(InsecureTrustManagerFactory.INSTANCE);
                    }

                    sslContextSpec.sslContext(sslContextBuilder).defaultConfiguration(ssl.getDefaultConfigurationType()).handshakeTimeout(ssl.getHandshakeTimeout()).closeNotifyFlushTimeout(ssl.getCloseNotifyFlushTimeout()).closeNotifyReadTimeout(ssl.getCloseNotifyReadTimeout());
                });
            }

            return httpClient;
        }

        @Bean
        public HttpClientProperties httpClientProperties() {
            return new HttpClientProperties();
        }

        @Bean
        public NettyRoutingFilter routingFilter(HttpClient httpClient, ObjectProvider<List<HttpHeadersFilter>> headersFilters, HttpClientProperties properties) {
            return new NettyRoutingFilter(httpClient, headersFilters, properties);
        }

        @Bean
        public NettyWriteResponseFilter nettyWriteResponseFilter(GatewayProperties properties) {
            return new NettyWriteResponseFilter(properties.getStreamingMediaTypes());
        }

        @Bean
        public ReactorNettyWebSocketClient reactorNettyWebSocketClient(HttpClient httpClient) {
            return new MyReactorNettyWebSocketClient(httpClient);
        }
    }
}
