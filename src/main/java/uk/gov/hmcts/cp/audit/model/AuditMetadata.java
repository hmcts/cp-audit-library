package uk.gov.hmcts.cp.audit.model;

import lombok.Builder;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("PMD.ShortVariable")
@Builder
public record AuditMetadata(
        UUID id,
        String name,
        String createdAt,
        Optional<Correlation> correlation,
        Optional<Context> context
) {

    public record Correlation(String client) {
    }

    public record Context(String user) {
    }
}
