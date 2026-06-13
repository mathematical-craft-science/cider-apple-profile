package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNullElse;


public final class ProfileTargetCandidateCommonRestMapper {

  private ProfileTargetCandidateCommonRestMapper() {
  }

  public static @NonNull ProfileTargetObjectiveWeights toWeights(
      @Nullable ProfileTargetObjectiveWeightsRequest request
  ) {
    if (request == null) {
      return ProfileTargetObjectiveWeights.balanced();
    }

    return new ProfileTargetObjectiveWeights(
        weightOrDefault(request.sugar()),
        weightOrDefault(request.acidity()),
        request.tannin()
    );
  }

  private static @NonNull BigDecimal weightOrDefault(@Nullable BigDecimal value) {
    return requireNonNullElse(value, BigDecimal.ONE); // NOSONAR
  }
}
