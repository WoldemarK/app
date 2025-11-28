package com.example.individualsapi.aspect;

import com.example.individualsapi.metric.LoginCountTotalMetric;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoginMetricAspect {

    private final LoginCountTotalMetric loginCountTotalMetric;

    @AfterReturning("execution(public * com.example.individualsapi.service.TokenService.login(..))")
    public void afterLogin() {
        loginCountTotalMetric.incrementLoginCount();
    }
}
