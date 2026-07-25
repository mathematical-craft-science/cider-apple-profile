package org.craft.mcs.cap.infrastructure.adapter.in.rest.materialstate;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.port.in.ProjectCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.materialstate.dto.CurrentMaterialStateResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
public final class MaterialStateController implements MaterialStateApi {

  private final @NonNull ProjectCurrentMaterialStateUseCase projectCurrentMaterialStateUseCase;

  @Override
  @GetMapping(
      value = "/material-subjects/{subjectType}/{subjectId}/current-state",
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull CurrentMaterialStateResponse> getCurrent(
      @PathVariable @NonNull MeasurementSubjectType subjectType,
      @PathVariable @NonNull UUID subjectId
  ) {
    final var state = projectCurrentMaterialStateUseCase.project(
        new MeasurementSubjectReference(subjectType, subjectId)
    );
    return ResponseEntity.ok(CurrentMaterialStateRestMapper.toResponse(state));
  }
}
