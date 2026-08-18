package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementevidence;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryCurrentMeasurementEvidenceUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementevidence.dto.CurrentMeasurementEvidenceResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/** Exposes the Measurement-owned current-evidence query without material-owner semantics. */
@RestController
@RequiredArgsConstructor
public final class MeasurementEvidenceController implements MeasurementEvidenceApi {

  private final @NonNull QueryCurrentMeasurementEvidenceUseCase measurementEvidenceQuery;

  @Override
  @GetMapping(
      value = "/measurement-subjects/{subjectType}/{subjectId}/current-evidence",
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull CurrentMeasurementEvidenceResponse> getCurrentEvidence(
      @PathVariable @NonNull MeasurementSubjectType subjectType,
      @PathVariable @NonNull UUID subjectId
  ) {
    final var evidence = measurementEvidenceQuery.getCurrent(
        new MeasurementSubjectReference(subjectType, subjectId)
    );
    return ResponseEntity.ok(CurrentMeasurementEvidenceRestMapper.toResponse(evidence));
  }
}
