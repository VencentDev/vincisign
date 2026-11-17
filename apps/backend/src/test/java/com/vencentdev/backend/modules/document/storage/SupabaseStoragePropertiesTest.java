package com.vencentdev.backend.modules.document.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SupabaseStoragePropertiesTest {

  @Test
  void bindsTheSignedDownloadTtlNameUsedByApplicationConfiguration() {
    Binder binder =
        new Binder(
            new MapConfigurationPropertySource(
                Map.of(
                    "app.storage.supabase.url", "https://project.supabase.co",
                    "app.storage.supabase.service-role-key", "server-only-key",
                    "app.storage.supabase.signed-download-url-ttl", "300s")));

    SupabaseStorageProperties properties =
        binder.bind("app.storage.supabase", SupabaseStorageProperties.class).get();

    assertThat(properties.getSignedDownloadUrlTtl()).isEqualTo(Duration.ofSeconds(300));
  }

  @Test
  void doesNotCreateAStorageAdapterWhenLocalCredentialsAreBlank() {
    new ApplicationContextRunner()
        .withUserConfiguration(SupabaseStorageConfiguration.class)
        .withPropertyValues(
            "app.storage.supabase.url=", "app.storage.supabase.service-role-key=")
        .run(context -> assertThat(context).doesNotHaveBean(DocumentStorage.class));
  }
}
