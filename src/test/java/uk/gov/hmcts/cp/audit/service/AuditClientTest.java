package uk.gov.hmcts.cp.audit.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Slf4j
class AuditClientTest {
    @Mock
    JmsTemplate jmsTemplate;

    @InjectMocks
    AuditClient auditClient;

    @Captor
    ArgumentCaptor<MessagePostProcessor> messageCaptor;

    @Test
    void post_message_should_send_to_artemis() {
        auditClient.postMessageToArtemis("My message", "{}");
        verify(jmsTemplate).convertAndSend(eq("jms.topic.auditing.event"), eq("{}"), messageCaptor.capture());
    }
}