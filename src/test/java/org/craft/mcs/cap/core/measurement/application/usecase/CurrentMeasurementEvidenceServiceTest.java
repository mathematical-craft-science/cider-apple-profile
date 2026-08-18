package org.craft.mcs.cap.core.measurement.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementCharacteristicView;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementEvidenceView;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementFreshnessThresholdProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectCurrentStateProvider;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.testing.MeasurementTestValues;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class CurrentMeasurementEvidenceServiceTest {

  private static final Instant ASSESSED_AT = Instant.parse("2026-08-18T10:00:00Z");

  @Test
  void projectsOriginAndFreshnessAgainstTheAuthoritativeCurrentState() {
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.PRODUCTION_BATCH,
        newUuid()
    );
    final var workspaceId = WorkspaceId.newId();
    final var currentStateId = MaterialStateId.newId();
    final var inheritedStateId = MaterialStateId.newId();
    final var resultResolver = mock(CurrentMeasurementResultResolver.class);
    final var accessControl = mock(MeasurementSubjectAccessControl.class);
    final var currentStateProvider = mock(MeasurementSubjectCurrentStateProvider.class);
    final var freshnessThresholdProvider = mock(MeasurementFreshnessThresholdProvider.class);
    when(accessControl.requireReadAccess(subject)).thenReturn(workspaceId);
    when(currentStateProvider.currentStatesOf(workspaceId, Set.of(subject)))
        .thenReturn(Map.of(subject, currentStateId));
    when(freshnessThresholdProvider.staleAfter(workspaceId)).thenReturn(Duration.ofDays(14));
    when(resultResolver.resolve(workspaceId, Map.of(subject, currentStateId))).thenReturn(List.of(
        result(
            subject,
            currentStateId,
            currentStateId,
            MeasurementCharacteristic.PH,
            "3.35",
            ASSESSED_AT.minus(Duration.ofDays(1))
        ),
        result(
            subject,
            currentStateId,
            inheritedStateId,
            MeasurementCharacteristic.SPECIFIC_GRAVITY,
            "1.050",
            ASSESSED_AT.minus(Duration.ofDays(14))
        )
    ));
    final var service = new CurrentMeasurementEvidenceService(
        resultResolver,
        accessControl,
        currentStateProvider,
        freshnessThresholdProvider,
        Clock.fixed(ASSESSED_AT, ZoneOffset.UTC)
    );

    final var evidence = service.getCurrent(subject);

    assertThat(evidence)
        .returns(subject, CurrentMeasurementEvidenceView::subject)
        .returns(currentStateId, CurrentMeasurementEvidenceView::currentMaterialStateId)
        .satisfies(view -> assertThat(view.characteristics()).hasSize(2));
    assertThat(evidence.characteristic(MeasurementCharacteristic.PH))
        .get()
        .returns(MeasurementValueOrigin.MEASURED, CurrentMeasurementCharacteristicView::origin)
        .returns(MeasurementFreshnessStatus.CURRENT, CurrentMeasurementCharacteristicView::freshnessStatus);
    assertThat(evidence.characteristic(MeasurementCharacteristic.SPECIFIC_GRAVITY))
        .get()
        .returns(MeasurementValueOrigin.INHERITED, CurrentMeasurementCharacteristicView::origin)
        .returns(MeasurementFreshnessStatus.STALE, CurrentMeasurementCharacteristicView::freshnessStatus);
  }

  private static @NonNull CurrentMeasurementResultView result(
      @NonNull MeasurementSubjectReference subject,
      @NonNull MaterialStateId currentStateId,
      @NonNull MaterialStateId evidenceStateId,
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull String value,
      @NonNull Instant measuredAt
  ) {
    return new CurrentMeasurementResultView(
        subject,
        currentStateId,
        evidenceStateId,
        MeasurementSetId.newId(),
        MeasurementRecordId.newId(),
        characteristic,
        MeasurementTestValues.point(characteristic, new BigDecimal(value)),
        measuredAt,
        MeasurementProvenance.derived()
    );
  }
}
