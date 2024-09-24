package com.SAE.Serveur.Model;

import org.json.JSONObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

public class WebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> session = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        String query = session.getUri().getQuery();
        String[] params = query.split("=");
        String idPlayer = params[1];

        if (idPlayer != null) {
            System.out.println("Connection : " + session.toString() + " Joueur " + idPlayer);
            this.session.put(idPlayer, session);
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        this.session.values().remove(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);
        for (WebSocketSession webSocketSession: this.session.values()) {
            if (webSocketSession.equals(session)) {
                webSocketSession.sendMessage(message);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JSONObject jsonMessage = new JSONObject(message.getPayload());
        String destinataire = jsonMessage.getString("destinataire");
        String content = jsonMessage.getString("message");

        sendMessageToUser(destinataire, content);
    }

    public void sendMessageToUser(String idPlayer, String message) throws Exception {
        WebSocketSession session = this.session.get(idPlayer);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage("Message de l'utilisateur : " + message));
        }
    }
}
