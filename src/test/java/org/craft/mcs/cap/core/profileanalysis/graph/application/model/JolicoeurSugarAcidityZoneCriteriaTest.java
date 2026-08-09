package org.craft.mcs.cap.core.profileanalysis.graph.application.model;

import java.util.HashSet;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class JolicoeurSugarAcidityZoneCriteriaTest {

  @Test
  void copiesZones() {
    final var zones = new HashSet<>(Set.of(JolicoeurSugarAcidityZone.PERFECT_APPLES));
    final var criteria = new JolicoeurSugarAcidityZoneCriteria(zones);

    zones.add(JolicoeurSugarAcidityZone.HIGH_SUGAR_APPLES);

    assertThat(criteria.zones()).containsExactly(JolicoeurSugarAcidityZone.PERFECT_APPLES);
  }

  @Test
  void rejectsEmptySet() {
    assertThatThrownBy(() -> new JolicoeurSugarAcidityZoneCriteria(Set.of()))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("cannot be empty");
  }

  @Test
  void rejectsUnclassifiedAssessmentWithoutThrowing() {
    final var criteria = JolicoeurSugarAcidityZoneCriteria.of(
        JolicoeurSugarAcidityZone.PERFECT_APPLES
    );

    assertThat(criteria.matches(JolicoeurSugarAcidityZoneAssessment.unclassified())).isFalse();
  }
}
