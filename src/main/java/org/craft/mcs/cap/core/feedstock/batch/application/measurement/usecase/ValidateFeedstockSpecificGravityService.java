package org.craft.mcs.cap.core.feedstock.batch.application.measurement.usecase;

import org.craft.mcs.cap.core.feedstock.batch.application.measurement.port.in.ValidateFeedstockSpecificGravityUseCase;
import org.craft.mcs.cap.core.feedstock.batch.domain.measurement.FeedstockSpecificGravityPolicy;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/**
 * Exposes feedstock-specific SG validation without coupling generic Measurement to Feedstock.
 */
@Component
public final class ValidateFeedstockSpecificGravityService
    implements ValidateFeedstockSpecificGravityUseCase {

  private final @NonNull FeedstockSpecificGravityPolicy policy = new FeedstockSpecificGravityPolicy();

  @Override
  public void validate(@NonNull SpecificGravityValue specificGravity) {
    policy.validate(specificGravity);
  }
}
