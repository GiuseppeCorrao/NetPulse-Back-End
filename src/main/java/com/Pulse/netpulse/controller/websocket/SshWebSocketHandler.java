package com.Pulse.netpulse.controller.websocket;

import com.Pulse.netpulse.utility.SshSessionWrapper;
import com.Pulse.netpulse.service.SshService;
import com.Pulse.netpulse.utility.parser.TerminalParser;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SshWebSocketHandler extends TextWebSocketHandler {



    private final SshService sshService;
    private SshSessionWrapper sshSessionWrapper;

    private String host = "";
    private String user = "";
    private String password = "";
    private final TerminalParser parser = new TerminalParser();

    // Possibly state of the conversation
    private enum ConnectionState {
        WAITING_HOST, WAITING_USER, WAITING_PASSWORD, CONNECTED
    }

    private ConnectionState actualState = ConnectionState.WAITING_HOST;

    // Constructor for receiving the SSH Service managed from SPRING
    public SshWebSocketHandler(SshService sshService) {
        this.sshService = sshService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        webSocketSession.sendMessage(new TextMessage("=== Welcome to NetPulse ===\r\n"));
        webSocketSession.sendMessage(new TextMessage("Insert the (SSH) server IP address:"));

        // Ensure the initial state is correct
        this.actualState = ConnectionState.WAITING_HOST;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // The payload is the input from the user
        String inputText = message.getPayload().trim();

        // If the connection is established, send the text directly to Linux shell
        if (actualState == ConnectionState.CONNECTED) {
            if (sshSessionWrapper != null && sshSessionWrapper.getChannel().isConnected()) {
                String command = message.getPayload();
                if (!command.endsWith("\n")) {
                    command += "\n";
                }
                sshSessionWrapper.getOutputStream().write(command.getBytes(StandardCharsets.UTF_8));
                sshSessionWrapper.getOutputStream().flush();
            }
            return;
        }

        // If the connection is not established, collect credential
        switch (actualState) {
            case WAITING_HOST:
                this.host = inputText;
                session.sendMessage(new TextMessage("Host saved: " + host + "\r\nInsert SSH user: "));
                this.actualState = ConnectionState.WAITING_USER; // Changing State
                break;

            case WAITING_USER:
                this.user = inputText;
                session.sendMessage(new TextMessage("User saved: " + user + "\r\nInsert the SSH password: "));
                this.actualState = ConnectionState.WAITING_PASSWORD; // Changing State
                break;

            case WAITING_PASSWORD:
                this.password = inputText;
                session.sendMessage(new TextMessage("Esatblishing connection to " + host + "...\r\n"));


                // After collecting credential, start the service
                try {
                    // 1. Open SSH session
                    this.sshSessionWrapper = sshService.openSshSession(host, user, password);
                    InputStream sshOutput = sshSessionWrapper.getInputStream();

                    session.sendMessage(new TextMessage("🟢 CONNECTED TO SSH SHELL!\r\n"));
                    this.actualState = ConnectionState.CONNECTED; // Set the final State

                    // 2. Start the tread that read the Linux output, then send to the WebSocket session
                    new Thread(() -> {
                        byte[] buffer = new byte[1024];
                        int i;
                        try {
                            while ((i = sshOutput.read(buffer)) != -1) {
                                if (session.isOpen()) {
                                    String rawOutput = new String(buffer, 0, i, StandardCharsets.UTF_8);
                                    String jsonOutput = parser.parseToStructuredJson(rawOutput);

                                    if (jsonOutput != null) {
                                        System.out.println(jsonOutput);
                                        session.sendMessage(new TextMessage(jsonOutput));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Interruption of SSH write." + e);
                            try {
                                session.sendMessage(new TextMessage("Interruption of SSH write."));
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }).start();

                } catch (Exception e) {
                    // If the password is wrong, or the host is not reachable, reset the quiz information
                    session.sendMessage(new TextMessage("❌ ERROR SSH: " + e.getMessage() + "\r\n"));
                    session.sendMessage(new TextMessage("Restart. Insert the (SSH) server IP address: "));
                    this.actualState = ConnectionState.WAITING_HOST;
                }
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if (sshSessionWrapper != null) {
            sshSessionWrapper.closeAll();
        }
        System.out.println("Sessione WebSocket e SSH chiuse accuratamente.");
    }
}