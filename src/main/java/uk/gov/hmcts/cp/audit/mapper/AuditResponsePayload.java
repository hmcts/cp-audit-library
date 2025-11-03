package uk.gov.hmcts.cp.audit.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class AuditResponsePayload {

    private String url;
    private Map<String, String> responseHeaders;
    private String responseBody;
    private Integer responseStatus;
}