package org.craft.mcs.cap.core.measurement.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.application.port.out.CurrentMeasurementResultQuery;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementFreshnessThresholdProvider;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementSubjectAccessControl;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceType;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.value;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class CurrentMeasurementProfileProjectorTest {

  private static final MeasurementProvenance PROVENANCE = new MeasurementProvenance(
      MeasurementSourceType.LAB,
      MeasurementSourceName.of("CAP Lab"),
      MeasurementMethod.of("Reference method"),
      MeasurementSourceReference.of("report-1"),
      null
  );

  @Test
  void partialNewMeasurementDoesNotHideOlderCharacteristics() {
    final var workspaceId = WorkspaceId.newId();
    final var subject = new MeasurementSubjectReference(
        MeasurementSubjectType.FEEDSTOCK_BATCH,
        newUuid()
    );
    final var resultQuery = mock(CurrentMeasurementResultQuery.class);
    final var accessControl = mock(MeasurementSubjectAccessControl.class);
    final var freshnessThresholdProvider = mock(MeasurementFreshnessThresholdProvider.class);
    when(accessControl.requireReadAccess(subject)).thenReturn(workspaceId);
    when(freshnessThresholdProvider.staleAfter(workspaceId)).thenReturn(Duration.ofDays(14));
    org.mockito.Mockito.doNothing()
        .when(accessControl)
        .requireReadAccess(MeasurementSubjectType.FEEDSTOCK_BATCH, workspaceId);
    when(resultQuery.findBySubjects(workspaceId, Set.of(subject))).thenReturn(List.of(
        result(subject, MeasurementCharacteristic.SPECIFIC_GRAVITY, "1.052", "2026-07-01T10:00:00Z"),
        result(subject, MeasurementCharacteristic.TITRATABLE_ACIDITY, "6.2",
            "2026-07-01T10:00:00Z"),
        result(subject, MeasurementCharacteristic.PH, "3.35", "2026-07-20T10:00:00Z")
    ));
    final var projector = new CurrentMeasurementProfileProjector(
        resultQuery,
        accessControl,
        freshnessThresholdProvider,
        Clock.fixed(Instant.parse("2026-07-21T10:00:00Z"), ZoneOffset.UTC)
    );

    final var current = projector.project(subject).orElseThrow();
    final var axes = new ChemicalProfileAxisExtractor().extract(current.chemicalProfile());

    assertThat(axes)
        .extracting(
            value -> value.requireValueOf(ChemicalAxis.SUGAR).value(),
            value -> value.requireValueOf(ChemicalAxis.ACIDITY).value()
        )
        .containsExactly(new java.math.BigDecimal("1.052"), new java.math.BigDecimal("6.2"));
    assertThat(current.chemicalProfile().ph()).isPresent();
    assertThat(current.evidence())
        .extracting(
            evidence -> evidence.characteristic(),
            evidence -> evidence.freshnessStatus()
        )
        .containsExactly(
            org.assertj.core.groups.Tuple.tuple(
                MeasurementCharacteristic.SPECIFIC_GRAVITY,
                org.craft.mcs.cap.core.measurement.application.model.MeasurementFreshnessStatus.STALE
            ),
            org.assertj.core.groups.Tuple.tuple(
                MeasurementCharacteristic.PH,
                org.craft.mcs.cap.core.measurement.application.model.MeasurementFreshnessStatus.CURRENT
            ),
            org.assertj.core.groups.Tuple.tuple(
                MeasurementCharacteristic.TITRATABLE_ACIDITY,
                org.craft.mcs.cap.core.measurement.application.model.MeasurementFreshnessStatus.STALE
            )
        );
  }

  private static CurrentMeasurementResultView result(
      MeasurementSubjectReference subject,
      MeasurementCharacteristic characteristic,
      String value,
      String measuredAt
  ) {
    return new CurrentMeasurementResultView(
        subject,
        MeasurementSetId.newId(),
        MeasurementRecordId.newId(),
        characteristic,
        PointCharacteristicValue.of(value(characteristic, new java.math.BigDecimal(value))),
        Instant.parse(measuredAt),
        PROVENANCE
    );
  }
}
