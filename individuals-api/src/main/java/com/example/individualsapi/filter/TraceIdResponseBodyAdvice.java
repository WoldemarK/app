//package com.example.individualsapi.filter;
//
//import com.example.individuals_api.api.dto.UserRegistrationRequest;
//import io.opentelemetry.api.trace.Span;
//import io.opentelemetry.api.trace.SpanContext;
//import org.springframework.core.MethodParameter;
//import org.springframework.http.MediaType;
//import org.springframework.http.converter.HttpMessageConverter;
//import org.springframework.http.server.ServerHttpRequest;
//import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//@ControllerAdvice
//public class TraceIdResponseBodyAdvice implements ResponseBodyAdvice<Object> {
//    @Override
//    public boolean supports(MethodParameter returnType,
//                            Class<? extends HttpMessageConverter<?>> converterType) {
//        return true;
//    }
//
//    @Override
//    public Object beforeBodyWrite(Object body,
//                                  MethodParameter returnType,
//                                  MediaType selectedContentType,
//                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
//                                  ServerHttpRequest request,
//                                  ServerHttpResponse response) {
//        SpanContext ctx = Span.current().getSpanContext();
//
//        if (body instanceof UserRegistrationRequest user) {
//            Map<String, Object> result = new HashMap<>();
//            result.put("email", user.getEmail());
//            result.put("username", user.getUsername());
//            result.put("firstName", user.getFirstName());
//            result.put("lastName", user.getLastName());
//            result.put("traceId", ctx.getTraceId());
//            return result;
//        }
//        return body;
//    }
//}
