package org.craft.mcs.cap.core.feedstock.batch.application.inventory.usecase;

import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.FeedstockMaterialCharacteristicsUnavailableException;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockMaterialCharacteristics;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Projects current Feedstock evidence to the characteristics required by a material consumer. */
final class FeedstockMaterialCharacteristicsProjector {

  private FeedstockMaterialCharacteristicsProjector() {
  }

  static @NonNull FeedstockMaterialCharacteristics project(
      @NonNull FeedstockCurrentProfileView profile
  ) {
    requireNonNull(profile, "feedstock current profile cannot be null");
    final var specificGravity = profile.specificGravity();
    final var titratableAcidity = profile.titratableAcidity();
    if (specificGravity == null || titratableAcidity == null) {
      throw new FeedstockMaterialCharacteristicsUnavailableException(profile.batchId());
    }
    return new FeedstockMaterialCharacteristics(
        specificGravity,
        titratableAcidity,
        profile.tannin(),
        profile.yeastAssimilableNitrogen()
    );
  }
}
