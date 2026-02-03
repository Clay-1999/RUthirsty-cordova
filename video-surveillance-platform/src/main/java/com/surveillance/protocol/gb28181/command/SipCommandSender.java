package com.surveillance.protocol.gb28181.command;

import com.surveillance.protocol.gb28181.config.GB28181Properties;
import com.surveillance.protocol.gb28181.session.DeviceSession;
import com.surveillance.protocol.gb28181.session.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * SIP Command Sender
 * Sends SIP commands to devices
 */
@Slf4j
@Component
public class SipCommandSender {

    private final GB28181Properties gb28181Properties;
    private final SessionManager sessionManager;
    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;
    private final SipProvider sipProvider;

    public SipCommandSender(GB28181Properties gb28181Properties,
                            SessionManager sessionManager,
                            AddressFactory addressFactory,
                            HeaderFactory headerFactory,
                            MessageFactory messageFactory,
                            SipProvider sipProvider) {
        this.gb28181Properties = gb28181Properties;
        this.sessionManager = sessionManager;
        this.addressFactory = addressFactory;
        this.headerFactory = headerFactory;
        this.messageFactory = messageFactory;
        this.sipProvider = sipProvider;
    }

    /**
     * Send catalog query to device
     */
    public boolean sendCatalogQuery(String deviceId) {
        try {
            DeviceSession session = sessionManager.getSession(deviceId);
            if (session == null) {
                log.error("Device session not found: {}", deviceId);
                return false;
            }

            String sn = String.valueOf(System.currentTimeMillis());
            String xml = buildCatalogQueryXml(sn);

            return sendMessage(deviceId, xml);

        } catch (Exception e) {
            log.error("Failed to send catalog query", e);
            return false;
        }
    }

    /**
     * Send device info query
     */
    public boolean sendDeviceInfoQuery(String deviceId) {
        try {
            String sn = String.valueOf(System.currentTimeMillis());
            String xml = buildDeviceInfoQueryXml(sn);

            return sendMessage(deviceId, xml);

        } catch (Exception e) {
            log.error("Failed to send device info query", e);
            return false;
        }
    }

    /**
     * Send PTZ control command
     */
    public boolean sendPtzControl(String deviceId, String channelId, int command, int speed) {
        try {
            String xml = buildPtzControlXml(channelId, command, speed);

            return sendMessage(deviceId, xml);

        } catch (Exception e) {
            log.error("Failed to send PTZ control", e);
            return false;
        }
    }

