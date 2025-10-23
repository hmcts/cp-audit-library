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
import uk.gov.hmcts.cp.audit.model.AuditPayload;
import uk.gov.hmcts.cp.audit.service.AuditPayloadGenerationService;
import uk.gov.hmcts.cp.audit.service.AuditService;
import uk.gov.hmcts.cp.audit.service.PathParameterService;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
@AllArgsConstructor
// @ConditionalOnProperty(name = "audit.http.enabled", havingValue = "true")
@Slf4j
public class AuditFilter extends OncePerRequestFilter {

    private static final int CACHE_LIMIT = 65_536; // 64 KB

    private final AuditService auditService;
    private final AuditPayloadGenerationService auditPayloadGenerationService;
    private PathParameterService pathParameterService;

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

        wrappedResponse.copyBodyToResponse();
    }

    private void submitAuditPayload(final ContentCachingRequestWrapper wrappedRequest, final ContentCachingResponseWrapper wrappedResponse) {
        final String contextPath = removeLeadingForwardSlash(wrappedRequest.getContextPath());
        final String requestPath = wrappedRequest.getServletPath();
        final String requestPayload = getPayload(wrappedRequest.getContentAsByteArray(), wrappedRequest.getCharacterEncoding());
        final Map<String, String> headers = getHeaders(wrappedRequest);
        final Map<String, String> queryParams = getQueryParams(wrappedRequest);
        final Map<String, String> pathParams = pathParameterService.getPathParameters(requestPath);

        final AuditPayload auditRequestPayload = auditPayloadGenerationService.generatePayload(contextPath, requestPayload, headers, queryParams, pathParams);
        auditService.postMessageToArtemis(auditRequestPayload);

        final String responsePayload = getPayload(wrappedResponse.getContentAsByteArray(), wrappedResponse.getCharacterEncoding());
        if (StringUtils.hasText(responsePayload)) {
            final AuditPayload auditResponsePayload = auditPayloadGenerationService.generatePayload(contextPath, responsePayload, headers);
            auditService.postMessageToArtemis(auditResponsePayload);
        }
    }

    private String getPayload(final byte[] content, final String encoding) {
        try {
            return new String(content, encoding);
        } catch (IOException ex) {
            log.error("Unable to parse payload for audit", ex);
            return "";
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