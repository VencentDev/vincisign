package com.vencentdev.backend.modules.document.storage;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

final class SupabaseStorageConfiguredCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return hasText(context.getEnvironment().getProperty("app.storage.supabase.url"))
        && hasText(context.getEnvironment().getProperty("app.storage.supabase.service-role-key"));
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
