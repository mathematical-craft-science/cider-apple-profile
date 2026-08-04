package org.craft.mcs.cap.feedstock.application.currentprofile.model;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockLatestMeasurementSetView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FeedstockLatestMeasurementSetViewTest {

  private static final Instant MEASURED_AT = Instant.parse("2026-07-29T10:00:00Z");

  @Test
  void normalizesValidMeasurementSetMetadata() {
    final var view = new FeedstockLatestMeasurementSetView(
        UuidV7Generator.newUuid(),
        "  Current laboratory result  ",
        MEASURED_AT,
        2,
        null
    );

    assertThat(view)
        .returns("Current laboratory result", FeedstockLatestMeasurementSetView::name)
        .returns(MEASURED_AT, FeedstockLatestMeasurementSetView::measuredAt)
        .returns(2L, FeedstockLatestMeasurementSetView::version);
  }

  @Test
  void acceptsMaximumNameLengthAndZeroVersion() {
    final var view = new FeedstockLatestMeasurementSetView(
        UuidV7Generator.newUuid(),
        "a".repeat(160),
        MEASURED_AT,
        0,
        null
    );

    assertThat(view)
        .returns(160, item -> item.name().length())
        .returns(0L, FeedstockLatestMeasurementSetView::version);
  }

  @Test
  void rejectsBlankOrOversizedName() {
    final var blankNameMeasurementSetId = UuidV7Generator.newUuid();
    assertThatThrownBy(() -> new FeedstockLatestMeasurementSetView(
        blankNameMeasurementSetId,
        " ",
        MEASURED_AT,
        0,
        null
    )).isInstanceOf(ApplicationValidationException.class);

    final var oversizedNameMeasurementSetId = UuidV7Generator.newUuid();
    final var oversizedName = "a".repeat(161);
    assertThatThrownBy(() -> new FeedstockLatestMeasurementSetView(
        oversizedNameMeasurementSetId,
        oversizedName,
        MEASURED_AT,
        0,
        null
    )).isInstanceOf(ApplicationValidationException.class);
  }

  @Test
  void rejectsNegativeVersion() {
    final var measurementSetId = UuidV7Generator.newUuid();
    assertThatThrownBy(() -> new FeedstockLatestMeasurementSetView(
        measurementSetId,
        "Current laboratory result",
        MEASURED_AT,
        -1,
        null
    )).isInstanceOf(ApplicationValidationException.class);
  }
}
