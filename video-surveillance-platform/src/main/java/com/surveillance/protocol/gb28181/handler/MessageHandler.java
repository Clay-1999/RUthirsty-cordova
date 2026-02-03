package com.surveillance.protocol.gb28181.handler;

import com.surveillance.dao.entity.DeviceChannel;
import com.surveillance.dao.mapper.DeviceChannelMapper;
import com.surveillance.protocol.gb28181.config.GB28181Properties;
import com.surveillance.protocol.gb28181.session.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.sip.*;
import javax.sip.address.AddressFactory;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

/**
 * MESSAGE Handler
 * Handles keepalive, catalog query, device info, etc.
 */
@Slf4j
@Component
public class MessageHandler {

    private final GB28181Properties gb28181Properties;
    private final SessionManager sessionManager;
    private final DeviceChannelMapper deviceChannelMapper;
    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;
    private final SipProvider sipProvider;

    public MessageHandler(GB28181Properties gb28181Properties,
                          SessionManager sessionManager,
                          DeviceChannelMapper deviceChannelMapper,
                          AddressFactory addressFactory,
                          HeaderFactory headerFactory,
                          MessageFactory messageFactory,
                          SipProvider sipProvider) {
        this.gb28181Properties = gb28181Properties;
        this.sessionManager = sessionManager;
        this.deviceChannelMapper = deviceChannelMapper;
        this.addressFactory = addressFactory;
        this.headerFactory = headerFactory;
        this.messageFactory = messageFactory;
        this.sipProvider = sipProvider;
    }

    public void handle(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);

            String deviceId = fromHeader.getAddress().getURI().toString()
                    .replace("sip:", "").split("@")[0];

            byte[] content = request.getRawContent();
            if (content == null || content.length == 0) {
                log.warn("Empty MESSAGE content from device: {}", deviceId);
                sendResponse(requestEvent, Response.BAD_REQUEST);
                return;
            }

            String charset = gb28181Properties.getSip().getCharset();
            String xmlContent = new String(content, Charset.forName(charset));

            log.debug("Received MESSAGE from {}: {}", deviceId, xmlContent);

            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(content));

            Element root = document.getDocumentElement();
            String cmdType = getElementText(root, "CmdType");

            switch (cmdType) {
                case "Keepalive":
                    handleKeepalive(deviceId, root);
                    break;
                case "Catalog":
                    handleCatalog(deviceId, root);
                    break;
                case "DeviceInfo":
                    handleDeviceInfo(deviceId, root);
                    break;
                case "DeviceStatus":
                    handleDeviceStatus(deviceId, root);
                    break;
                default:
                    log.warn("Unknown MESSAGE CmdType: {}", cmdType);
            }

            sendResponse(requestEvent, Response.OK);

        } catch (Exception e) {
            log.error("Error handling MESSAGE request", e);
            sendResponse(requestEvent, Response.SERVER_INTERNAL_ERROR);
        }
    }

    private void handleKeepalive(String deviceId, Element root) {
        sessionManager.updateKeepalive(deviceId);
        log.debug("Keepalive from device: {}", deviceId);
    }

    private void handleCatalog(String deviceId, Element root) {
        try {
            NodeList deviceList = root.getElementsByTagName("Item");
            log.info("Received catalog from device: {}, {} channels", deviceId, deviceList.getLength());

            for (int i = 0; i < deviceList.getLength(); i++) {
                Element item = (Element) deviceList.item(i);

                DeviceChannel channel = new DeviceChannel();
                channel.setDeviceId(deviceId);
                channel.setChannelId(getElementText(item, "DeviceID"));
                channel.setChannelName(getElementText(item, "Name"));
                channel.setManufacturer(getElementText(item, "Manufacturer"));
                channel.setModel(getElementText(item, "Model"));
                channel.setOwner(getElementText(item, "Owner"));
                channel.setCivilCode(getElementText(item, "CivilCode"));
                channel.setAddress(getElementText(item, "Address"));
                channel.setParental(getElementInt(item, "Parental"));
                channel.setParentId(getElementText(item, "ParentID"));
                channel.setSafetyWay(getElementInt(item, "SafetyWay"));
                channel.setRegisterWay(getElementInt(item, "RegisterWay"));
                channel.setSecrecy(getElementInt(item, "Secrecy"));
                channel.setStatus(getElementText(item, "Status"));

                String longitude = getElementText(item, "Longitude");
                String latitude = getElementText(item, "Latitude");
                if (longitude != null && !longitude.isEmpty()) {
                    channel.setLongitude(Double.parseDouble(longitude));
                }
                if (latitude != null && !latitude.isEmpty()) {
                    channel.setLatitude(Double.parseDouble(latitude));
                }

                channel.setPtzType(getElementInt(item, "PTZType"));

                // Save or update channel
                DeviceChannel existing = deviceChannelMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DeviceChannel>()
                                .eq("device_id", deviceId)
                                .eq("channel_id", channel.getChannelId())
                );

                if (existing == null) {
                    deviceChannelMapper.insert(channel);
                } else {
                    channel.setId(existing.getId());
                    deviceChannelMapper.updateById(channel);
                }
            }

            log.info("Catalog updated for device: {}", deviceId);

        } catch (Exception e) {
            log.error("Error handling catalog", e);
        }
    }

    private void handleDeviceInfo(String deviceId, Element root) {
        log.info("Device info from: {}", deviceId);
        // Can update device information in database
    }

    private void handleDeviceStatus(String deviceId, Element root) {
        String status = getElementText(root, "Status");
        log.debug("Device status from {}: {}", deviceId, status);
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

    private String getElementText(Element parent, String tagName) {
        try {
            NodeList nodeList = parent.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getTextContent();
            }
        } catch (Exception e) {
            log.debug("Element not found: {}", tagName);
        }
        return null;
    }

    private Integer getElementInt(Element parent, String tagName) {
        String text = getElementText(parent, tagName);
        if (text != null && !text.isEmpty()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                log.debug("Invalid integer value for {}: {}", tagName, text);
            }
        }
        return null;
    }
}
