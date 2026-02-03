package com.surveillance.protocol.gb28181.handler;

import com.surveillance.dao.entity.StreamSession;
import com.surveillance.media.zlmediakit.StreamManager;
import com.surveillance.protocol.gb28181.config.GB28181Properties;
import com.surveillance.protocol.gb28181.session.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;

/**
 * INVITE Handler
 * Handles real-time video streaming requests
 */
@Slf4j
@Component
public class InviteHandler {

    private final GB28181Properties gb28181Properties;
    private final SessionManager sessionManager;
    private final StreamManager streamManager;
    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;
    private final SipProvider sipProvider;

    public InviteHandler(GB28181Properties gb28181Properties,
                         SessionManager sessionManager,
                         StreamManager streamManager,
                         AddressFactory addressFactory,
                         HeaderFactory headerFactory,
                         MessageFactory messageFactory,
                         SipProvider sipProvider) {
        this.gb28181Properties = gb28181Properties;
        this.sessionManager = sessionManager;
        this.streamManager = streamManager;
        this.addressFactory = addressFactory;
        this.headerFactory = headerFactory;
        this.messageFactory = messageFactory;
        this.sipProvider = sipProvider;
    }

    public void handle(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
            ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
            CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);

            String deviceId = fromHeader.getAddress().getURI().toString()
                    .replace("sip:", "").split("@")[0];
            String channelId = toHeader.getAddress().getURI().toString()
                    .replace("sip:", "").split("@")[0];

            log.info("INVITE request from device: {}, channel: {}", deviceId, channelId);

            // Parse SDP from request
            byte[] content = request.getRawContent();
            if (content == null) {
                log.error("No SDP in INVITE request");
                sendResponse(requestEvent, Response.BAD_REQUEST);
                return;
            }

            // Generate SSRC
            String ssrc = generateSSRC();

            // Create RTP session
            StreamSession streamSession = streamManager.createRtpSession(deviceId, channelId, ssrc, 0);
            if (streamSession == null) {
                log.error("Failed to create RTP session");
                sendResponse(requestEvent, Response.SERVER_INTERNAL_ERROR);
                return;
            }

            // Store session info
            streamSession.setCallId(callIdHeader.getCallId());
            streamSession.setFromTag(fromHeader.getTag());

            // Send 200 OK with SDP
            sendOkWithSdp(requestEvent, streamSession);

            log.info("INVITE handled successfully, session: {}", streamSession.getSessionId());

        } catch (Exception e) {
            log.error("Error handling INVITE request", e);
            sendResponse(requestEvent, Response.SERVER_INTERNAL_ERROR);
        }
    }

    private void sendOkWithSdp(RequestEvent requestEvent, StreamSession streamSession)
            throws ParseException, SipException, InvalidArgumentException {

        ServerTransaction serverTransaction = requestEvent.getServerTransaction();
        if (serverTransaction == null) {
            serverTransaction = sipProvider.getNewServerTransaction(requestEvent.getRequest());
        }

        Response response = messageFactory.createResponse(Response.OK, requestEvent.getRequest());

        // Add Contact header
        ToHeader toHeader = (ToHeader) requestEvent.getRequest().getHeader(ToHeader.NAME);
        Address contactAddress = addressFactory.createAddress(toHeader.getAddress().getURI());
        ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
        response.addHeader(contactHeader);

        // Build SDP
        String sdp = buildSdp(streamSession);
        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
        response.setContent(sdp, contentTypeHeader);

        serverTransaction.sendResponse(response);
    }

    private String buildSdp(StreamSession streamSession) {
        StringBuilder sdp = new StringBuilder();

        sdp.append("v=0\r\n");
        sdp.append("o=").append(gb28181Properties.getSip().getId())
                .append(" 0 0 IN IP4 ").append(gb28181Properties.getMedia().getIp()).append("\r\n");
        sdp.append("s=Play\r\n");
        sdp.append("c=IN IP4 ").append(gb28181Properties.getMedia().getIp()).append("\r\n");
        sdp.append("t=0 0\r\n");
        sdp.append("m=video ").append(streamSession.getRtpPort()).append(" RTP/AVP 96 98 97\r\n");
        sdp.append("a=rtpmap:96 PS/90000\r\n");
        sdp.append("a=rtpmap:98 H264/90000\r\n");
        sdp.append("a=rtpmap:97 MPEG4/90000\r\n");
        sdp.append("a=recvonly\r\n");
        sdp.append("a=setup:passive\r\n");
        sdp.append("a=connection:new\r\n");
        sdp.append("y=").append(streamSession.getSsrc()).append("\r\n");

        return sdp.toString();
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

    private String generateSSRC() {
        // Generate 10-digit SSRC
        return String.format("%010d", (int) (Math.random() * 10000000000L));
    }
}
