package uk.gov.hmcts.cp.audit.mapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface AuditPayloadMapper {

    @Mapping(source = "request.pathInfo", target = "url")
    @Mapping(source = "request.queryString", target = "urlQueryParameters")
    @Mapping(source = "request", target = "requestHeaders", qualifiedByName = "requestHeadersToMap")
    @Mapping(source = "content", target = "requestBody")
    AuditRequestPayload requestToPayLoad(HttpServletRequest request, String content);

    @Mapping(source = "request.pathInfo", target = "url")
    @Mapping(source = "response", target = "responseHeaders", qualifiedByName = "responseHeadersToMap")
    @Mapping(source = "content", target = "responseBody")
    @Mapping(source = "response.status", target = "responseStatus")
    AuditResponsePayload responseToPayload(HttpServletRequest request, HttpServletResponse response, String content);

    @Named("requestHeadersToMap")
    static Map<String, String> requestHeadersToMap(final HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream().collect(
                Collectors.toMap(name -> name, name -> request.getHeader(name), (name, value) -> name + value)
        );
    }

    @Named("responseHeadersToMap")
    static Map<String, String> responseHeadersToMap(final HttpServletResponse response) {
        return response.getHeaderNames().stream().collect(
                Collectors.toMap(name -> name, name -> response.getHeader(name), (name, value) -> name + value)
        );
    }
}