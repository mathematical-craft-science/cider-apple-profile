package org.craft.mcs.cap.shared.platform.query;

import java.util.HashSet;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.application.model.SugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class PointCriteriaTest {

  @Test
  void textSearchCriteriaNormalizesInput() {
    final var criteria = new TextSearchCriteria("  Kingston  ");

    assertThat(criteria.query()).isEqualTo("Kingston");
  }

  @Test
  void textSearchCriteriaRejectsBlankInput() {
    assertThatThrownBy(() -> new TextSearchCriteria(" "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be blank");
  }

  @Test
  void sugarAcidityZoneCriteriaCopiesZones() {
    final var zones = new HashSet<>(Set.of(SugarAcidityZone.PERFECT_APPLES));
    final var criteria = new SugarAcidityZoneCriteria(zones);

    zones.add(SugarAcidityZone.SUGAR_RICH_APPLES);

    assertThat(criteria.zones()).containsExactly(SugarAcidityZone.PERFECT_APPLES);
  }

  @Test
  void sugarAcidityZoneCriteriaRejectsEmptySet() {
    assertThatThrownBy(() -> new SugarAcidityZoneCriteria(Set.of()))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("cannot be empty");
  }

  @Test
  void capabilityCriteriaHasFactoriesForCapableAndNotCapable() {
    assertThat(ChemicalProfileCapabilityCriteria.capable(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT).capable())
        .isTrue();
    assertThat(ChemicalProfileCapabilityCriteria.notCapable(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT).capable())
        .isFalse();
  }

}
