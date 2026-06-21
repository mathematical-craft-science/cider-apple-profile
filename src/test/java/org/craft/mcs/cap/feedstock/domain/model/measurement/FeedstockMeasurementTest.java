package org.craft.mcs.cap.feedstock.domain.model.measurement;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.craft.mcs.cap.shared.testing.TestClocks.futureInstant;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FeedstockMeasurementTest {

  @Test
  void rejectsFutureMeasurementDateOnCreation() {
    assertThatThrownBy(() -> FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.newId(),
        FeedstockMeasurementName.of("Press sample"),
        chemicalProfile(),
        futureInstant()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("feedstock measurement date cannot be in the future");
  }

  @Test
  void rejectsFutureMeasurementDateOnChange() {
    final var measurement = FeedstockMeasurement.create(
        FeedstockMeasurementId.newId(),
        FeedstockBatchId.newId(),
        FeedstockMeasurementName.of("Press sample"),
        chemicalProfile(),
        feedstockMeasurementInstant()
    );

    assertThatThrownBy(() -> measurement.changeMeasuredAt(futureInstant()))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("feedstock measurement date cannot be in the future");
  }

  private static ChemicalProfile chemicalProfile() {
    return ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.052")
        )))
        .build();
  }
}
