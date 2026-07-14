package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ExactChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.StatisticalChemicalMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.CreateFeedstockMeasurementRequest;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.measurement.StatisticalMeasurement;
import org.junit.jupiter.api.Test;

import static org.craft.mcs.cap.shared.testing.TestClocks.feedstockMeasurementInstant;
import static org.assertj.core.api.Assertions.assertThat;


class FeedstockMeasurementRestMapperTest {

  @Test
  void mapsCreateRequestToCommandWithoutWorkspaceId() {
    final var batchId = FeedstockBatchId.newId();

    final var command = FeedstockMeasurementRestMapper.toCommand(
        batchId,
        new CreateFeedstockMeasurementRequest(
            "Late press sample",
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
                null,
                "lab",
                null
            ),
            feedstockMeasurementInstant(),
            "first lab result"
        )
    );

    assertThat(command.batchId()).isEqualTo(batchId);
    assertThat(command.name().value()).isEqualTo("Late press sample");
    assertThat(command.note()).isEqualTo("first lab result");
    assertThat(command.chemicalProfile().sugar()).hasValueSatisfying(
        sugar -> assertThat(sugar).isInstanceOf(ExactMeasurement.class)
    );
    assertThat(command.chemicalProfile().titratableAcidity()).hasValueSatisfying(
        acidity -> assertThat(acidity).isInstanceOf(StatisticalMeasurement.class)
    );
  }
}
