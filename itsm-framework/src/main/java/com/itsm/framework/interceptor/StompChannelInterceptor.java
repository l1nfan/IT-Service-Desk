package com.itsm.framework.interceptor;

import java.security.Principal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import com.itsm.common.core.domain.model.LoginUser;
import com.itsm.framework.web.service.TokenService;

@Component
public class StompChannelInterceptor implements ChannelInterceptor
{
    private static final Logger log = LoggerFactory.getLogger(StompChannelInterceptor.class);

    @Autowired
    private TokenService tokenService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel)
    {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand()))
        {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null)
            {
                LoginUser loginUser = (LoginUser) sessionAttributes.get("loginUser");
                if (loginUser != null)
                {
                    String userId = loginUser.getUserId().toString();
                    accessor.setUser(new StompPrincipal(userId));
                    log.info("STOMP CONNECT: userId={}", userId);
                }
                else
                {
                    log.warn("STOMP CONNECT rejected: no loginUser in session");
                    return null;
                }
            }
        }
        else if (accessor != null
                && (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())))
        {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null)
            {
                LoginUser loginUser = (LoginUser) sessionAttributes.get("loginUser");
                if (loginUser == null)
                {
                    log.warn("STOMP {} rejected: no loginUser in session", accessor.getCommand());
                    return null;
                }
                String token = (String) sessionAttributes.get("token");
                if (token == null || tokenService.getLoginUserByToken(token) == null)
                {
                    log.warn("STOMP {} rejected: token expired for userId={}", accessor.getCommand(), loginUser.getUserId());
                    StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
                    errorAccessor.setSessionId(accessor.getSessionId());
                    errorAccessor.setMessage("Token expired, please reconnect");
                    return MessageBuilder.createMessage(new byte[0], errorAccessor.getMessageHeaders());
                }
            }
        }
        return message;
    }

    private record StompPrincipal(String name) implements Principal
    {
        @Override
        public String getName()
        {
            return name;
        }
    }
}
