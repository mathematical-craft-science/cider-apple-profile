package org.craft.mcs.cap.core.feedstock.application.detail.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.usecase.FeedstockBatchSummaryMapper;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.FeedstockBatchCurrentProfileOperationType;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.FeedstockBatchCurrentProfileOperationView;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.FeedstockBatchCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.FeedstockBatchDetailView;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.GetFeedstockBatchDetailQuery;
import org.craft.mcs.cap.core.feedstock.application.detail.port.in.GetFeedstockBatchDetailUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.ListFeedstockMeasurementsUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.in.FeedstockCurrentProfilePotentialAbvView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.usecase.FeedstockCurrentProfileAssessmentService;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Builds feedstock batch details from existing batch, measurement and analysis read-side services.
 *
 * <p>The use case keeps detail assembly out of the aggregate model: batch and measurement slices
 * remain responsible for their own facts, while this service produces one coherent current-profile
 * snapshot for API clients.
 */
@Component
@RequiredArgsConstructor
public final class GetFeedstockBatchDetailService implements GetFeedstockBatchDetailUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull ListFeedstockMeasurementsUseCase listFeedstockMeasurementsUseCase;
  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;
  private final @NonNull FermentationStartReadinessAssessor fermentationStartReadinessAssessor;
  private final @NonNull FeedstockCurrentProfileAssessmentService batchAssessmentService;

  @Override
  public @NonNull FeedstockBatchDetailView get(@NonNull GetFeedstockBatchDetailQuery query) {
    requireNonNull(query, "feedstock batch detail query cannot be null");

    final var batch = feedstockBatchRepository.findById(query.batchId())
        .orElseThrow(() -> new FeedstockBatchNotFoundException(query.batchId()));
    if (!batch.ciderMakerId().equals(query.ciderMakerId())) {
      throw new FeedstockBatchOwnershipMismatchException(
          batch.batchId(),
          query.ciderMakerId(),
          batch.ciderMakerId()
      );
    }

    final var batchSummary = FeedstockBatchSummaryMapper.toSummary(batch);
    final var measurements = listFeedstockMeasurementsUseCase.listByBatchId(batch.batchId());
    final var currentMeasurement = currentMeasurementQueryService.findCurrentMeasurement(batch.batchId())
        .orElse(null); // NOSONAR

    return new FeedstockBatchDetailView(
        batchSummary,
        toCurrentProfile(batchSummary, currentMeasurement),
        measurements
    );
  }

  private @NonNull FeedstockBatchCurrentProfileView toCurrentProfile(
      @NonNull FeedstockBatchSummary batch,
      @Nullable FeedstockMeasurement measurement
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    if (measurement == null) {
      return new FeedstockBatchCurrentProfileView(
          null,
          null,
          null,
          null,
          null,
          batchAssessmentService.planningReadinessWithoutMeasurement(batch),
          batchAssessmentService.dataQualityWithoutMeasurement(),
          null,
          null
      );
    }

    final var evaluation = sugarAcidityProfileProjector.project(measurement.chemicalProfile());
    final var freshness = batchAssessmentService.freshness(measurement);
    return new FeedstockBatchCurrentProfileView(
        toSummary(batch, measurement),
        freshness,
        evaluation,
        potentialAbv(evaluation),
        yeastAssimilableNitrogen(evaluation),
        batchAssessmentService.planningReadiness(batch, measurement.chemicalProfile()),
        batchAssessmentService.dataQuality(measurement.chemicalProfile(), evaluation, freshness),
        fermentationStartReadinessAssessor.assess(measurement.chemicalProfile()),
        new FeedstockBatchCurrentProfileOperationView(
            FeedstockBatchCurrentProfileOperationType.MEASURED,
            measurement.measuredAt()
        )
    );
  }

  private static @NonNull FeedstockMeasurementSummary toSummary(
      @NonNull FeedstockBatchSummary batch,
      @NonNull FeedstockMeasurement measurement
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    requireNonNull(measurement, "feedstock measurement cannot be null");
    return new FeedstockMeasurementSummary(
        measurement.measurementId(),
        batch.ciderMakerId(),
        measurement.batchId(),
        measurement.name(),
        measurement.measuredAt(),
        measurement.chemicalProfile(),
        measurement.note()
    );
  }

  private @Nullable FeedstockCurrentProfilePotentialAbvView potentialAbv(
      @NonNull SugarAcidityProfileEvaluation evaluation
  ) {
    final var capability = sugarAcidityProfileProjector.assessCapability(
        evaluation,
        ChemicalProfileCapability.ABV_PREDICTION
    );
    if (!capability.capable()) {
      return null;
    }

    final var sugar = evaluation.axes().valueOf(ChemicalAxis.SUGAR)
        .orElseThrow(() -> new IllegalStateException("ABV-capable profile must have sugar axis"));
    return new FeedstockCurrentProfilePotentialAbvView(
        sugar.valueKind(),
        potentialAbvPredictor.predict(SpecificGravityValue.of(sugar.value()))
    );
  }

  private static @Nullable AxisValue yeastAssimilableNitrogen(
      @NonNull SugarAcidityProfileEvaluation evaluation
  ) {
    return evaluation.axes()
        .valueOf(ChemicalAxis.YEAST_ASSIMILABLE_NITROGEN)
        .orElse(null); // NOSONAR
  }
}
