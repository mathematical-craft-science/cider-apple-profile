package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementunits.mapper;

import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class MeasurementUnitMetadataRestMapperTest {

  @Test
  void includesConfiguredMeasurementFreshnessThreshold() {
    final var response = MeasurementUnitMetadataRestMapper.toResponse(Duration.ofDays(14));

    assertThat(response)
        .satisfies(metadata -> assertThat(metadata.units()).isNotEmpty())
        .satisfies(metadata -> assertThat(metadata.measurementFreshness().staleAfter()).isEqualTo("PT336H"));
  }
}
