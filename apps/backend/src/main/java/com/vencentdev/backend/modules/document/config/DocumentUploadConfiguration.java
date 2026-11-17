package com.vencentdev.backend.modules.document.config;

import com.vencentdev.backend.modules.document.validation.DocumentUploadProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DocumentUploadProperties.class)
public class DocumentUploadConfiguration {}
