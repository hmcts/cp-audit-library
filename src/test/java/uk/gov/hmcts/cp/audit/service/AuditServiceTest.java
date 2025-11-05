package uk.gov.hmcts.cp.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.MessagePostProcessor;
import uk.gov.hmcts.cp.audit.model.AuditMetadata;
import uk.gov.hmcts.cp.audit.model.AuditPayload;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private AuditClient auditClient;

    @InjectMocks
    private AuditService auditService;

    @Captor
    private ArgumentCaptor<MessagePostProcessor> captor;

    @Test
    void dontPostMessageToArtemisWhenAuditPayloadIsNull() {
        auditService.postMessageToArtemis(null);
        verifyNoInteractions(objectMapper);
    }

    @Test
    void logsAndSendsMessageWhenSerializationSucceeds() throws JsonProcessingException, JMSException {
        final AuditPayload auditPayload = mock(AuditPayload.class);
        final String serializedMessage = "{\"key\":\"value\"}";
        when(objectMapper.writeValueAsString(auditPayload)).thenReturn(serializedMessage);
        when(auditPayload.timestamp()).thenReturn("2024-10-10T10:00:00Z");
        final String auditMethodName = "dummy-name";
        when(auditPayload._metadata()).thenReturn(AuditMetadata.builder().id(randomUUID()).name(auditMethodName).build());

        auditService.postMessageToArtemis(auditPayload);

        verify(auditClient).postMessageToArtemis("dummy-name", serializedMessage);
    }

    @Test
    void logsErrorWhenSerializationFails() throws JsonProcessingException {
        final AuditPayload auditPayload = mock(AuditPayload.class);
        when(objectMapper.writeValueAsString(auditPayload)).thenThrow(new JsonProcessingException("Serialization error") {
        });

        auditService.postMessageToArtemis(auditPayload);

        verify(objectMapper).writeValueAsString(auditPayload);
        // verify(jmsTemplate, never()).convertAndSend(anyString(), anyString());
    }
}
