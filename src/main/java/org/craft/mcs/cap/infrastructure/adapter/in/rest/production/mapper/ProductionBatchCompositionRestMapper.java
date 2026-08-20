package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CalculatedCompositionCharacteristicsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionWarning;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Maps an accepted production candidate assessment to its REST composition snapshot.
 */
public final class ProductionBatchCompositionRestMapper {

  private ProductionBatchCompositionRestMapper() {
  }

  public static @NonNull ProductionBatchCompositionResponse toResponse(
      @NonNull ProductionBatchCandidateView candidate
  ) {
    requireNonNull(candidate, "production batch candidate cannot be null");
    final var composition = candidate.sourceComposition();
    final var normalizerCode = candidate.normalizerCode();

    return new ProductionBatchCompositionResponse(
        composition.sources().stream()
            .map(ProductionBatchCompositionRestMapper::toSourceResponse)
            .toList(),
        toCharacteristicsResponse(candidate.profile()),
        PotentialAbvPredictionRestMapper.toResponse(candidate.potentialAbv()),
        new DecimalQuantityResponse(composition.totalVolume().liters(), CanonicalUnitLabels.VOLUME),
        candidate.warnings().stream()
            .map(warning -> CompositionWarning.valueOf(warning.name()))
            .toList(),
        normalizerCode == null
            ? null
            : ProfileAxisNormalizerCode.valueOf(normalizerCode.name()),
        candidate.targetDistance() == null
        ? null
        : ProfileTargetRestMapper.toResponse(candidate.targetDistance()) // NOSONAR
    );
  }

  public static @NonNull CalculatedCompositionCharacteristicsResponse toCharacteristicsResponse(
      @NonNull ProductionProfileSnapshot profile
  ) {
    requireNonNull(profile, "production profile snapshot cannot be null");
    return new CalculatedCompositionCharacteristicsResponse(
        new DecimalQuantityResponse(
            profile.specificGravity().asBigDecimal(),
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        new DecimalQuantityResponse(
            profile.titratableAcidity().asBigDecimal(),
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        ),
        profile.tannin() == null
        ? null
        : new DecimalQuantityResponse(
            profile.tannin().asBigDecimal(), // NOSONAR
            CanonicalUnitLabels.TANNIN
        ),
        profile.yeastAssimilableNitrogen() == null
        ? null
        : new DecimalQuantityResponse(
            profile.yeastAssimilableNitrogen().asBigDecimal(), // NOSONAR
            CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN
        )
    );
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

}
