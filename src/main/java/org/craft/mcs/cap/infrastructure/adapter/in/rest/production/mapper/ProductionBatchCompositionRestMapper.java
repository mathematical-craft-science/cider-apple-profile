package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import org.craft.mcs.cap.core.production.candidate.application.model.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.CalculatedSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Maps an accepted production candidate assessment to its REST composition snapshot. */
public final class ProductionBatchCompositionRestMapper {

  private ProductionBatchCompositionRestMapper() {
  }

  public static @NonNull ProductionBatchCompositionResponse toResponse(
      @NonNull ProductionBatchCandidateView candidate
  ) {
    requireNonNull(candidate, "production batch candidate cannot be null");
    final var composition = candidate.sourceComposition();

    return new ProductionBatchCompositionResponse(
        composition.sources().stream()
            .map(ProductionBatchCompositionRestMapper::toSourceResponse)
            .toList(),
        toProfilePointResponse(candidate.profile()),
        PotentialAbvPredictionRestMapper.toResponse(candidate.potentialAbv()),
        toYeastAssimilableNitrogenResponse(candidate.profile()),
        new DecimalQuantityResponse(composition.totalVolume().liters(), CanonicalUnitLabels.VOLUME),
        candidate.warnings(),
        candidate.normalizerCode(),
        candidate.targetDistance() == null
            ? null
            : ProfileTargetRestMapper.toResponse(candidate.targetDistance()) // NOSONAR
    );
  }

  public static @NonNull SugarAcidityProfilePointResponse toProfilePointResponse(
      @NonNull ProductionProfileSnapshot profile
  ) {
    requireNonNull(profile, "production profile snapshot cannot be null");
    return new SugarAcidityProfilePointResponse(
        new CalculatedSugarAcidityAxisPointResponse(profile.sugar().asBigDecimal(), CanonicalUnitLabels.SUGAR),
        new CalculatedSugarAcidityAxisPointResponse(
            profile.acidity().asBigDecimal(),
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        ),
        toTanninAxisPointResponse(profile)
    );
  }

  public static @Nullable DecimalQuantityResponse toYeastAssimilableNitrogenResponse(
      @NonNull ProductionProfileSnapshot profile
  ) {
    requireNonNull(profile, "production profile snapshot cannot be null");
    final var yan = profile.yeastAssimilableNitrogen();
    return yan == null
        ? null
        : new DecimalQuantityResponse(yan.asBigDecimal(), CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN);
  }

  private static @NonNull ProductionBatchSourceResponse toSourceResponse(
      @NonNull ProductionSourceComponent source
  ) {
    requireNonNull(source, "production source component cannot be null");
    return switch (source.reference().type()) {
      case FEEDSTOCK_BATCH -> new ProductionBatchSourceResponse(
          source.reference().id(),
          new DecimalQuantityResponse(source.volume().liters(), CanonicalUnitLabels.VOLUME)
      );
    };
  }

  private static @Nullable CalculatedSugarAcidityAxisPointResponse toTanninAxisPointResponse(
      @NonNull ProductionProfileSnapshot profile
  ) {
    final var tannin = profile.tannin();
    return tannin == null
        ? null
        : new CalculatedSugarAcidityAxisPointResponse(tannin.asBigDecimal(), CanonicalUnitLabels.TANNIN);
  }
}
