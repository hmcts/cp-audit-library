package uk.gov.hmcts.cp.audit.mapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class AuditRequestPayload {

    private String url;
    private String urlQueryParameters;
    private Map<String,String> requestHeaders;
    // Seems wrong that we send the whole body but lets go with it for now
    private String requestBody;
}
