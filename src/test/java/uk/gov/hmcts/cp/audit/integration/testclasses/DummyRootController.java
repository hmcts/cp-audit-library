package uk.gov.hmcts.cp.audit.integration.testclasses;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class DummyRootController {

    @PostMapping("/")
    public ResponseEntity<String> rootEndpoint() {
        log.info("/ endpoint hit");
        return ResponseEntity.ok("Hello");
    }
}
