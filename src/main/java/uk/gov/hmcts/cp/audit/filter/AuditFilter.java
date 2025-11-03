package uk.gov.hmcts.cp.audit.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import uk.gov.hmcts.cp.audit.mapper.AuditPayloadMapper;
import uk.gov.hmcts.cp.audit.model.AuditPayload;
import uk.gov.hmcts.cp.audit.service.AuditPayloadGenerationService;
import uk.gov.hmcts.cp.audit.service.AuditService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
@AllArgsConstructor
// Not convinced we should hide the AuditFilter bean. If subscriber brings in, they need to wire it up
// We should test with the full stack of spring beans
// We allow the disabling of the audit functionality to allow simplification of some integration tests
// @ConditionalOnProperty(name = "audit.http.enabled", havingValue = "true")
@Slf4j
public class AuditFilter extends OncePerRequestFilter {

    private static final int CACHE_LIMIT = 65_536; // 64 KB

    private final AuditPayloadMapper mapper;
    private final AuditService auditService;
    private final AuditPayloadGenerationService auditPayloadGenerationService;

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        final String path = request.getRequestURI();
        return path.contains("/health") || path.contains("/actuator");
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {

        final ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, CACHE_LIMIT);
        final ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(wrappedRequest, wrappedResponse);

        submitAuditPayload(wrappedRequest, wrappedResponse);

        // Lets just demonstrate that we can get the required information ... in int tests
        // But not switch till we are happy
        newAuditRequestPayload(request, getRequestBody(wrappedRequest));
        newAuditResponsePayload(request, response, getResponseBody(wrappedResponse));

        wrappedResponse.copyBodyToResponse();
    }

    private void newAuditRequestPayload(HttpServletRequest request, String content) {
        mapper.requestToPayLoad(request, content);
    }

    private void newAuditResponsePayload(HttpServletRequest request, HttpServletResponse response, String content) {
        mapper.responseToPayload(request, response, content);
    }

    private void submitAuditPayload(final ContentCachingRequestWrapper wrappedRequest, final ContentCachingResponseWrapper wrappedResponse) {
        final String contextPath = removeLeadingForwardSlash(wrappedRequest.getContextPath());
        final String requestPath = wrappedRequest.getServletPath();
        final String requestPayload = getPayload(wrappedRequest.getContentAsByteArray(), wrappedRequest.getCharacterEncoding());
        final Map<String, String> headers = getHeaders(wrappedRequest);
        final Map<String, String> queryParams = getQueryParams(wrappedRequest);
        final Map<String, String> pathParams = Map.of("param", "path-param-todo");

        final AuditPayload auditRequestPayload = auditPayloadGenerationService.generatePayload(contextPath, requestPayload, headers, queryParams, pathParams);
        auditService.postMessageToArtemis(auditRequestPayload);

        final String responsePayload = getPayload(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding());
        if (StringUtils.hasText(responsePayload)) {
            final AuditPayload auditResponsePayload = auditPayloadGenerationService.generatePayload(contextPath, responsePayload, headers);
            auditService.postMessageToArtemis(auditResponsePayload);
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request) throws IOException {
        return new String(request.getInputStream().readAllBytes(), request.getCharacterEncoding());
    }

    private String getResponseBody(ContentCachingResponseWrapper response) throws UnsupportedEncodingException {
        return new String(response.getContentAsByteArray(), response.getCharacterEncoding());
    }

    /**
     * This works for response but not request
     */
    private String getPayload(final byte[] content, final String encoding) {
        try {
            return new String(content, encoding);
        } catch (IOException ex) {
            log.error("Failed to parse payload for audit {}", ex.getMessage());
            throw new RuntimeException("Failed to parse payload for audit");
        }
    }

    private Map<String, String> getHeaders(final HttpServletRequest request) {
        final Map<String, String> headers = new HashMap<>();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    private Map<String, String> getQueryParams(final HttpServletRequest request) {
        final Map<String, String> queryParams = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> queryParams.put(key, String.join(",", value)));
        return queryParams;
    }


    private String removeLeadingForwardSlash(final String contextPath) {
        if (contextPath != null && contextPath.startsWith("/")) {
            return contextPath.substring(1);
        }
        return contextPath;
    }

}