package com.vencentdev.backend.modules.document.storage;

import java.util.Objects;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SupabaseStorageProperties.class)
@Conditional(SupabaseStorageConfiguredCondition.class)
class SupabaseStorageConfiguration {

  @Bean
  RestClient supabaseStorageRestClient(SupabaseStorageProperties properties) {
    String serviceRoleKey = Objects.requireNonNull(properties.getServiceRoleKey());
    return RestClient.builder()
        .baseUrl(properties.storageApiUrl().toString())
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + serviceRoleKey)
        .defaultHeader("apikey", serviceRoleKey)
        .build();
  }

  @Bean
  SupabaseStorageTransport supabaseStorageTransport(
      RestClient supabaseStorageRestClient, SupabaseStorageProperties properties) {
    return new SupabaseStorageRestClient(
        supabaseStorageRestClient, properties.storageApiUrl());
  }

  @Bean
  @ConditionalOnMissingBean(DocumentStorage.class)
  DocumentStorage documentStorage(
      SupabaseStorageTransport transport, SupabaseStorageProperties properties) {
    return new SupabaseStorageAdapter(
        transport,
        properties.getBucket(),
        properties.getSignedDownloadUrlTtl(),
        new DocumentStorageKeyGenerator());
  }
}
