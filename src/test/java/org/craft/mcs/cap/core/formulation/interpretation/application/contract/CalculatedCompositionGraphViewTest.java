package org.craft.mcs.cap.core.formulation.interpretation.application.contract;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculatedCompositionView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculateCompositionResult;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationPotentialAbvView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CalculatedCompositionGraphViewTest {

  @Test
  void acceptsTanninLevelExactlyWhenCalculatedTanninIsAvailable() {
    assertThat(new CalculatedCompositionGraphView(
        calculation("1800"),
        FormulationTanninLevel.MEDIUM
    ).northAmericanTanninLevel())
        .isEqualTo(FormulationTanninLevel.MEDIUM);
    assertThat(new CalculatedCompositionGraphView(calculation(null), null).northAmericanTanninLevel())
        .isNull();
  }

  @Test
  void rejectsTanninLevelPresenceThatDoesNotMatchCalculatedTannin() {
    assertThatThrownBy(() -> new CalculatedCompositionGraphView(calculation("1800"), null))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage(
            "North American tannin level must be present exactly when composition tannin is available"
        );
    assertThatThrownBy(() -> new CalculatedCompositionGraphView(
        calculation(null),
        FormulationTanninLevel.MEDIUM
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage(
            "North American tannin level must be present exactly when composition tannin is available"
        );
  }

  private static @NonNull CalculateCompositionResult calculation(@Nullable String tannin) {
    final var specificGravity = SpecificGravityValue.of("1.060");
    return new CalculateCompositionResult(
        new CalculatedCompositionView(
            List.of(),
            new CalculatedCompositionView.Characteristics(
                specificGravity,
                TitratableAcidityValue.of("5.0"),
                tannin == null ? null : TanninValue.of(tannin),
                null
            ),
            null,
            List.of()
        ),
        new FormulationPotentialAbvView(
            specificGravity,
            SpecificGravityValue.of("1.000"),
            FormulationPotentialAbvView.FinalGravitySource.FIXED_DRY_CIDER_DEFAULT,
            AbvValue.of(new BigDecimal("7.875")),
            FormulationPotentialAbvView.Method.STANDARD_GRAVITY_DROP,
            FormulationPotentialAbvView.Confidence.MEDIUM
        ),
        null,
        null
    );
  }
}
