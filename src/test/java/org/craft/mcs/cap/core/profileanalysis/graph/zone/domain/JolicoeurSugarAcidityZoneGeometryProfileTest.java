package org.craft.mcs.cap.core.profileanalysis.graph.zone.domain;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphAxisScale;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class JolicoeurSugarAcidityZoneGeometryProfileTest {

  @Test
  void rejectsEmptyGeometryProfile() {
    assertThatThrownBy(() -> new JolicoeurSugarAcidityZoneGeometryProfile(
        List.of(),
        specificGravityScale(),
        titratableAcidityScale()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("sugar-acidity zone geometry profile must contain at least one zone");
  }

  @Test
  void rejectsDuplicateZones() {
    final var geometry = new RectangleSugarAcidityZoneGeometry(
        JolicoeurSugarAcidityZone.PERFECT_APPLES,
        MeasuredValueRange.closedOpen(
            SpecificGravityValue.of("1.050"),
            SpecificGravityValue.of("1.060")
        ),
        MeasuredValueRange.closedOpen(
            TitratableAcidityValue.of("4.0"),
            TitratableAcidityValue.of("6.0")
        )
    );

    assertThatThrownBy(() -> new JolicoeurSugarAcidityZoneGeometryProfile(
        List.of(geometry, geometry),
        specificGravityScale(),
        titratableAcidityScale()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("sugar-acidity zone geometry profile cannot contain duplicate zones");
  }

  private static SugarAcidityGraphAxisScale<SpecificGravityValue> specificGravityScale() {
    return new SugarAcidityGraphAxisScale<>(
        SpecificGravityValue.of("1.000"),
        SpecificGravityValue.of("1.100"),
        List.of(SpecificGravityValue.of("1.050"))
    );
  }

  private static SugarAcidityGraphAxisScale<TitratableAcidityValue> titratableAcidityScale() {
    return new SugarAcidityGraphAxisScale<>(
        TitratableAcidityValue.of("0"),
        TitratableAcidityValue.of("10"),
        List.of(TitratableAcidityValue.of("5"))
    );
  }
}
