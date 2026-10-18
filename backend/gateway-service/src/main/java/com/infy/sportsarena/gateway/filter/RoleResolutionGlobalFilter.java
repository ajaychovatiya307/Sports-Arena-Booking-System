package com.infy.sportsarena.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Security hardening for the header-based role model (SRS US01).
 *
 * The React client sends {@code X-User-Id} and {@code X-User-Role} on every
 * request. A malicious user could tamper with the role stored in localStorage
 * and attempt to perform admin operations. To prevent this, the gateway NEVER
 * trusts the client-supplied {@code X-User-Role}. Instead, for any request that
 * carries an {@code X-User-Id}, it resolves the authoritative role from
 * auth-service and rewrites the {@code X-User-Role} header before forwarding.
 *
 * Requests without an {@code X-User-Id} (e.g. /api/auth/login, /api/auth/register)
 * have any stray {@code X-User-Role} stripped and are passed through unchanged.
 */
@Component
public class RoleResolutionGlobalFilter implements GlobalFilter, Ordered {

    private static final String HDR_USER_ID = "X-User-Id";
    private static final String HDR_USER_ROLE = "X-User-Role";

    private final WebClient authClient;

    public RoleResolutionGlobalFilter(WebClient.Builder loadBalancedWebClientBuilder) {
        this.authClient = loadBalancedWebClientBuilder.baseUrl("http://auth-service").build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String userId = request.getHeaders().getFirst(HDR_USER_ID);

        // No identity → strip any client-provided role and continue (public endpoints).
        if (userId == null || userId.isBlank()) {
            ServerHttpRequest stripped = request.mutate()
                    .headers(h -> h.remove(HDR_USER_ROLE))
                    .build();
            return chain.filter(exchange.mutate().request(stripped).build());
        }

        // Resolve the real role from auth-service and overwrite the header.
        return authClient.get()
                .uri(uri -> uri.path("/api/auth/resolve").queryParam("userId", userId).build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> {
                    Object role = body == null ? null : body.get("role");
                    return role == null ? "" : role.toString();
                })
                .onErrorReturn("")
                .defaultIfEmpty("")
                .flatMap(resolvedRole -> {
                    ServerHttpRequest mutated = request.mutate()
                            .headers(h -> {
                                h.remove(HDR_USER_ROLE);
                                if (!resolvedRole.isBlank()) {
                                    h.set(HDR_USER_ROLE, resolvedRole);
                                }
                            })
                            .build();
                    return chain.filter(exchange.mutate().request(mutated).build());
                });
    }

    @Override
    public int getOrder() {
        // Run early, before routing to the downstream service.
        return -1;
    }
}