    /**
     * Send INVITE for real-time streaming
     */
    public boolean sendInvite(String deviceId, String channelId, String ssrc, int rtpPort) {
        try {
            DeviceSession session = sessionManager.getSession(deviceId);
            if (session == null) {
                log.error("Device session not found: {}", deviceId);
                return false;
            }

            // Build SIP INVITE request
            Request request = buildInviteRequest(deviceId, channelId, session);

            // Build SDP
            String sdp = buildInviteSdp(ssrc, rtpPort);
            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
            request.setContent(sdp, contentTypeHeader);

            // Send request
            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);
            clientTransaction.sendRequest();

            log.info("Sent INVITE to device: {}, channel: {}", deviceId, channelId);
            return true;

        } catch (Exception e) {
            log.error("Failed to send INVITE", e);
            return false;
        }
    }

    /**
     * Send MESSAGE request
     */
    private boolean sendMessage(String deviceId, String xmlContent) {
        try {
            DeviceSession session = sessionManager.getSession(deviceId);
            if (session == null) {
                log.error("Device session not found: {}", deviceId);
                return false;
            }

            Request request = buildMessageRequest(deviceId, session);

            // Set content
            String charset = gb28181Properties.getSip().getCharset();
            byte[] content = xmlContent.getBytes(Charset.forName(charset));
            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("Application", "MANSCDP+xml");
            request.setContent(content, contentTypeHeader);

            // Send request
            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);
            clientTransaction.sendRequest();

            log.debug("Sent MESSAGE to device: {}", deviceId);
            return true;

        } catch (Exception e) {
            log.error("Failed to send MESSAGE", e);
            return false;
        }
    }

    private Request buildMessageRequest(String deviceId, DeviceSession session) throws ParseException, InvalidArgumentException {
        String platformId = gb28181Properties.getSip().getId();
        String platformDomain = gb28181Properties.getSip().getDomain();
        String transport = gb28181Properties.getSip().getTransport();

        // From header
        SipURI fromUri = addressFactory.createSipURI(platformId, platformDomain);
        Address fromAddress = addressFactory.createAddress(fromUri);
        FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf(System.currentTimeMillis()));

        // To header
        SipURI toUri = addressFactory.createSipURI(deviceId, session.getIp() + ":" + session.getPort());
        Address toAddress = addressFactory.createAddress(toUri);
        ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

        // Via header
        ViaHeader viaHeader = headerFactory.createViaHeader(
                gb28181Properties.getSip().getIp(),
                gb28181Properties.getSip().getPort(),
                transport,
                null
        );

        // CallId header
        CallIdHeader callIdHeader = sipProvider.getNewCallId();

        // CSeq header
        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.MESSAGE);

        // MaxForwards header
        MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

        // Request URI
        SipURI requestUri = addressFactory.createSipURI(deviceId, session.getIp() + ":" + session.getPort());

        // Create request
        Request request = messageFactory.createRequest(
                requestUri,
                Request.MESSAGE,
                callIdHeader,
                cSeqHeader,
                fromHeader,
                toHeader,
                new ArrayList<ViaHeader>() {{ add(viaHeader); }},
                maxForwardsHeader
        );

        return request;
    }

    private Request buildInviteRequest(String deviceId, String channelId, DeviceSession session)
            throws ParseException, InvalidArgumentException {

        String platformId = gb28181Properties.getSip().getId();
        String platformDomain = gb28181Properties.getSip().getDomain();
        String transport = gb28181Properties.getSip().getTransport();

        // From header
        SipURI fromUri = addressFactory.createSipURI(platformId, platformDomain);
        Address fromAddress = addressFactory.createAddress(fromUri);
        FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, String.valueOf(System.currentTimeMillis()));

        // To header
        SipURI toUri = addressFactory.createSipURI(channelId, session.getIp() + ":" + session.getPort());
        Address toAddress = addressFactory.createAddress(toUri);
        ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

        // Via header
        ViaHeader viaHeader = headerFactory.createViaHeader(
                gb28181Properties.getSip().getIp(),
                gb28181Properties.getSip().getPort(),
                transport,
                null
        );

        // CallId header
        CallIdHeader callIdHeader = sipProvider.getNewCallId();

        // CSeq header
        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.INVITE);

        // MaxForwards header
        MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

        // Contact header
        SipURI contactUri = addressFactory.createSipURI(platformId, gb28181Properties.getSip().getIp() + ":" + gb28181Properties.getSip().getPort());
        Address contactAddress = addressFactory.createAddress(contactUri);
        ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);

        // Subject header
        SubjectHeader subjectHeader = headerFactory.createSubjectHeader(channelId + ":0,0:0");

        // Request URI
        SipURI requestUri = addressFactory.createSipURI(channelId, session.getIp() + ":" + session.getPort());

        // Create request
        Request request = messageFactory.createRequest(
                requestUri,
                Request.INVITE,
                callIdHeader,
                cSeqHeader,
                fromHeader,
                toHeader,
                new ArrayList<ViaHeader>() {{ add(viaHeader); }},
                maxForwardsHeader
        );

        request.addHeader(contactHeader);
        request.addHeader(subjectHeader);

        return request;
    }

    private String buildCatalogQueryXml(String sn) {
        return "<?xml version=\"1.0\" encoding=\"GB2312\"?>\r\n" +
                "<Query>\r\n" +
                "<CmdType>Catalog</CmdType>\r\n" +
                "<SN>" + sn + "</SN>\r\n" +
                "<DeviceID>" + gb28181Properties.getSip().getId() + "</DeviceID>\r\n" +
                "</Query>\r\n";
    }

    private String buildDeviceInfoQueryXml(String sn) {
        return "<?xml version=\"1.0\" encoding=\"GB2312\"?>\r\n" +
                "<Query>\r\n" +
                "<CmdType>DeviceInfo</CmdType>\r\n" +
                "<SN>" + sn + "</SN>\r\n" +
                "<DeviceID>" + gb28181Properties.getSip().getId() + "</DeviceID>\r\n" +
                "</Query>\r\n";
    }

    private String buildPtzControlXml(String channelId, int command, int speed) {
        String cmdCode = buildPtzCmdCode(command, speed);

        return "<?xml version=\"1.0\" encoding=\"GB2312\"?>\r\n" +
                "<Control>\r\n" +
                "<CmdType>DeviceControl</CmdType>\r\n" +
                "<SN>" + System.currentTimeMillis() + "</SN>\r\n" +
                "<DeviceID>" + channelId + "</DeviceID>\r\n" +
                "<PTZCmd>" + cmdCode + "</PTZCmd>\r\n" +
                "<Info>\r\n" +
                "</Info>\r\n" +
                "</Control>\r\n";
    }

    private String buildPtzCmdCode(int command, int speed) {
        // PTZ command encoding according to GB28181
        // A50F [direction] [speed] [zoom] [focus] [iris] checksum
        int byte1 = 0xA5;
        int byte2 = 0x0F;
        int byte3 = 0;
        int byte4 = speed & 0xFF;
        int byte5 = 0;
        int byte6 = 0;
        int byte7 = 0;

        // Direction commands
        switch (command) {
            case 1: // UP
                byte3 = 0x08;
                break;
            case 2: // DOWN
                byte3 = 0x04;
                break;
            case 3: // LEFT
                byte3 = 0x02;
                break;
            case 4: // RIGHT
                byte3 = 0x01;
                break;
            case 5: // ZOOM_IN
                byte5 = 0x10;
                break;
            case 6: // ZOOM_OUT
                byte5 = 0x20;
                break;
            case 0: // STOP
                byte3 = 0;
                byte4 = 0;
                byte5 = 0;
                break;
        }

        int checksum = (byte1 + byte2 + byte3 + byte4 + byte5 + byte6 + byte7) % 256;

        return String.format("%02X%02X%02X%02X%02X%02X%02X%02X",
                byte1, byte2, byte3, byte4, byte5, byte6, byte7, checksum);
    }

    private String buildInviteSdp(String ssrc, int rtpPort) {
        StringBuilder sdp = new StringBuilder();

        sdp.append("v=0\r\n");
        sdp.append("o=").append(gb28181Properties.getSip().getId())
                .append(" 0 0 IN IP4 ").append(gb28181Properties.getMedia().getIp()).append("\r\n");
        sdp.append("s=Play\r\n");
        sdp.append("c=IN IP4 ").append(gb28181Properties.getMedia().getIp()).append("\r\n");
        sdp.append("t=0 0\r\n");
        sdp.append("m=video ").append(rtpPort).append(" RTP/AVP 96 98 97\r\n");
        sdp.append("a=rtpmap:96 PS/90000\r\n");
        sdp.append("a=rtpmap:98 H264/90000\r\n");
        sdp.append("a=rtpmap:97 MPEG4/90000\r\n");
        sdp.append("a=recvonly\r\n");
        sdp.append("a=setup:passive\r\n");
        sdp.append("a=connection:new\r\n");
        sdp.append("y=").append(ssrc).append("\r\n");

        return sdp.toString();
    }
}
