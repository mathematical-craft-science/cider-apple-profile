package org.craft.mcs.cap.shared.platform.query;

import java.util.HashSet;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.assessment.SugarAcidityZone;
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
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be empty");
  }

  @Test
  void capabilityCriteriaHasFactoriesForCapableAndNotCapable() {
    assertThat(ChemicalProfileCapabilityCriteria.capable(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT).capable())
        .isTrue();
    assertThat(ChemicalProfileCapabilityCriteria.notCapable(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT).capable())
        .isFalse();
  }

  @Test
  void opaqueCursorCodecRoundTripsParts() {
    final var cursor = OpaqueCursorCodec.encode("Kingston Black", "0194d5df-1111-7000-8000-000000000001");

    assertThat(OpaqueCursorCodec.decode(cursor, 2))
        .containsExactly("Kingston Black", "0194d5df-1111-7000-8000-000000000001");
  }

  @Test
  void opaqueCursorCodecRejectsMalformedCursor() {
    assertThatThrownBy(() -> OpaqueCursorCodec.decode("not-a-valid-two-part-cursor", 2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cursor is malformed");
  }
}
