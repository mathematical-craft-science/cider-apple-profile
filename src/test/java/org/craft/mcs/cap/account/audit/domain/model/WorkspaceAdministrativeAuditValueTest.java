package org.craft.mcs.cap.account.audit.domain.model;

import org.craft.mcs.cap.core.account.audit.domain.model.WorkspaceAdministrativeAuditRuleException;
import org.craft.mcs.cap.core.account.audit.domain.model.value.WorkspaceAdministrativeAuditValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkspaceAdministrativeAuditValueTest {

  @Test
  void normalizesAuditValue() {
    assertThat(WorkspaceAdministrativeAuditValue.of("  ACTIVE  "))
        .extracting(WorkspaceAdministrativeAuditValue::value)
        .isEqualTo("ACTIVE");
  }

  @Test
  void rejectsBlankAuditValue() {
    assertThatThrownBy(() -> WorkspaceAdministrativeAuditValue.of("  "))
        .isInstanceOf(WorkspaceAdministrativeAuditRuleException.class)
        .hasMessage("workspace administrative audit value cannot be blank");
  }

  @Test
  void rejectsAuditValueExceedingPersistenceBoundary() {
    final var tooLong = "a".repeat(2_001);

    assertThatThrownBy(() -> WorkspaceAdministrativeAuditValue.of(tooLong))
        .isInstanceOf(WorkspaceAdministrativeAuditRuleException.class)
        .hasMessage("workspace administrative audit value cannot exceed 2000 characters");
  }
}
