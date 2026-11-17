package com.vencentdev.backend.modules.project.service;

import com.vencentdev.backend.modules.project.enums.ProjectStatus;
import com.vencentdev.backend.modules.project.enums.ProjectTransitionAction;
import java.util.EnumMap;
import java.util.Map;

final class ProjectTransitionRules {

  private static final Map<ProjectTransitionAction, Map<ProjectStatus, ProjectStatus>> TRANSITIONS =
      buildTransitions();

  private ProjectTransitionRules() {}

  static ProjectStatus target(
      ProjectStatus current,
      ProjectTransitionAction action,
      ProjectStatus suspendedFromStatus) {
    if (action == ProjectTransitionAction.RESUME) {
      return current == ProjectStatus.SUSPENDED ? suspendedFromStatus : null;
    }
    return TRANSITIONS.getOrDefault(action, Map.of()).get(current);
  }

  private static Map<ProjectTransitionAction, Map<ProjectStatus, ProjectStatus>> buildTransitions() {
    EnumMap<ProjectTransitionAction, Map<ProjectStatus, ProjectStatus>> transitions =
        new EnumMap<>(ProjectTransitionAction.class);

    add(transitions, ProjectTransitionAction.DOCUMENT_UPLOADED, ProjectStatus.DRAFT,
        ProjectStatus.DOCUMENT_UPLOADED);
    add(transitions, ProjectTransitionAction.PREPARE, ProjectStatus.DOCUMENT_UPLOADED,
        ProjectStatus.DOCUMENT_PREPARED);
    add(transitions, ProjectTransitionAction.REOPEN_DOCUMENT, ProjectStatus.DOCUMENT_PREPARED,
        ProjectStatus.DOCUMENT_UPLOADED);
    add(transitions, ProjectTransitionAction.ADD_PARTICIPANTS, ProjectStatus.DOCUMENT_PREPARED,
        ProjectStatus.PARTICIPANTS_ADDED);
    add(transitions, ProjectTransitionAction.CORRECT_PARTICIPANTS, ProjectStatus.PARTICIPANTS_ADDED,
        ProjectStatus.DOCUMENT_PREPARED);
    add(transitions, ProjectTransitionAction.MARK_READY, ProjectStatus.PARTICIPANTS_ADDED,
        ProjectStatus.READY_TO_SEND);
    add(transitions, ProjectTransitionAction.EDIT_BEFORE_SEND, ProjectStatus.READY_TO_SEND,
        ProjectStatus.DOCUMENT_PREPARED);
    add(transitions, ProjectTransitionAction.SEND, ProjectStatus.READY_TO_SEND,
        ProjectStatus.SENT);
    add(transitions, ProjectTransitionAction.VIEW, ProjectStatus.SENT, ProjectStatus.VIEWED);
    add(transitions, ProjectTransitionAction.PARTIALLY_SIGN, ProjectStatus.VIEWED,
        ProjectStatus.PARTIALLY_SIGNED);
    add(transitions, ProjectTransitionAction.REOPEN_APPROVAL, ProjectStatus.AWAITING_APPROVAL,
        ProjectStatus.PARTIALLY_SIGNED);
    add(transitions, ProjectTransitionAction.AWAIT_APPROVAL, ProjectStatus.VIEWED,
        ProjectStatus.AWAITING_APPROVAL);
    add(transitions, ProjectTransitionAction.AWAIT_APPROVAL, ProjectStatus.PARTIALLY_SIGNED,
        ProjectStatus.AWAITING_APPROVAL);
    add(transitions, ProjectTransitionAction.AWAIT_NOTARIZATION, ProjectStatus.VIEWED,
        ProjectStatus.AWAITING_NOTARIZATION);
    add(transitions, ProjectTransitionAction.AWAIT_NOTARIZATION, ProjectStatus.PARTIALLY_SIGNED,
        ProjectStatus.AWAITING_NOTARIZATION);
    add(transitions, ProjectTransitionAction.AWAIT_NOTARIZATION, ProjectStatus.AWAITING_APPROVAL,
        ProjectStatus.AWAITING_NOTARIZATION);
    add(transitions, ProjectTransitionAction.START_NOTARIZATION,
        ProjectStatus.AWAITING_NOTARIZATION, ProjectStatus.NOTARIZATION_IN_PROGRESS);

    for (ProjectStatus source : new ProjectStatus[] {
      ProjectStatus.VIEWED,
      ProjectStatus.PARTIALLY_SIGNED,
      ProjectStatus.AWAITING_APPROVAL,
      ProjectStatus.AWAITING_NOTARIZATION,
      ProjectStatus.NOTARIZATION_IN_PROGRESS
    }) {
      add(transitions, ProjectTransitionAction.COMPLETE, source, ProjectStatus.COMPLETED);
    }

    add(transitions, ProjectTransitionAction.ARCHIVE, ProjectStatus.COMPLETED,
        ProjectStatus.ARCHIVED);
    add(transitions, ProjectTransitionAction.REVOKE, ProjectStatus.COMPLETED,
        ProjectStatus.REVOKED);

    for (ProjectStatus source : activeStatuses()) {
      add(transitions, ProjectTransitionAction.EXPIRE, source, ProjectStatus.EXPIRED);
      add(transitions, ProjectTransitionAction.CANCEL, source, ProjectStatus.CANCELLED);
      add(transitions, ProjectTransitionAction.SUSPEND, source, ProjectStatus.SUSPENDED);
    }

    for (ProjectStatus source : new ProjectStatus[] {
      ProjectStatus.VIEWED,
      ProjectStatus.PARTIALLY_SIGNED,
      ProjectStatus.AWAITING_APPROVAL,
      ProjectStatus.AWAITING_NOTARIZATION,
      ProjectStatus.NOTARIZATION_IN_PROGRESS
    }) {
      add(transitions, ProjectTransitionAction.DECLINE, source, ProjectStatus.DECLINED);
    }

    for (ProjectStatus source : activeStatuses()) {
      add(transitions, ProjectTransitionAction.REJECT, source, ProjectStatus.REJECTED);
    }
    add(transitions, ProjectTransitionAction.CANCEL, ProjectStatus.SUSPENDED,
        ProjectStatus.CANCELLED);
    add(transitions, ProjectTransitionAction.REJECT, ProjectStatus.SUSPENDED,
        ProjectStatus.REJECTED);

    return transitions;
  }

  private static ProjectStatus[] activeStatuses() {
    return new ProjectStatus[] {
      ProjectStatus.DRAFT,
      ProjectStatus.DOCUMENT_UPLOADED,
      ProjectStatus.DOCUMENT_PREPARED,
      ProjectStatus.PARTICIPANTS_ADDED,
      ProjectStatus.READY_TO_SEND,
      ProjectStatus.SENT,
      ProjectStatus.VIEWED,
      ProjectStatus.PARTIALLY_SIGNED,
      ProjectStatus.AWAITING_APPROVAL,
      ProjectStatus.AWAITING_NOTARIZATION,
      ProjectStatus.NOTARIZATION_IN_PROGRESS
    };
  }

  private static void add(
      Map<ProjectTransitionAction, Map<ProjectStatus, ProjectStatus>> transitions,
      ProjectTransitionAction action,
      ProjectStatus source,
      ProjectStatus target) {
    transitions
        .computeIfAbsent(action, ignored -> new EnumMap<>(ProjectStatus.class))
        .put(source, target);
  }
}
