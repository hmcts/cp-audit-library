package uk.gov.hmcts.cp.audit.integration;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cp.audit.mapper.AuditPayloadMapper;
import uk.gov.hmcts.cp.audit.mapper.AuditPayloadMapperImpl;
import uk.gov.hmcts.cp.audit.mapper.AuditRequestPayload;
import uk.gov.hmcts.cp.audit.mapper.AuditResponsePayload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Interim solution
 * We use an integration / spring-boot test here so that we can confirm that the values that we pull from the
 * http request / response accurately contain the fields that we require
 * Once we wire the mapper into actually sending the payloads we can use an integration test that picks up the request
 * being sent to artemis. And drop this one
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Slf4j
class AuditMapperIntegrationTest {

    @Resource
    private MockMvc mockMvc;

    @Captor
    ArgumentCaptor<HttpServletRequest> requestCaptor;
    @Captor
    ArgumentCaptor<HttpServletResponse> responseCaptor;
    @Captor
    ArgumentCaptor<String> stringCaptor;
    @MockitoBean
    AuditPayloadMapper mockAuditPayloadMapper;

    AuditPayloadMapper realAuditPayloadMapper = new AuditPayloadMapperImpl();

    @Test
    void root_endpoint_should_send_mapped_request_payload() throws Exception {
        mockMvc
                .perform(
                        post("/case/1234/details?param1=abc")
                                .header("test-header", "some-value")
                                .content("json body"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Hello"));
        verify(mockAuditPayloadMapper).requestToPayLoad(requestCaptor.capture(), stringCaptor.capture());

        AuditRequestPayload requestPayload = realAuditPayloadMapper.requestToPayLoad(requestCaptor.getValue(), stringCaptor.getValue());

        assertThat(requestPayload.getUrl()).isEqualTo("/case/1234/details");
        assertThat(requestPayload.getUrlQueryParameters()).isEqualTo("param1=abc");
        assertThat(requestPayload.getRequestHeaders()).containsEntry("Content-Length", "9");
        assertThat(requestPayload.getRequestHeaders()).containsEntry("test-header", "some-value");
        assertThat(requestPayload.getRequestBody()).isEqualTo("json body");
    }

    @Test
    void root_endpoint_should_send_mapped_response_payload() throws Exception {
        mockMvc
                .perform(
                        post("/case/1234/details?param1=abc")
                                .header("test-header", "some-value")
                                .content("json body"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Hello"));
        verify(mockAuditPayloadMapper).responseToPayload(requestCaptor.capture(), responseCaptor.capture(), stringCaptor.capture());

        AuditResponsePayload responsePayload = realAuditPayloadMapper.responseToPayload(requestCaptor.getValue(), responseCaptor.getValue(), stringCaptor.getValue());

        assertThat(responsePayload.getUrl()).isEqualTo("/case/1234/details");
        assertThat(responsePayload.getResponseHeaders()).containsEntry("Content-Length", "5");
        assertThat(responsePayload.getResponseBody()).isEqualTo("Hello");
        assertThat(responsePayload.getResponseStatus()).isEqualTo(200);
    }
}