package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementCharacteristicInput;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementObservationInput;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementRecordView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementResultRetractionView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetRetractionView;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.PatchMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.ReplaceMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCorrectionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetName;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.infrastructure.adapter.in.measurement.MeasurementValueInputAcl;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.CreateMeasurementSetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.DirectMeasurementCharacteristicRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.IntervalMeasurementValueRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementActorReferenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementCharacteristicRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementInputSourceType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementProvenanceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementProvenanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementRecordResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementResultRetractionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementResultRetractionStatus;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSetResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSetRetractionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSetRevisionHistoryResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSetRevisionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSubjectRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSubjectResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementValueRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.PatchMeasurementSetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.PointMeasurementValueRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.RepeatedMeasurementCharacteristicRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.ReplaceMeasurementSetRequest;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.method;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.methodValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.reference;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.referenceValue;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceName;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceNameValue;


/** Maps generic measurement transport contracts to provider-neutral application commands. */
@Component
@RequiredArgsConstructor
public final class MeasurementSetRestMapper {

  private final @NonNull MeasurementValueInputAcl valueInputAcl;

  @NonNull CreateMeasurementSetCommand toCommand(
      @NonNull CreateMeasurementSetRequest request
  ) {
    requireNonNull(request, "create measurement set request cannot be null");
    return new CreateMeasurementSetCommand(
        request.measurementSetId() == null ? null : MeasurementSetId.of(request.measurementSetId()), // NOSONAR
        toSubject(request.subject()),
        MaterialStateId.of(request.observedMaterialStateId()),
        MeasurementSetName.of(request.name()),
        request.note(),
        toInputs(request.measurements())
    );
  }

  @NonNull ReplaceMeasurementSetCommand toCommand(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull ReplaceMeasurementSetRequest request
  ) {
    return new ReplaceMeasurementSetCommand(
        measurementSetId,
        request.expectedVersion(),
        MeasurementSetName.of(request.name()),
        request.note(),
        MeasurementCorrectionReason.of(request.correctionReason()),
        toInputs(request.measurements())
    );
  }

  @NonNull PatchMeasurementSetCommand toCommand(
      @NonNull MeasurementSetId measurementSetId,
      @NonNull PatchMeasurementSetRequest request
  ) {
    return new PatchMeasurementSetCommand(
        measurementSetId,
        request.expectedVersion(),
        request.name() == null ? null : MeasurementSetName.of(request.name()), // NOSONAR
        request.noteSpecified(),
        request.note(),
        MeasurementCorrectionReason.of(request.correctionReason()),
        toInputs(request.replacements()),
        request.removals()
    );
  }

  private static @NonNull MeasurementSubjectReference toSubject(
      @NonNull MeasurementSubjectRequest request
  ) {
    requireNonNull(request, "measurement subject request cannot be null");
    return new MeasurementSubjectReference(request.type(), request.id());
  }

  @NonNull MeasurementSetResponse toResponse(@NonNull MeasurementSetView view) {
    requireNonNull(view, "measurement set view cannot be null");
    return new MeasurementSetResponse(
        view.measurementSetId().value(),
        view.workspaceId().value(),
        new MeasurementSubjectResponse(view.subject().type(), view.subject().id()),
        view.lifecycle(),
        toRetractionResponse(view.retraction()),
        view.version(),
        view.revisionId().value(),
        view.revisionNumber().value(),
        view.materialStateId().value(),
        view.name().value(),
        view.note(),
        view.correctionReason() == null ? null : view.correctionReason().value(), // NOSONAR
        view.revisionCreatedAt(),
        new MeasurementActorReferenceResponse(view.actor().type(), view.actor().id()),
        view.records().stream().map(MeasurementSetRestMapper::toRecordResponse).toList(),
        view.resultRetractions().stream()
            .map(MeasurementSetRestMapper::toResultRetractionResponse)
            .toList(),
        view.createdAt(),
        view.updatedAt()
    );
  }

  @NonNull MeasurementSetRevisionHistoryResponse toRevisionHistoryResponse(
      @NonNull List<@NonNull MeasurementSetView> revisions
  ) {
    requireNonNull(revisions, "measurement set revisions cannot be null");
    final var current = revisions.getFirst();
    return new MeasurementSetRevisionHistoryResponse(
        current.measurementSetId().value(),
        current.lifecycle(),
        toRetractionResponse(current.retraction()),
        current.version(),
        revisions.stream()
            .map(MeasurementSetRestMapper::toRevisionResponse)
            .toList()
    );
  }

  private static @NonNull MeasurementSetRevisionResponse toRevisionResponse(
      @NonNull MeasurementSetView view
  ) {
    return new MeasurementSetRevisionResponse(
        view.revisionId().value(),
        view.revisionNumber().value(),
        view.materialStateId().value(),
        view.name().value(),
        view.note(),
        view.correctionReason() == null ? null : view.correctionReason().value(), // NOSONAR
        view.revisionCreatedAt(),
        new MeasurementActorReferenceResponse(view.actor().type(), view.actor().id()),
        view.records().stream().map(MeasurementSetRestMapper::toRecordResponse).toList(),
        view.resultRetractions().stream()
            .map(MeasurementSetRestMapper::toResultRetractionResponse)
            .toList()
    );
  }

