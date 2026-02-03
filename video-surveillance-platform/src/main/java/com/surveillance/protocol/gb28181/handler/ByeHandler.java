package com.surveillance.protocol.gb28181.handler;

import com.surveillance.media.zlmediakit.StreamManager;
import com.surveillance.protocol.gb28181.session.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.*;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * BYE Handler
 * Handles stream termination requests
 */
@Slf4j
@Component
public class ByeHandler {

    private final SessionManager sessionManager;
    private final StreamManager streamManager;
    private final MessageFactory messageFactory;
    private final SipProvider sipProvider;

    public ByeHandler(SessionManager sessionManager,
                      StreamManager streamManager,
                      MessageFactory messageFactory,
                      SipProvider sipProvider) {
        this.sessionManager = sessionManager;
        this.streamManager = streamManager;
        this.messageFactory = messageFactory;
        this.sipProvider = sipProvider;
    }

    public void handle(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
            CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);

            String deviceId = fromHeader.getAddress().getURI().toString()
                    .replace("sip:", "").split("@")[0];

            log.info("BYE request from device: {}, callId: {}", deviceId, callIdHeader.getCallId());

            // Close stream session
            // Note: In a real implementation, you would need to find the session by callId
            // and close it properly

            sendResponse(requestEvent, Response.OK);

            log.info("BYE handled successfully for device: {}", deviceId);

        } catch (Exception e) {
            log.error("Error handling BYE request", e);
            sendResponse(requestEvent, Response.SERVER_INTERNAL_ERROR);
        }
    }

    private void sendResponse(RequestEvent requestEvent, int statusCode) {
        try {
            ServerTransaction serverTransaction = requestEvent.getServerTransaction();
            if (serverTransaction == null) {
                serverTransaction = sipProvider.getNewServerTransaction(requestEvent.getRequest());
            }

            Response response = messageFactory.createResponse(statusCode, requestEvent.getRequest());
            serverTransaction.sendResponse(response);
        } catch (Exception e) {
            log.error("Failed to send response", e);
        }
    }
}
