package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.profiletarget.common.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.ObjectiveWeights;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNullElse;


public final class ProfileTargetCandidateCommonRestMapper {

  private ProfileTargetCandidateCommonRestMapper() {
  }

  public static @NonNull ObjectiveWeights toFeedstockWeights(
      @Nullable ProfileTargetObjectiveWeightsRequest request
  ) {
    if (request == null) {
      return FeedstockTargetFitConfiguration.ObjectiveWeights.balanced();
    }
    return new FeedstockTargetFitConfiguration.ObjectiveWeights(
        weightOrDefault(request.specificGravity()),
        weightOrDefault(request.titratableAcidity()),
        request.tannin()
    );
  }

  public static AppleTargetFitConfiguration.@NonNull ObjectiveWeights toAppleWeights(
      @Nullable ProfileTargetObjectiveWeightsRequest request
  ) {
    if (request == null) {
      return AppleTargetFitConfiguration.ObjectiveWeights.balanced();
    }
    return new AppleTargetFitConfiguration.ObjectiveWeights(
        weightOrDefault(request.specificGravity()),
        weightOrDefault(request.titratableAcidity()),
        request.tannin()
    );
  }

  private static @NonNull BigDecimal weightOrDefault(@Nullable BigDecimal value) {
    return requireNonNullElse(value, BigDecimal.ONE); // NOSONAR
  }
}
