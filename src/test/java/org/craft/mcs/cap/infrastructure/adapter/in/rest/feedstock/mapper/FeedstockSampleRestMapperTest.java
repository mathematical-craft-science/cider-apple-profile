package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.FeedstockType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.StatisticalChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto.CreateFeedstockSampleRequest;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class FeedstockSampleRestMapperTest {

  @Test
  void mapsCreateRequestToCommandWithoutCiderMakerId() {
    final var batchId = FeedstockBatchId.newId();

    final var command = FeedstockSampleRestMapper.toCommand(new CreateFeedstockSampleRequest(
        batchId.value(),
        "Late press sample",
        FeedstockType.APPLE_JUICE,
        new ChemicalProfileRequest(
            new ExactChemicalMeasurementRequest("EXACT", new BigDecimal("1.052")),
            null,
            new StatisticalChemicalMeasurementRequest(
                "STATISTICAL",
                new BigDecimal("5.2"),
                new BigDecimal("5.1"),
                new BigDecimal("0.2"),
                3
            ),
            null,
            "lab",
            null
        ),
        Instant.parse("2026-10-15T10:00:00Z"),
        "first lab result"
    ));

    assertThat(command.batchId()).isEqualTo(batchId);
    assertThat(command.name().value()).isEqualTo("Late press sample");
    assertThat(command.type()).isEqualTo(FeedstockType.APPLE_JUICE);
    assertThat(command.note()).isEqualTo("first lab result");
    assertThat(command.chemicalProfile().sugar()).hasValueSatisfying(
        sugar -> assertThat(sugar).isInstanceOf(ExactMeasurement.class)
    );
    assertThat(command.chemicalProfile().titratableAcidity()).hasValueSatisfying(
        acidity -> assertThat(acidity).isInstanceOf(StatisticalMeasurement.class)
    );
  }
}
