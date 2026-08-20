package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper;

import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetCharacteristics;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetCharacteristics;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.mapper.ProfileCharacteristicRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetScoringCharacteristicsResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps typed profile-target scoring characteristics without graph or target-objective concerns. */
public final class ProfileTargetScoringCharacteristicsRestMapper {

  private ProfileTargetScoringCharacteristicsRestMapper() {
  }

  public static @NonNull ProfileTargetScoringCharacteristicsResponse toResponse(
      @NonNull FeedstockTargetCharacteristics input
  ) {
    requireNonNull(input, "feedstock target characteristics cannot be null");
    final var tannin = input.tannin();
    return new ProfileTargetScoringCharacteristicsResponse(
        ProfileCharacteristicRestMapper.toResponse(
            input.specificGravity(),
            SpecificGravityValue::asBigDecimal,
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        ProfileCharacteristicRestMapper.toResponse(
            input.titratableAcidity(),
            TitratableAcidityValue::asBigDecimal,
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        ),
        tannin == null
            ? null
            : ProfileCharacteristicRestMapper.toResponse(
                tannin,
                TanninValue::asBigDecimal,
                CanonicalUnitLabels.TANNIN
            )
    );
  }

  public static @NonNull ProfileTargetScoringCharacteristicsResponse toResponse(
      @NonNull AppleTargetCharacteristics input
  ) {
    requireNonNull(input, "apple target characteristics cannot be null");
    final var tannin = input.tannin();
    return new ProfileTargetScoringCharacteristicsResponse(
        ProfileCharacteristicRestMapper.toResponse(
            input.specificGravity(),
            SpecificGravityValue::asBigDecimal,
            CanonicalUnitLabels.SPECIFIC_GRAVITY
        ),
        ProfileCharacteristicRestMapper.toResponse(
            input.titratableAcidity(),
            TitratableAcidityValue::asBigDecimal,
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        ),
        tannin == null
            ? null
            : ProfileCharacteristicRestMapper.toResponse(
                tannin,
                TanninValue::asBigDecimal,
                CanonicalUnitLabels.TANNIN
            )
    );
  }
}
