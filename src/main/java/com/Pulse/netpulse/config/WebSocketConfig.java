package com.Pulse.netpulse.config;

import com.Pulse.netpulse.controller.websocket.SshWebSocketHandler;
import com.Pulse.netpulse.service.SshService;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SshService sshService;

    // Spring inietta automaticamente SshService qui dentro
    public WebSocketConfig(SshService sshService) {
        this.sshService = sshService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SshWebSocketHandler(sshService), "/ssh")
                .setAllowedOrigins("*"); // In produzione inserisci i domini specifici
    }
}