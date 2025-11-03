package uk.gov.hmcts.cp.audit.integration.testclasses;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class DummyController {

    @PostMapping("/case/{id}/details")
    public ResponseEntity<String> rootEndpoint(@PathVariable("id") String id) {
        log.info("/endpoint hit for id:{}", id);
        return ResponseEntity.ok("Hello");
    }
}
