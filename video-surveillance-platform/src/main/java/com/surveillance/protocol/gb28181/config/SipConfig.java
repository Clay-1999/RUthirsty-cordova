package com.surveillance.protocol.gb28181.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sip.*;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import java.util.Properties;
import java.util.TooManyListenersException;

/**
 * SIP Stack Configuration
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "gb28181", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SipConfig {

    private final GB28181Properties gb28181Properties;

    public SipConfig(GB28181Properties gb28181Properties) {
        this.gb28181Properties = gb28181Properties;
    }

    @Bean
    public SipFactory sipFactory() {
        SipFactory factory = SipFactory.getInstance();
        factory.setPathName("gov.nist");
        return factory;
    }

    @Bean
    public AddressFactory addressFactory(SipFactory sipFactory) throws PeerUnavailableException {
        return sipFactory.createAddressFactory();
    }

    @Bean
    public HeaderFactory headerFactory(SipFactory sipFactory) throws PeerUnavailableException {
        return sipFactory.createHeaderFactory();
    }

    @Bean
    public MessageFactory messageFactory(SipFactory sipFactory) throws PeerUnavailableException {
        return sipFactory.createMessageFactory();
    }

    @Bean
    public SipStack sipStack(SipFactory sipFactory) throws PeerUnavailableException {
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "GB28181-SIP-Stack");
        properties.setProperty("javax.sip.IP_ADDRESS", gb28181Properties.getSip().getIp());
        properties.setProperty("javax.sip.OUTBOUND_PROXY", gb28181Properties.getSip().getIp() + ":" + gb28181Properties.getSip().getPort() + "/" + gb28181Properties.getSip().getTransport());
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "logs/sip_server.log");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "logs/sip_debug.log");
        properties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT", "true");
        properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY",
                "gov.nist.javax.sip.stack.NioMessageProcessorFactory");
        properties.setProperty("gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE", "20");

        SipStack sipStack = sipFactory.createSipStack(properties);
        log.info("SIP Stack created successfully");
        return sipStack;
    }

    @Bean
    public SipProvider sipProvider(SipStack sipStack) throws ObjectInUseException,
            TransportNotSupportedException, InvalidArgumentException, TooManyListenersException {

        String transport = gb28181Properties.getSip().getTransport();
        Integer port = gb28181Properties.getSip().getPort();

        ListeningPoint listeningPoint = sipStack.createListeningPoint(
                gb28181Properties.getSip().getIp(),
                port,
                transport
        );

        SipProvider sipProvider = sipStack.createSipProvider(listeningPoint);
        log.info("SIP Provider created on {}:{} ({})",
                gb28181Properties.getSip().getIp(), port, transport);

        return sipProvider;
    }
}
