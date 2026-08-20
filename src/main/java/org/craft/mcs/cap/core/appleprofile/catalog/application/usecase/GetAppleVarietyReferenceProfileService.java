package org.craft.mcs.cap.core.appleprofile.catalog.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.catalog.application.AppleVarietyNotFoundException;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyReferenceProfileView;
import org.craft.mcs.cap.core.appleprofile.catalog.application.port.in.GetAppleVarietyReferenceProfileUseCase;
import org.craft.mcs.cap.core.appleprofile.catalog.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Reads a catalog aggregate and publishes only its immutable reference-profile projection. */
@Component
@RequiredArgsConstructor
public final class GetAppleVarietyReferenceProfileService
    implements GetAppleVarietyReferenceProfileUseCase {

  private final @NonNull AppleVarietyRepository appleVarietyRepository;

  @Override
  public @NonNull AppleVarietyReferenceProfileView get(@NonNull AppleVarietyId varietyId) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    return appleVarietyRepository.findById(varietyId)
        .map(GetAppleVarietyReferenceProfileService::toView)
        .orElseThrow(() -> new AppleVarietyNotFoundException(varietyId));
  }

  private static @NonNull AppleVarietyReferenceProfileView toView(@NonNull AppleVariety variety) {
    final var profile = variety.referenceProfile();
    return new AppleVarietyReferenceProfileView(
        variety.varietyId(),
        variety.name().value(),
        profile.specificGravity(),
        profile.titratableAcidity(),
        profile.ph(),
        profile.tannin()
    );
  }
}
