package org.craft.mcs.cap.core.cideranalysis.classification.application.contract;

import java.util.List;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Published result of a named cider-apple classification system. */
public record CiderAppleClassificationView(
    @NonNull String profileCode,
    boolean classified,
    @Nullable String appleClass,
    @NonNull Confidence confidence,
    @NonNull List<@NonNull BorderlineEvidence> borderlineEvidence
) {

  public CiderAppleClassificationView {
    requireNonNull(profileCode, "classification profile code cannot be null");
    if (profileCode.isBlank()) {
      throw new ApplicationValidationException("classification profile code cannot be blank");
    }
    if (appleClass != null && appleClass.isBlank()) {
      throw new ApplicationValidationException("classified apple class cannot be blank");
    }
    requireNonNull(confidence, "classification confidence cannot be null");
    borderlineEvidence = List.copyOf(
        requireNonNull(borderlineEvidence, "classification borderline evidence cannot be null")
    );
    if (classified != (appleClass != null)) {
      throw new ApplicationValidationException("classification flag must agree with the classified apple class");
    }
    if (!classified && (confidence != Confidence.UNKNOWN || !borderlineEvidence.isEmpty())) {
      throw new ApplicationValidationException(
          "unclassified view must have unknown confidence and no borderline evidence"
      );
    }
    if (classified && confidence == Confidence.UNKNOWN) {
      throw new ApplicationValidationException("classified view cannot have unknown confidence");
    }
  }

  public boolean borderline() {
    return !borderlineEvidence.isEmpty();
  }

  public record BorderlineEvidence(
      @NonNull Characteristic characteristic,
      @NonNull BorderlineReason reason
  ) {

    public BorderlineEvidence {
      requireNonNull(characteristic, "classification characteristic cannot be null");
      requireNonNull(reason, "borderline reason cannot be null");
    }
  }

  public enum Confidence { HIGH, MEDIUM, LOW, UNKNOWN }

  public enum Characteristic { SUGAR_CONTENT, ACIDITY, TANNIN }

  public enum BorderlineReason { CLOSE_TO_BOUNDARY, RANGE_OVERLAPS_BOUNDARY, MISSING_AXIS }
}
