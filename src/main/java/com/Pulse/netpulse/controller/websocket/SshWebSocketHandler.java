package com.Pulse.netpulse.controller.websocket;

import com.Pulse.netpulse.dto.utility.SshSessionWrapper;
import com.Pulse.netpulse.service.SshService;
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

    // Definiamo gli stati possibili della nostra conversazione
    private enum ConnectionState {
        WAITING_HOST, WAITING_USER, WAITING_PASSWORD, CONNECTED
    }
    // All'inizio, l'handler è in attesa dell'IP (Host)
    private ConnectionState actualState = ConnectionState.WAITING_HOST;

    // Costruttore per ricevere lo SshService gestito da Spring
    public SshWebSocketHandler(SshService sshService) {
        this.sshService = sshService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        webSocketSession.sendMessage(new TextMessage("=== Benvenuto in NetPulse ===\r\n"));
        webSocketSession.sendMessage(new TextMessage("Inserisci l'IP del server SSH: "));
        // Ci assicuriamo che lo stato iniziale sia corretto
        this.actualState = ConnectionState.WAITING_HOST;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Il payload è il testo che hai digitato su Postman
        String testoDigitato = message.getPayload().trim();

        // Se siamo già connessi, mandiamo il testo direttamente alla shell Linux
        if (actualState == ConnectionState.CONNECTED) {
            if (sshSessionWrapper != null && sshSessionWrapper.getChannel().isConnected()) {
                String command = message.getPayload();
                if (!command.endsWith("\n")) {
                    command += "\n";
                }
                sshSessionWrapper.getOutputStream().write(command.getBytes(StandardCharsets.UTF_8));
                sshSessionWrapper.getOutputStream().flush();
            }
            return; // Usciamo dal metodo, non serve fare altro
        }

        // Se non siamo connessi, stiamo raccogliendo le credenziali:
        switch (actualState) {
            case WAITING_HOST:
                this.host = testoDigitato;
                session.sendMessage(new TextMessage("Host impostato: " + host + "\r\nInserisci l'utente SSH: "));
                this.actualState = ConnectionState.WAITING_USER; // Cambiamo stato!
                break;

            case WAITING_USER:
                this.user = testoDigitato;
                session.sendMessage(new TextMessage("Utente impostato: " + user + "\r\nInserisci la password SSH: "));
                this.actualState = ConnectionState.WAITING_PASSWORD; // Cambiamo stato!
                break;

            case WAITING_PASSWORD:
                this.password = testoDigitato;
                session.sendMessage(new TextMessage("Connessione in corso a " + host + "...\r\n"));

                // Ora che abbiamo HOST, USER e PASSWORD reali, avviamo il servizio!
                try {
                    // 1. Apriamo la sessione SSH
                    this.sshSessionWrapper = sshService.openSshSession(host, user, password);
                    InputStream sshOutput = sshSessionWrapper.getInputStream();

                    session.sendMessage(new TextMessage("🟢 CONNESSO ALLA SHELL SSH!\r\n"));
                    this.actualState = ConnectionState.CONNECTED; // Impostiamo lo stato finale

                    // 2. Facciamo partire il thread che legge l'output Linux e lo spara sul WebSocket
                    new Thread(() -> {
                        byte[] buffer = new byte[1024];
                        int i;
                        try {
                            while ((i = sshOutput.read(buffer)) != -1) {
                                if (session.isOpen()) {
                                    String output = new String(buffer, 0, i, StandardCharsets.UTF_8);
                                    session.sendMessage(new TextMessage(output));
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Interruzione lettura SSH.");
                            try {
                                session.sendMessage(new TextMessage("Interruzione lettura SSH"));
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }).start();

                } catch (Exception e) {
                    // Se la password è sbagliata o l'host è irraggiungibile, resettiamo il quiz!
                    session.sendMessage(new TextMessage("❌ Errore SSH: " + e.getMessage() + "\r\n"));
                    session.sendMessage(new TextMessage("Ricominciamo. Inserisci l'IP del server SSH: "));
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