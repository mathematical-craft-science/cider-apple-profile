package org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record AppleVarietyClassification(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyAnalysisName varietyName,
    @NonNull String profileCode,
    boolean classified,
    @Nullable String appleClass,
    @NonNull Confidence confidence,
    @NonNull List<@NonNull BorderlineEvidence> borderlineEvidence
) {

  public AppleVarietyClassification {
    requireNonNull(varietyId, "classified apple variety id cannot be null");
    requireNonNull(varietyName, "classified apple variety name cannot be null");
    requireNonNull(profileCode, "apple classification profile code cannot be null");
    if (profileCode.isBlank()) {
      throw new ApplicationValidationException("apple classification profile code cannot be blank");
    }
    if (appleClass != null && appleClass.isBlank()) {
      throw new ApplicationValidationException("classified apple class cannot be blank");
    }
    requireNonNull(confidence, "apple classification confidence cannot be null");
    borderlineEvidence = List.copyOf(requireNonNull(
        borderlineEvidence,
        "apple classification borderline evidence cannot be null"
    ));
    if (classified != (appleClass != null)) {
      throw new ApplicationValidationException(
          "apple classification flag must agree with the classified apple class"
      );
    }
    if (!classified && (confidence != Confidence.UNKNOWN || !borderlineEvidence.isEmpty())) {
      throw new ApplicationValidationException(
          "unclassified apple variety must have unknown confidence and no borderline evidence"
      );
    }
    if (classified && confidence == Confidence.UNKNOWN) {
      throw new ApplicationValidationException("classified apple variety cannot have unknown confidence");
    }
  }

  public boolean borderline() {
    return !borderlineEvidence.isEmpty();
  }

  public record BorderlineEvidence(
      @NonNull Characteristic characteristic,
      @NonNull Reason reason
  ) {

    public BorderlineEvidence {
      requireNonNull(characteristic, "apple classification characteristic cannot be null");
      requireNonNull(reason, "apple classification borderline reason cannot be null");
    }
  }

  public enum Confidence { HIGH, MEDIUM, LOW, UNKNOWN }

  public enum Characteristic { SUGAR_CONTENT, ACIDITY, TANNIN }

  public enum Reason { CLOSE_TO_BOUNDARY, RANGE_OVERLAPS_BOUNDARY, MISSING_AXIS }
}
