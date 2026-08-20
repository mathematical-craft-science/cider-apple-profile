package org.craft.mcs.cap.core.appleprofile.analysis.application.classification.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfileProvider;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyClassification;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyClassificationSystem;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.in.ClassifyAppleVarietyUseCase;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.out.AppleCiderClassificationAnalyzer;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for classifying an apple variety by one configured cider classification
 * system.
 *
 * <p>This use case obtains an immutable catalog profile through the AppleProfile ACL and delegates
 * classification to the consumer-owned analysis port. Provider strategies and domain results do
 * not cross the AppleProfile boundary.
 */
@Component
@RequiredArgsConstructor
public final class ClassifyAppleVarietyService
    implements ClassifyAppleVarietyUseCase {

  private final @NonNull AppleVarietyAnalysisProfileProvider profileProvider;
  private final @NonNull AppleCiderClassificationAnalyzer classificationAnalyzer;

  @Override
  public @NonNull AppleVarietyClassification classify(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyClassificationSystem system
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(system, "classification system cannot be null");

    final var variety = profileProvider.get(varietyId);

    return classificationAnalyzer.classify(variety, system);
  }
}
