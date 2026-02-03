package com.surveillance.protocol.gb28181.handler;

import com.surveillance.protocol.gb28181.config.GB28181Properties;
import com.surveillance.protocol.gb28181.session.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.*;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * SIP Request Processor
 * Main entry point for all SIP messages
 */
@Slf4j
@Component
public class SipRequestProcessor implements SipListener {

    private final GB28181Properties gb28181Properties;
    private final SessionManager sessionManager;
    private final AddressFactory addressFactory;
    private final HeaderFactory headerFactory;
    private final MessageFactory messageFactory;
    private final SipProvider sipProvider;

    private final RegisterHandler registerHandler;
    private final MessageHandler messageHandler;
    private final InviteHandler inviteHandler;
    private final ByeHandler byeHandler;

    public SipRequestProcessor(GB28181Properties gb28181Properties,
                               SessionManager sessionManager,
                               AddressFactory addressFactory,
                               HeaderFactory headerFactory,
                               MessageFactory messageFactory,
                               SipProvider sipProvider,
                               RegisterHandler registerHandler,
                               MessageHandler messageHandler,
                               InviteHandler inviteHandler,
                               ByeHandler byeHandler) {
        this.gb28181Properties = gb28181Properties;
        this.sessionManager = sessionManager;
        this.addressFactory = addressFactory;
        this.headerFactory = headerFactory;
        this.messageFactory = messageFactory;
        this.sipProvider = sipProvider;
        this.registerHandler = registerHandler;
        this.messageHandler = messageHandler;
        this.inviteHandler = inviteHandler;
        this.byeHandler = byeHandler;

        try {
            sipProvider.addSipListener(this);
            log.info("SIP Request Processor initialized");
        } catch (TooManyListenersException e) {
            log.error("Failed to add SIP listener", e);
        }
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            String method = request.getMethod();

            log.info("Received SIP request: {}", method);

            switch (method) {
                case Request.REGISTER:
                    registerHandler.handle(requestEvent);
                    break;
                case Request.MESSAGE:
                    messageHandler.handle(requestEvent);
                    break;
                case Request.INVITE:
                    inviteHandler.handle(requestEvent);
                    break;
                case Request.BYE:
                    byeHandler.handle(requestEvent);
                    break;
                case Request.ACK:
                    // ACK doesn't need response
                    break;
                default:
                    log.warn("Unsupported SIP method: {}", method);
                    sendResponse(requestEvent, Response.NOT_IMPLEMENTED);
            }
        } catch (Exception e) {
            log.error("Error processing SIP request", e);
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        log.debug("Received SIP response: {}", response.getStatusCode());
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        log.warn("SIP transaction timeout");
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        log.error("SIP IO exception", exceptionEvent);
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        log.debug("SIP transaction terminated");
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        log.debug("SIP dialog terminated");
    }

    /**
     * Send SIP response
     */
    private void sendResponse(RequestEvent requestEvent, int statusCode) {
        try {
            ServerTransaction serverTransaction = requestEvent.getServerTransaction();
            if (serverTransaction == null) {
                serverTransaction = sipProvider.getNewServerTransaction(requestEvent.getRequest());
            }

            Response response = messageFactory.createResponse(statusCode, requestEvent.getRequest());
            serverTransaction.sendResponse(response);
        } catch (Exception e) {
            log.error("Failed to send SIP response", e);
        }
    }
}