  private static @NonNull MeasurementResultRetractionResponse toResultRetractionResponse(
      @NonNull MeasurementResultRetractionView retraction
  ) {
    return new MeasurementResultRetractionResponse(
        retraction.recordId().value(),
        retraction.characteristic(),
        MeasurementResultRetractionStatus.RETRACTED,
        toRecordResponse(retraction.resultSnapshot()),
        retraction.observationSnapshots().stream()
            .map(MeasurementSetRestMapper::toRecordResponse)
            .toList(),
        retraction.sourceRevisionId().value(),
        retraction.sourceRevisionNumber().value(),
        new MeasurementActorReferenceResponse(
            retraction.actor().type(),
            retraction.actor().id()
        ),
        retraction.retractedAt(),
        retraction.reason().value()
    );
  }

  private static @Nullable MeasurementSetRetractionResponse toRetractionResponse(
      @Nullable MeasurementSetRetractionView retraction
  ) {
    return retraction == null
        ? null
        : new MeasurementSetRetractionResponse(
            new MeasurementActorReferenceResponse(
                retraction.actor().type(),
                retraction.actor().id()
            ),
            retraction.retractedAt(),
            retraction.reason().value()
        );
  }

  private static @NonNull MeasurementRecordResponse toRecordResponse(
      @NonNull MeasurementRecordView recordView
  ) {
    return new MeasurementRecordResponse(
        recordView.recordId().value(),
        recordView.characteristic(),
        recordView.role(),
        MeasurementValueRestMapper.toResponse(recordView.characteristic(), recordView.value()),
        recordView.measuredAt(),
        new MeasurementProvenanceResponse(
            recordView.provenance().sourceType(),
            sourceNameValue(recordView.provenance().sourceName()),
            methodValue(recordView.provenance().method()),
            referenceValue(recordView.provenance().reference()),
            recordView.provenance().confidence()
        ),
        recordView.derivationMethod(),
        recordView.derivationInputs().stream().map(MeasurementRecordId::value).toList()
    );
  }

  private @NonNull List<@NonNull MeasurementCharacteristicInput> toInputs(
      @NonNull List<@NonNull MeasurementCharacteristicRequest> requests
  ) {
    requireNonNull(requests, "measurement characteristic requests cannot be null");
    return requests.stream().map(this::toInput).toList();
  }

  private @NonNull MeasurementCharacteristicInput toInput(
      @NonNull MeasurementCharacteristicRequest request
  ) {
    return switch (request) {
      case DirectMeasurementCharacteristicRequest(_, var characteristic, var value, var measuredAt, var provenance) ->
          MeasurementCharacteristicInput.direct(
              characteristic,
              toInput(characteristic, value),
              measuredAt,
              toProvenance(provenance)
          );
      case RepeatedMeasurementCharacteristicRequest(
          _,
          var characteristic,
          var observations,
          var derivationMethod
      ) -> MeasurementCharacteristicInput.repeated(
          characteristic,
          observations.stream()
              .map(observation -> new MeasurementObservationInput(
                  valueInputAcl.point(
                      characteristic,
                      observation.value().value(),
                      observation.value().unit()
                  ),
                  observation.measuredAt(),
                  toProvenance(observation.provenance())
              ))
              .toList(),
          derivationMethod
      );
    };
  }

  private @NonNull CharacteristicValue<MeasuredValue> toInput(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull MeasurementValueRequest request
  ) {
    return switch (request) {
      case PointMeasurementValueRequest(_, var measuredValue, var unit) ->
          valueInputAcl.point(characteristic, measuredValue, unit);
      case IntervalMeasurementValueRequest(_, var lower, var upper, var unit) -> valueInputAcl.interval(
          characteristic,
          lower,
          upper,
          unit
      );
    };
  }

  private static @NonNull MeasurementProvenance toProvenance(
      @NonNull MeasurementProvenanceRequest request
  ) {
    return new MeasurementProvenance(
        toSourceType(request.sourceType()),
        sourceName(request.sourceName()),
        method(request.method()),
        reference(request.reference()),
        request.confidence()
    );
  }

  private static @NonNull MeasurementSourceType toSourceType(
      @NonNull MeasurementInputSourceType sourceType
  ) {
    return switch (sourceType) {
      case USER -> MeasurementSourceType.USER;
      case INSTRUMENT -> MeasurementSourceType.INSTRUMENT;
      case LAB -> MeasurementSourceType.LAB;
      case IMPORT -> MeasurementSourceType.IMPORT;
      case CURATOR -> MeasurementSourceType.CURATOR;
      case INSTITUTE -> MeasurementSourceType.INSTITUTE;
    };
  }

}
