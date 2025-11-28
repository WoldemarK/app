package com.example.individualsapi.filter;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class TraceIdToMdcFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        SpanContext ctx = Span.current().getSpanContext();
        boolean added;

        if (ctx.isValid()) {
            MDC.put("trace_id", ctx.getTraceId());
            MDC.put("span_id", ctx.getSpanId());
            added = true;
        } else {
            added = false;
        }

        return chain.filter(exchange)
                .doOnTerminate(() -> {
                    if (added) {
                        MDC.remove("trace_id");
                        MDC.remove("span_id");
                    }
                })
                .doOnCancel(() -> {
                    if (added) {
                        MDC.remove("trace_id");
                        MDC.remove("span_id");
                    }
                });
    }
}
