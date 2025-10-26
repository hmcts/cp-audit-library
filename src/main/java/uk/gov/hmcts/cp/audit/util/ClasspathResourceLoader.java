package uk.gov.hmcts.cp.audit.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ClasspathResourceLoader {

    private final ResourceLoader resourceLoader;

    public Optional<Resource> loadFilesByPattern(final String resourcePattern) {
        try {
            final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);

            final Resource[] resources = resolver.getResources("classpath*:**/*" + resourcePattern);

            log.info("Found {} files matching pattern {}", resources.length, resourcePattern);

            return resources.length > 0 ? Optional.of(resources[0]) : Optional.empty();
        } catch (Exception e) {
            log.error("Error loading resources for pattern: {}", resourcePattern, e);
            return Optional.empty();
        }
    }
}
