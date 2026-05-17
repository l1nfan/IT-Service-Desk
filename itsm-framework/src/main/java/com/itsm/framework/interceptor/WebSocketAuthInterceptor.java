package com.itsm.framework.interceptor;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;
import com.itsm.common.core.domain.model.LoginUser;
import com.itsm.framework.web.service.TokenService;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor
{
    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final TokenService tokenService;

    public WebSocketAuthInterceptor(TokenService tokenService)
    {
        this.tokenService = tokenService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes)
    {
        try
        {
            String query = request.getURI().getQuery();
            if (query == null)
            {
                log.warn("WebSocket handshake rejected: no query parameters");
                return false;
            }
            String token = UriComponentsBuilder.fromUri(request.getURI()).build()
                    .getQueryParams().getFirst("token");
            if (token == null || token.isEmpty())
            {
                log.warn("WebSocket handshake rejected: no token");
                return false;
            }
            LoginUser loginUser = tokenService.getLoginUserByToken(token);
            if (loginUser == null)
            {
                log.warn("WebSocket handshake rejected: invalid token");
                return false;
            }
            attributes.put("loginUser", loginUser);
            attributes.put("token", token);
            return true;
        }
        catch (Exception e)
        {
            log.warn("WebSocket handshake error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception)
    {
    }
}
