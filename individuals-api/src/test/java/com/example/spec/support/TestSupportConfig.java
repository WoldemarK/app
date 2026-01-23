package com.example.spec.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration(proxyBeanMethods = false)
@ComponentScan(basePackages = "com.example.environment.config.testcontainer")
public class TestSupportConfig {

}
