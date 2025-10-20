package uk.gov.moj.cpp.filter.audit.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PathParameterNameExtractorTest {

    private final PathParameterNameExtractor extractor = new PathParameterNameExtractor();

    private static Stream<Arguments> pathParameterCases() {
        return Stream.of(
                of("/users/{userId}/orders/{orderId}", List.of("userId", "orderId")),
                of("/users/orders", List.of()),
                of("/users/{userId}/profile", List.of("userId")),
                of("", List.of()),
                of(null, List.of())
        );
    }

    @ParameterizedTest(name = "Extracts path parameters from \"{0}\"")
    @MethodSource("pathParameterCases")
    @DisplayName("Extracts path parameters from various API specs")
    void extractsPathParametersFromVariousApiSpecs(final String path, final List<String> expected) {
        final List<String> result = extractor.extractPathParametersFromApiSpec(path);
        assertThat(result).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Ignores invalid path parameter syntax")
    void ignoresInvalidPathParameterSyntax() {
        final String path = "/users/{userId/orders/{orderId}";
        final List<String> result = extractor.extractPathParametersFromApiSpec(path);
        assertThat(result).containsExactly("orderId");
    }
}
