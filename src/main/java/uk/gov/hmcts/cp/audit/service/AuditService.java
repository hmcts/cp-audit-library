package uk.gov.hmcts.cp.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cp.audit.model.AuditPayload;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class AuditService {

    private final ObjectMapper objectMapper;
    private final AuditClient client;

    public void postMessageToArtemis(final AuditPayload auditPayload) {
        if (null == auditPayload) {
            log.warn("AuditPayload is null");
            return;
        }

        try {
            final String valueAsString = objectMapper.writeValueAsString(auditPayload);
            log.info("Posting audit message to Artemis with ID = {} and timestamp = {}", auditPayload._metadata().id(), auditPayload.timestamp());
            client.postMessageToArtemis(auditPayload._metadata().name(), valueAsString);
        } catch (JsonProcessingException e) {
            // Log the error but don't re-throw to avoid breaking the main request flow
            final UUID auditMetadataId = (auditPayload._metadata() != null) ? auditPayload._metadata().id() : null;
            if (auditMetadataId != null) {
                log.error("Failed to post audit message with ID {} to Artemis", auditMetadataId);
            } else {
                log.error("Failed to post audit message to Artemis");
            }
        }
    }
}