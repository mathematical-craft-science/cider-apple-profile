package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement;

import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.port.in.CorrectMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryMeasurementSetsUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRetractionReason;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.CreateMeasurementSetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSetListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSetResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSetRevisionHistoryResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.PatchMeasurementSetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.ReplaceMeasurementSetRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class MeasurementSetController implements MeasurementSetApi {

  private final @NonNull CreateMeasurementSetUseCase createUseCase;
  private final @NonNull QueryMeasurementSetsUseCase queryUseCase;
  private final @NonNull CorrectMeasurementSetUseCase correctUseCase;
  private final @NonNull RetractMeasurementSetUseCase retractUseCase;
  private final @NonNull MeasurementSetRestMapper restMapper;

  @Override
  @PostMapping(
      value = "/measurement-sets",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull MeasurementSetResponse> create(
      @RequestBody @NonNull CreateMeasurementSetRequest request
  ) {
    final var response = restMapper.toResponse(
        createUseCase.create(restMapper.toCommand(request))
    );
    return ResponseEntity.created(URI.create("/measurement-sets/" + response.measurementSetId()))
        .body(response);
  }

  @Override
  @GetMapping(value = "/measurement-sets/{measurementSetId}", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull MeasurementSetResponse> get(
      @PathVariable @NonNull UUID measurementSetId
  ) {
    return ResponseEntity.ok(restMapper.toResponse(
        queryUseCase.get(MeasurementSetId.of(measurementSetId))
    ));
  }

  @Override
  @GetMapping(
      value = "/measurement-sets/{measurementSetId}/revisions",
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull MeasurementSetRevisionHistoryResponse> revisions(
      @PathVariable @NonNull UUID measurementSetId
  ) {
    return ResponseEntity.ok(restMapper.toRevisionHistoryResponse(
        queryUseCase.listRevisions(MeasurementSetId.of(measurementSetId))
    ));
  }

  @Override
  @GetMapping(value = "/measurement-sets", produces = APPLICATION_JSON_VALUE)
  public @NonNull ResponseEntity<@NonNull MeasurementSetListResponse> list(
      @RequestParam @NonNull MeasurementSubjectType subjectType,
      @RequestParam @NonNull UUID subjectId
  ) {
    final var items = queryUseCase.list(new MeasurementSubjectReference(subjectType, subjectId))
        .stream()
        .map(restMapper::toResponse)
        .toList();
    return ResponseEntity.ok(new MeasurementSetListResponse(items));
  }

  @Override
  @PutMapping(
      value = "/measurement-sets/{measurementSetId}",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull MeasurementSetResponse> replace(
      @PathVariable @NonNull UUID measurementSetId,
      @RequestBody @NonNull ReplaceMeasurementSetRequest request
  ) {
    return ResponseEntity.ok(restMapper.toResponse(
        correctUseCase.replace(restMapper.toCommand(
            MeasurementSetId.of(measurementSetId),
            request
        ))
    ));
  }

  @Override
  @PatchMapping(
      value = "/measurement-sets/{measurementSetId}",
      consumes = APPLICATION_JSON_VALUE,
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull MeasurementSetResponse> patch(
      @PathVariable @NonNull UUID measurementSetId,
      @RequestBody @NonNull PatchMeasurementSetRequest request
  ) {
    return ResponseEntity.ok(restMapper.toResponse(
        correctUseCase.patch(restMapper.toCommand(
            MeasurementSetId.of(measurementSetId),
            request
        ))
    ));
  }

  @Override
  @DeleteMapping("/measurement-sets/{measurementSetId}")
  public @NonNull ResponseEntity<Void> retract(
      @PathVariable @NonNull UUID measurementSetId,
      @RequestParam long expectedVersion,
      @RequestParam @NonNull String reason
  ) {
    retractUseCase.retract(new RetractMeasurementSetCommand(
        MeasurementSetId.of(measurementSetId),
        expectedVersion,
        MeasurementRetractionReason.of(reason)
    ));
    return ResponseEntity.noContent().build();
  }
}
