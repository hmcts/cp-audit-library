package uk.gov.hmcts.cp.audit.integration.testclasses;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class JmsMessageListener {

    private DummyService dummyListener;

    @JmsListener(destination = "jms.topic.auditing.event", containerFactory = "jmsListenerContainerFactory")
    public void onMessage(String message) {
        log.info("JmsMessageListener received message:{}", message);
        dummyListener.getMessage(message);
    }
}