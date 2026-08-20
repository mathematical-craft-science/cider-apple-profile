package org.craft.mcs.cap.core.production.batch.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionBatchDetailsView;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionBatchView;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionMeasuredProfileView;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionMeasurementProfileProvider;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionPotentialAbvAnalyzer;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/** Assembles the production details read model from batch state and specialized projections. */
@Component
@RequiredArgsConstructor
public final class ProductionBatchDetailsAssembler {

  private final @NonNull ProductionMeasurementProfileProvider measurementProfileProvider;
  private final @NonNull ProductionCurrentProfileProjector currentProfileProjector;
  private final @NonNull ProductionPotentialAbvAnalyzer potentialAbvAnalyzer;
  private final @NonNull ProductionVesselReferenceResolver vesselReferenceResolver;

  public @NonNull ProductionBatchDetailsView assemble(@NonNull ProductionBatchView batch) {
    requireNonNull(batch, "production batch view cannot be null");
    return assemble(
        batch,
        vesselReferenceResolver.resolve(batch),
        measurementProfileProvider.findCurrent(batch.batchId()).orElse(null) // NOSONAR
    );
  }

  public @NonNull ProductionBatchDetailsView assemble(
      @NonNull ProductionBatchView batch,
      @NonNull VesselReferenceView vessel,
      @Nullable ProductionMeasuredProfileView measuredProfile
  ) {
    requireNonNull(batch, "production batch view cannot be null");
    requireNonNull(vessel, "production vessel reference cannot be null");
    final var currentProfile = currentProfileProjector.project(
        batch.initialProfileSnapshot(),
        measuredProfile
    );
    return new ProductionBatchDetailsView(
        batch,
        vessel,
        currentProfile,
        potentialAbvAnalyzer.predict(
            SpecificGravityValue.of(midpoint(currentProfile.specificGravity().value()))
        )
    );
  }
}
