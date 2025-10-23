package uk.gov.hmcts.cp.audit.integration;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
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
import uk.gov.hmcts.cp.audit.integration.testclasses.DummyService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Slf4j
class AuditIntegrationTest {

    @Resource
    private MockMvc mockMvc;

    @Captor
    ArgumentCaptor<String> stringCaptor;
    @MockitoBean
    DummyService dummyService;

    @Test
    void root_endpoint_should_be_audited() throws Exception {
        mockMvc
                .perform(
                        post("/")
                                .header("test-header", "some-value")
                                .content("json body"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Hello"));

        sleep_to_let_queue_flush();
        verify(dummyService, times(2)).getMessage(stringCaptor.capture());
        log.info("Test message was {}", stringCaptor.getAllValues());
        // we should map it to AuditPayload and check every value ... but we should do that in lower unit tests when we assemble the payload
        // But we make it difficult because we still have ObjectNode content in AuditPayload :(
        DocumentContext document = JsonPath.parse(stringCaptor.getAllValues());
        Object element0 = document.read(".[0]");
        Object element1 = document.read(".[1]");
        log.info("Request audit payload:{}", element0.toString());
        log.info("Response audit payload:{}", element1.toString());
    }

    @SneakyThrows
    private void sleep_to_let_queue_flush() {
        Thread.sleep(500);
    }
}