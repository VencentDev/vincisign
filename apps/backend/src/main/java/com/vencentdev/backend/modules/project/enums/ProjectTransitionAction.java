package com.vencentdev.backend.modules.project.enums;

import java.util.Arrays;

public enum ProjectTransitionAction {
  DOCUMENT_UPLOADED("document-uploaded", "project.document_uploaded", false),
  PREPARE("prepare", "project.document_prepared", true),
  REOPEN_DOCUMENT("reopen-document", "project.document_uploaded", true),
  ADD_PARTICIPANTS("add-participants", "project.participants_added", true),
  CORRECT_PARTICIPANTS("correct-participants", "project.document_prepared", true),
  MARK_READY("mark-ready", "project.ready_to_send", true),
  EDIT_BEFORE_SEND("edit-before-send", "project.document_prepared", true),
  SEND("send", "project.sent", true),
  VIEW("view", "project.viewed", true),
  PARTIALLY_SIGN("partially-sign", "project.partially_signed", true),
  REOPEN_APPROVAL("reopen-approval", "project.partially_signed", true),
  AWAIT_APPROVAL("await-approval", "project.awaiting_approval", true),
  AWAIT_NOTARIZATION("await-notarization", "project.awaiting_notarization", true),
  START_NOTARIZATION("start-notarization", "project.notarization_started", true),
  COMPLETE("complete", "project.completed", true),
  ARCHIVE("archive", "project.archived", true),
  EXPIRE("expire", "project.expired", true),
  CANCEL("cancel", "project.cancelled", true),
  DECLINE("decline", "project.declined", true),
  REJECT("reject", "project.rejected", true),
  SUSPEND("suspend", "project.suspended", true),
  RESUME("resume", "project.resumed", true),
  REVOKE("revoke", "project.revoked", true);

  private final String apiValue;
  private final String eventType;
  private final boolean publicAction;

  ProjectTransitionAction(String apiValue, String eventType, boolean publicAction) {
    this.apiValue = apiValue;
    this.eventType = eventType;
    this.publicAction = publicAction;
  }

  public String apiValue() {
    return apiValue;
  }

  public String eventType() {
    return eventType;
  }

  public static ProjectTransitionAction fromApiValue(String value) {
    return Arrays.stream(values())
        .filter(action -> action.publicAction && action.apiValue.equals(value))
        .findFirst()
        .orElse(null);
  }
}
