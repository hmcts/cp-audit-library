package uk.gov.hmcts.cp.audit.integration.testclasses;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DummyService {

    public void getMessage(String message) {
        // do nothing we will mock this bean in our tests
    }
}
