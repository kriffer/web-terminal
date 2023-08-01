package io.kriffer.webterminal.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import io.kriffer.webterminal.model.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private ObjectWriter objectWriter;

    public WebSocketHandler(ObjectMapper objectMapper) {
        this.objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable throwable) {
        log.error("error occured at sender " + session, throwable);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info(String.format("Session %s closed because of %s", session.getId(), status.getReason()));
        sessions.remove(session.getId());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Connected ... " + session.getId());
        sessions.put(session.getId(), session);
    }


    @Override
    protected void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) throws Exception {
        var clientMessage = message.getPayload();
        ObjectMapper mapper = new ObjectMapper();
        Request com = mapper.readValue(clientMessage, Request.class);
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(com.getUsername(), com.getHost(), com.getPort());

            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(com.getPassword());
            session.connect();

            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setOutputStream(System.out, true);
            OutputStream outputStreamStdErr = new ByteArrayOutputStream();
            channelExec.setErrStream(outputStreamStdErr, true);

            InputStream in = channelExec.getInputStream();
            channelExec.setCommand(com.getCommand());

            channelExec.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            if (!com.getSessionUser().isEmpty()) {
                TextMessage textMessage1 = new TextMessage("-> " + com.getSessionUser() + "$ " + com.getCommand());
                webSocketSession.sendMessage(textMessage1);
            }

                try (ByteArrayInputStream inErr = new ByteArrayInputStream(channelExec.getErrStream().readAllBytes())) {
                    String inErrContent = new String(inErr.readAllBytes());
                    if(!inErrContent.isEmpty()){
                        TextMessage textMessage2 = new TextMessage( inErrContent);
                        webSocketSession.sendMessage(textMessage2);
                    }
                }

            while ((line = reader.readLine()) != null) {
                TextMessage textMessage = new TextMessage(line);
                webSocketSession.sendMessage(textMessage);
            }

            channelExec.disconnect();
            session.disconnect();


        } catch (Exception e) {
            log.error("Error happened: " + e);
            e.printStackTrace();
        }
    }

}
