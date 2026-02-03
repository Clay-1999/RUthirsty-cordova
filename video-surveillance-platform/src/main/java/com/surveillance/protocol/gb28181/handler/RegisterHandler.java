package com.surveillance.protocol.gb28181.handler;

import cn.hutool.crypto.digest.DigestUtil;
import com.surveillance.dao.entity.Device;
import com.surveillance.dao.mapper.DeviceMapper;
import com.surveillance.protocol.gb28181.config.GB28181Properties;
import com.surveillance.protocol.gb28181.session.DeviceSession;
import com.surveillance.protocol.gb28181.session.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.*;
import javax.sip.address.AddressFactory;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.time.LocalDateTime;

/**
 * REGISTER Message Handler
 * Handles device registration
 */
@Slf4j
@Component
public class RegisterHandler {

    private final GB28181Properties gb28181Properties;
    private final SessionManager sessionManager;
    private final DeviceMapper deviceMapper;
    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;
    private final SipProvider sipProvider;

    public RegisterHandler(GB28181Properties gb28181Properties,
                           SessionManager sessionManager,
                           DeviceMapper deviceMapper,
                           AddressFactory addressFactory,
                           HeaderFactory headerFactory,
                           MessageFactory messageFactory,
                           SipProvider sipProvider) {
        this.gb28181Properties = gb28181Properties;
        this.sessionManager = sessionManager;
        this.deviceMapper = deviceMapper;
        this.addressFactory = addressFactory;
        this.headerFactory = headerFactory;
        this.messageFactory = messageFactory;
        this.sipProvider = sipProvider;
    }

    public void handle(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
            ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
            ExpiresHeader expiresHeader = (ExpiresHeader) request.getHeader(ExpiresHeader.NAME);
            AuthorizationHeader authorizationHeader = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);

            String deviceId = fromHeader.getAddress().getURI().toString()
                    .replace("sip:", "").split("@")[0];
            String deviceIp = viaHeader.getHost();
            int devicePort = viaHeader.getPort();
            String transport = viaHeader.getTransport();
            int expires = expiresHeader != null ? expiresHeader.getExpires() : 3600;

            log.info("Device registration request from: {} ({}:{})", deviceId, deviceIp, devicePort);

            // Check if authentication is required
            if (authorizationHeader == null) {
                // Send 401 Unauthorized with challenge
                sendUnauthorized(requestEvent, deviceId);
                return;
            }

            // Verify authentication
            if (!verifyAuthentication(authorizationHeader, request.getMethod(), deviceId)) {
                log.warn("Authentication failed for device: {}", deviceId);
                sendResponse(requestEvent, Response.FORBIDDEN);
                return;
            }

            // Registration successful
            DeviceSession session = sessionManager.createOrUpdateSession(deviceId);
            session.setIp(deviceIp);
            session.setPort(devicePort);
            session.setTransport(transport);
            session.setHostAddress(viaHeader.getHost());
            session.setRegisterTime(LocalDateTime.now());
            session.setLastKeepaliveTime(LocalDateTime.now());
            session.setExpires(expires);

            CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
            session.setCallId(callIdHeader.getCallId());
            session.setFromTag(fromHeader.getTag());

            // Update database
            updateDeviceInDatabase(session);

            // Send 200 OK
            sendOk(requestEvent, expires);

            log.info("Device registered successfully: {}", deviceId);

        } catch (Exception e) {
            log.error("Error handling REGISTER request", e);
            sendResponse(requestEvent, Response.SERVER_INTERNAL_ERROR);
        }
    }

    private void sendUnauthorized(RequestEvent requestEvent, String deviceId) throws ParseException, SipException, InvalidArgumentException {
        ServerTransaction serverTransaction = requestEvent.getServerTransaction();
        if (serverTransaction == null) {
            serverTransaction = sipProvider.getNewServerTransaction(requestEvent.getRequest());
        }

        Response response = messageFactory.createResponse(Response.UNAUTHORIZED, requestEvent.getRequest());

        // Add WWW-Authenticate header
        String realm = gb28181Properties.getSip().getDomain();
        String nonce = DigestUtil.md5Hex(System.currentTimeMillis() + deviceId);

        WWWAuthenticateHeader wwwAuthenticateHeader = headerFactory.createWWWAuthenticateHeader("Digest");
        wwwAuthenticateHeader.setRealm(realm);
        wwwAuthenticateHeader.setNonce(nonce);
        wwwAuthenticateHeader.setAlgorithm("MD5");

        response.addHeader(wwwAuthenticateHeader);

        serverTransaction.sendResponse(response);
        log.debug("Sent 401 Unauthorized to device: {}", deviceId);
    }

    private void sendOk(RequestEvent requestEvent, int expires) throws ParseException, SipException, InvalidArgumentException {
        ServerTransaction serverTransaction = requestEvent.getServerTransaction();
        if (serverTransaction == null) {
            serverTransaction = sipProvider.getNewServerTransaction(requestEvent.getRequest());
        }

        Response response = messageFactory.createResponse(Response.OK, requestEvent.getRequest());

        // Add Expires header
        ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(expires);
        response.addHeader(expiresHeader);

        // Add Date header
        DateHeader dateHeader = headerFactory.createDateHeader(java.util.Calendar.getInstance());
        response.addHeader(dateHeader);

        serverTransaction.sendResponse(response);
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

    private boolean verifyAuthentication(AuthorizationHeader authHeader, String method, String deviceId) {
        try {
            String username = authHeader.getUsername();
            String realm = authHeader.getRealm();
            String nonce = authHeader.getNonce();
            String uri = authHeader.getURI().toString();
            String response = authHeader.getResponse();

            // Get password from configuration or database
            String password = gb28181Properties.getSip().getPassword();

            // Calculate expected response
            String ha1 = DigestUtil.md5Hex(username + ":" + realm + ":" + password);
            String ha2 = DigestUtil.md5Hex(method + ":" + uri);
            String expectedResponse = DigestUtil.md5Hex(ha1 + ":" + nonce + ":" + ha2);

            return expectedResponse.equalsIgnoreCase(response);
        } catch (Exception e) {
            log.error("Error verifying authentication", e);
            return false;
        }
    }

    private void updateDeviceInDatabase(DeviceSession session) {
        try {
            Device device = deviceMapper.selectById(session.getDeviceId());
            if (device == null) {
                device = new Device();
                device.setDeviceId(session.getDeviceId());
                device.setDeviceName(session.getDeviceId());
                device.setDeviceType("GB28181");
                deviceMapper.insert(device);
            }

            device.setIpAddress(session.getIp());
            device.setPort(session.getPort());
            device.setTransport(session.getTransport());
            device.setHostAddress(session.getHostAddress());
            device.setRegisterTime(session.getRegisterTime());
            device.setLastKeepaliveTime(session.getLastKeepaliveTime());
            device.setExpires(session.getExpires());
            device.setStatus("ONLINE");

            deviceMapper.updateById(device);
        } catch (Exception e) {
            log.error("Error updating device in database", e);
        }
    }
}
