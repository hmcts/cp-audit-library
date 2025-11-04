package uk.gov.hmcts.cp.audit.mapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditPayloadMapperTest {

    AuditPayloadMapper auditPayloadMapper = new AuditPayloadMapperImpl();

    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;

    String url = "http://localhost:8080";

    @Test
    void mapper_should_populate_request_payload() {
        when(request.getPathInfo()).thenReturn(url);
        when(request.getQueryString()).thenReturn("param1=abc");
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(List.of("h1")));
        when(request.getHeader("h1")).thenReturn("h1-value");

        AuditRequestPayload payload = auditPayloadMapper.requestToPayLoad(request, "{}");

        assertThat(payload.getUrl()).isEqualTo("xxx");
        assertThat(payload.getUrlQueryParameters()).isEqualTo("param1=abc");
        assertThat(payload.getRequestHeaders()).isEqualTo(Map.of("h1", "h1-value"));
        assertThat(payload.getRequestBody()).isEqualTo("{}");
    }

    @Test
    void mapper_should_populate_response_payload() {
        when(request.getPathInfo()).thenReturn(url);
        when(response.getHeaderNames()).thenReturn(List.of("h2"));
        when(response.getHeader("h2")).thenReturn("h2-value");
        when(response.getStatus()).thenReturn(200);

        AuditResponsePayload payload = auditPayloadMapper.responseToPayload(request, response, "response-json");

        assertThat(payload.getUrl()).isEqualTo(url);
        assertThat(payload.getResponseHeaders()).isEqualTo(Map.of("h2", "h2-value"));
        assertThat(payload.getResponseBody()).isEqualTo("response-json");
        assertThat(payload.getResponseStatus()).isEqualTo(200);
    }
}