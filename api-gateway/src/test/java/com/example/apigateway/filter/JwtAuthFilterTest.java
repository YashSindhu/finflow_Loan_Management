package com.example.apigateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthFilterTest {

    private JwtAuthFilter filter;
    private static final String SECRET = "mysupersecretkeymysupersecretkey12345678";

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter();
        ReflectionTestUtils.setField(filter, "secret", SECRET);
    }

    @Test
    void filter_isNotNull() {
        assertNotNull(filter);
    }

    @Test
    void config_isNotNull() {
        JwtAuthFilter.Config config = new JwtAuthFilter.Config();
        assertNotNull(config);
    }

    @Test
    void generateValidToken_canBeParsed() {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setSubject("user@test.com")
                .claim("role", "ROLE_USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertNotNull(token);
        assertFalse(token.isBlank());

        // Verify token can be parsed
        var claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("user@test.com", claims.getSubject());
        assertEquals("ROLE_USER", claims.get("role"));
    }

    @Test
    void expiredToken_throwsException() {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .setSubject("user@test.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(Exception.class, () ->
                Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
        );
    }

    @Test
    void apply_missingAuthorizationHeader_setsUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/applications").build()
        );

        filter.apply(new JwtAuthFilter.Config())
                .filter(exchange, unusedChain())
                .block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void apply_invalidAuthorizationHeader_setsUnauthorized() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/applications")
                        .header("Authorization", "Basic invalid")
                        .build()
        );

        filter.apply(new JwtAuthFilter.Config())
                .filter(exchange, unusedChain())
                .block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void apply_validToken_addsUserHeadersAndCallsChain() {
        String token = createToken("user@test.com", "ROLE_USER", System.currentTimeMillis() + 3600000);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/applications")
                        .header("Authorization", "Bearer " + token)
                        .build()
        );
        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        GatewayFilterChain chain = mutatedExchange -> {
            capturedExchange.set(mutatedExchange);
            return Mono.empty();
        };

        filter.apply(new JwtAuthFilter.Config())
                .filter(exchange, chain)
                .block();

        assertNotNull(capturedExchange.get());
        assertEquals("user@test.com", capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Email"));
        assertEquals("ROLE_USER", capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Role"));
    }

    @Test
    void apply_expiredToken_setsUnauthorizedWithExpiredHeader() {
        String token = createToken("user@test.com", "ROLE_USER", System.currentTimeMillis() - 5000);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/applications")
                        .header("Authorization", "Bearer " + token)
                        .build()
        );

        filter.apply(new JwtAuthFilter.Config())
                .filter(exchange, unusedChain())
                .block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals("Token expired", exchange.getResponse().getHeaders().getFirst("X-Auth-Error"));
    }

    @Test
    void apply_invalidToken_setsUnauthorizedWithInvalidHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/applications")
                        .header("Authorization", "Bearer invalid-token")
                        .build()
        );

        filter.apply(new JwtAuthFilter.Config())
                .filter(exchange, unusedChain())
                .block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals("Invalid token", exchange.getResponse().getHeaders().getFirst("X-Auth-Error"));
    }

    @Test
    void apply_unexpectedAuthError_setsUnauthorizedWithAuthErrorHeader() {
        String token = createToken("user@test.com", "ROLE_USER", System.currentTimeMillis() + 3600000);
        ReflectionTestUtils.setField(filter, "secret", null);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/applications")
                        .header("Authorization", "Bearer " + token)
                        .build()
        );

        filter.apply(new JwtAuthFilter.Config())
                .filter(exchange, unusedChain())
                .block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals("Auth error", exchange.getResponse().getHeaders().getFirst("X-Auth-Error"));
    }

    private String createToken(String subject, String role, long expirationTime) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(expirationTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private GatewayFilterChain unusedChain() {
        return exchange -> {
            fail("Gateway chain should not be called");
            return Mono.empty();
        };
    }
}
