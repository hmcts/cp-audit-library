package uk.gov.hmcts.cp.audit.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuditClient {

    private final JmsTemplate jmsTemplate;

    public void postMessageToArtemis(final String messageName, final String message) {
        log.info("Posting audit message {} to Artemis", messageName);
        jmsTemplate.convertAndSend("jms.topic.auditing.event", message, m -> {
            m.setStringProperty("CPPNAME", messageName);
            return m;
        });
    }
}