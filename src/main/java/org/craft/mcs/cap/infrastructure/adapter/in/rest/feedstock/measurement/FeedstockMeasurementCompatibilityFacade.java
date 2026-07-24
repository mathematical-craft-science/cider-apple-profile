package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.MeasurementSetNotFoundException;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementSetView;
import org.craft.mcs.cap.core.measurement.application.port.in.CorrectMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.CreateMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.QueryMeasurementSetsUseCase;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetCommand;
import org.craft.mcs.cap.core.measurement.application.port.in.RetractMeasurementSetUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetLifecycle;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.CreateFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.PatchFeedstockMeasurementRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.UpdateFeedstockMeasurementRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Compatibility ACL that preserves the legacy Feedstock measurement API over generic measurement use cases.
 */
@Component
@RequiredArgsConstructor
public final class FeedstockMeasurementCompatibilityFacade {

  private final @NonNull CreateMeasurementSetUseCase createMeasurementSetUseCase;
  private final @NonNull CorrectMeasurementSetUseCase correctMeasurementSetUseCase;
  private final @NonNull QueryMeasurementSetsUseCase queryMeasurementSetsUseCase;
  private final @NonNull RetractMeasurementSetUseCase retractMeasurementSetUseCase;
  private final @NonNull FeedstockMeasurementCompatibilityMapper compatibilityMapper;

  @NonNull FeedstockMeasurementResponse create(
      @NonNull UUID batchId,
      @NonNull CreateFeedstockMeasurementRequest request
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(request, "create feedstock measurement request cannot be null");
    return compatibilityMapper.toResponse(
        createMeasurementSetUseCase.create(
            compatibilityMapper.toCommand(subject(batchId), request)
        )
    );
  }

  @NonNull List<@NonNull FeedstockMeasurementResponse> list(@NonNull UUID batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return queryMeasurementSetsUseCase.list(subject(batchId)).stream()
        .filter(view -> view.lifecycle() == MeasurementSetLifecycle.ACTIVE)
        .filter(compatibilityMapper::supportsLegacyChemicalProfile)
        .map(compatibilityMapper::toResponse)
        .toList();
  }

  @NonNull FeedstockMeasurementResponse replace(
      @NonNull UUID batchId,
      @NonNull UUID measurementId,
      @NonNull UpdateFeedstockMeasurementRequest request
  ) {
    final var current = requireMeasurement(batchId, measurementId);
    return compatibilityMapper.toResponse(
        correctMeasurementSetUseCase.replace(
            compatibilityMapper.toCommand(current, request)
        )
    );
  }

  @NonNull FeedstockMeasurementResponse patch(
      @NonNull UUID batchId,
      @NonNull UUID measurementId,
      @NonNull PatchFeedstockMeasurementRequest request
  ) {
    final var current = requireMeasurement(batchId, measurementId);
    return compatibilityMapper.toResponse(
        correctMeasurementSetUseCase.patch(
            compatibilityMapper.toCommand(current, request)
        )
    );
  }

  void retract(
      @NonNull UUID batchId,
      @NonNull UUID measurementId
  ) {
    final var current = requireMeasurement(batchId, measurementId);
    retractMeasurementSetUseCase.retract(new RetractMeasurementSetCommand(
        current.measurementSetId(),
        current.version()
    ));
  }

  private @NonNull MeasurementSetView requireMeasurement(
      @NonNull UUID batchId,
      @NonNull UUID measurementId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    requireNonNull(measurementId, "measurement set id cannot be null");
    final var view = queryMeasurementSetsUseCase.get(MeasurementSetId.of(measurementId));
    final var expectedSubject = subject(batchId);
    if (!view.subject().equals(expectedSubject)
        || view.lifecycle() != MeasurementSetLifecycle.ACTIVE
        || !compatibilityMapper.supportsLegacyChemicalProfile(view)) {
      throw new MeasurementSetNotFoundException(view.measurementSetId());
    }
    return view;
  }

  private static @NonNull MeasurementSubjectReference subject(@NonNull UUID batchId) {
    return new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, batchId);
  }
}
