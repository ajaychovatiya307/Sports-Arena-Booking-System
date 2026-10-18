package com.infy.sportsarena.gateway.filter;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RoleResolutionGlobalFilterTest {

    @Test
    void getOrder_runsBeforeRouting() {
        WebClient.Builder builder = mock(WebClient.Builder.class, RETURNS_DEEP_STUBS);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        RoleResolutionGlobalFilter filter = new RoleResolutionGlobalFilter(builder);

        assertThat(filter.getOrder()).isEqualTo(-1);
    }

    @Test
    void filter_stripsRoleHeaderAndPassesThroughWhenNoUserId() {
        WebClient.Builder builder = mock(WebClient.Builder.class, RETURNS_DEEP_STUBS);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        RoleResolutionGlobalFilter filter = new RoleResolutionGlobalFilter(builder);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login")
                .header("X-User-Role", "ADMIN")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(org.mockito.ArgumentMatchers.any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertThat(captor.getValue().getRequest().getHeaders().getFirst("X-User-Role")).isNull();
    }

    @Test
    void filter_stripsRoleHeaderWhenUserIdIsBlank() {
        WebClient.Builder builder = mock(WebClient.Builder.class, RETURNS_DEEP_STUBS);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        RoleResolutionGlobalFilter filter = new RoleResolutionGlobalFilter(builder);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/auth/login")
                .header("X-User-Id", "  ").header("X-User-Role", "ADMIN").build());

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertThat(captor.getValue().getRequest().getHeaders().getFirst("X-User-Role")).isNull();
        verifyNoInteractions(builder.build());
    }

    @Test
    void filter_replacesSpoofedRoleWithResolvedRole() {
        WebClient.Builder builder = mock(WebClient.Builder.class, RETURNS_DEEP_STUBS);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        WebClient client = builder.build();
        when(client.get().uri(any(Function.class)).retrieve().bodyToMono(Map.class))
                .thenReturn(Mono.just(Map.of("role", "CUSTOMER")));
        RoleResolutionGlobalFilter filter = new RoleResolutionGlobalFilter(builder);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/teams")
                .header("X-User-Id", "U1").header("X-User-Role", "ADMIN").build());

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertThat(captor.getValue().getRequest().getHeaders().getFirst("X-User-Role")).isEqualTo("CUSTOMER");

        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<Function<UriBuilder, URI>> uriCaptor = ArgumentCaptor.forClass((Class) Function.class);
        verify(client.get(), times(2)).uri(uriCaptor.capture());
        URI resolvedUri = uriCaptor.getAllValues().get(1).apply(new DefaultUriBuilderFactory().builder());
        assertThat(resolvedUri.getPath()).isEqualTo("/api/auth/resolve");
        assertThat(resolvedUri.getQuery()).isEqualTo("userId=U1");
    }

    @Test
    void filter_removesRoleWhenResolvedRoleIsMissingOrEmpty() {
        assertRoleRemoved(Map.of());
        assertRoleRemoved(null);
    }

    @Test
    void filter_removesRoleWhenRoleResolutionFailsOrReturnsEmpty() {
        assertRoleRemoved(Mono.error(new IllegalStateException("auth unavailable")));
        assertRoleRemoved(Mono.empty());
    }

    private void assertRoleRemoved(Object result) {
        WebClient.Builder builder = mock(WebClient.Builder.class, RETURNS_DEEP_STUBS);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        WebClient client = builder.build();
        if (result instanceof Mono<?> mono) {
            when(client.get().uri(any(Function.class)).retrieve().bodyToMono(Map.class)).thenReturn((Mono) mono);
        } else {
            when(client.get().uri(any(Function.class)).retrieve().bodyToMono(Map.class))
                    .thenReturn(Mono.justOrEmpty((Map) result));
        }
        RoleResolutionGlobalFilter filter = new RoleResolutionGlobalFilter(builder);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/teams")
                .header("X-User-Id", "U1").header("X-User-Role", "ADMIN").build());

        filter.filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        assertThat(captor.getValue().getRequest().getHeaders().getFirst("X-User-Role")).isNull();
    }
}
