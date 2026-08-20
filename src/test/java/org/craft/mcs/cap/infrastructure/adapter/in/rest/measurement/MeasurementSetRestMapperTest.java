package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementResultRetractionView;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementSetRetractionView;
import org.craft.mcs.cap.core.measurement.application.contract.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementMethod;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordRole;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionChangeKind;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetRevisionNumber;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRetractionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.in.measurement.MeasurementValueInputAcl;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementRecordResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementResultRetractionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementResultRetractionStatus;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSetRetractionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSetRevisionHistoryResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSetRevisionResponse;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.craft.mcs.cap.shared.testing.MeasurementTestValues.value;
import static org.mockito.Mockito.mock;


class MeasurementSetRestMapperTest {

  private static final Instant MEASURED_AT = Instant.parse("2026-08-02T15:01:00Z");
  private static final Instant RETRACTED_AT = Instant.parse("2026-08-03T12:00:00Z");

  @Test
  void mapsCompleteRetractedResultSnapshotAndSetRetractionReason() {
    final var firstObservation = observation("1.050", MEASURED_AT.minusSeconds(60));
    final var secondObservation = observation("1.052", MEASURED_AT);
    final var result = new MeasurementRecordView(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementRecordRole.RESULT,
        PointCharacteristicValue.of(value(
            MeasurementCharacteristic.SPECIFIC_GRAVITY,
            new BigDecimal("1.051")
        )),
        MEASURED_AT,
        MeasurementProvenance.derived(),
        MeasurementDerivationMethod.ARITHMETIC_MEAN,
        List.of(firstObservation.recordId(), secondObservation.recordId())
    );
    final var sourceRevisionId = MeasurementSetRevisionId.newId();
    final var sourceRevisionNumber = MeasurementSetRevisionNumber.of(2);
    final var actor = ActorReference.accountUser(newUuid());
    final var reason = MeasurementCorrectionReason.of("Invalid hydrometer calibration");
    final var setRetractionReason = MeasurementRetractionReason.of("Invalid laboratory report");
    final var view = new MeasurementSetView(
        MeasurementSetId.newId(),
        WorkspaceId.newId(),
        new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, newUuid()),
        MeasurementSetLifecycle.RETRACTED,
        new MeasurementSetRetractionView(actor, RETRACTED_AT, setRetractionReason),
        3,
        MeasurementSetRevisionId.newId(),
        MeasurementSetRevisionNumber.of(3),
        MeasurementSetRevisionChangeKind.RETRACTION,
        MaterialStateId.newId(),
        MeasurementSetName.of("Repeated gravity reading"),
        null,
        null,
        setRetractionReason,
        RETRACTED_AT,
        actor,
        List.of(),
        List.of(new MeasurementResultRetractionView(
            sourceRevisionId,
            sourceRevisionNumber,
            result,
            List.of(firstObservation, secondObservation),
            actor,
            RETRACTED_AT,
            reason
        )),
        MEASURED_AT,
        RETRACTED_AT
    );

    final var mapper = new MeasurementSetRestMapper(mock(MeasurementValueInputAcl.class));
    final var response = mapper.toResponse(view);

    assertThat(response.retraction())
        .isNotNull()
        .returns(setRetractionReason.value(), MeasurementSetRetractionResponse::reason);
    assertThat(response.resultRetractions())
        .singleElement()
        .returns(MeasurementResultRetractionStatus.RETRACTED, MeasurementResultRetractionResponse::status)
        .returns(result.recordId().value(), retraction -> retraction.resultSnapshot().recordId())
        .returns(sourceRevisionId.value(), MeasurementResultRetractionResponse::sourceRevisionId)
        .returns(sourceRevisionNumber.value(), MeasurementResultRetractionResponse::sourceRevisionNumber)
        .satisfies(retraction -> assertThat(retraction.observationSnapshots())
            .extracting(MeasurementRecordResponse::recordId)
            .containsExactly(firstObservation.recordId().value(), secondObservation.recordId().value()));
    assertThat(mapper.toRevisionHistoryResponse(List.of(view)))
        .returns(view.measurementSetId().value(), MeasurementSetRevisionHistoryResponse::measurementSetId)
        .returns(view.lifecycle(), MeasurementSetRevisionHistoryResponse::lifecycle)
        .returns(view.version(), MeasurementSetRevisionHistoryResponse::version)
        .satisfies(history -> assertThat(history.revisions())
            .singleElement()
            .returns(view.revisionId().value(), MeasurementSetRevisionResponse::revisionId)
            .satisfies(revision -> assertThat(revision.resultRetractions()).hasSize(1)));
  }

  private static MeasurementRecordView observation(
      String measuredValue,
      Instant measuredAt
  ) {
    return new MeasurementRecordView(
        MeasurementRecordId.newId(),
        MeasurementCharacteristic.SPECIFIC_GRAVITY,
        MeasurementRecordRole.OBSERVATION,
        PointCharacteristicValue.of(value(
            MeasurementCharacteristic.SPECIFIC_GRAVITY,
            new BigDecimal(measuredValue)
        )),
        measuredAt,
        new MeasurementProvenance(
            MeasurementSourceType.INSTRUMENT,
            MeasurementSourceName.of("Hydrometer H-1"),
            MeasurementMethod.of("Hydrometer"),
            MeasurementSourceReference.of("instrument:H-1"),
            ConfidenceLevel.HIGH
        ),
        null,
        List.of()
    );
  }
}
