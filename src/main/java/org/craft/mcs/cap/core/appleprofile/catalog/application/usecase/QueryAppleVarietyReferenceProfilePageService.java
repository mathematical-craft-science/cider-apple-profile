package org.craft.mcs.cap.core.appleprofile.catalog.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyReferenceProfilePageQuery;
import org.craft.mcs.cap.core.appleprofile.catalog.application.contract.AppleVarietyReferenceProfileView;
import org.craft.mcs.cap.core.appleprofile.catalog.application.port.in.QueryAppleVarietyReferenceProfilePageUseCase;
import org.craft.mcs.cap.core.appleprofile.catalog.application.port.out.AppleVarietyReferenceProfilePageProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Publishes ordered catalog reference profiles without exposing persistence query ports. */
@Component
@RequiredArgsConstructor
public final class QueryAppleVarietyReferenceProfilePageService
    implements QueryAppleVarietyReferenceProfilePageUseCase {

  private final @NonNull AppleVarietyReferenceProfilePageProvider pageProvider;

  @Override
  public @NonNull List<@NonNull AppleVarietyReferenceProfileView> query(
      @NonNull AppleVarietyReferenceProfilePageQuery query
  ) {
    return pageProvider.findProfiles(requireNonNull(query, "reference profile page query cannot be null"));
  }
}
