package org.craft.mcs.cap.core.profileanalysis.classification.domain.result;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Immutable outcome returned by one named cider-apple classification profile. */
public record ClassificationResult<C extends Enum<C>>(
    @NonNull String profileCode,
    @Nullable C appleClass,
    @NonNull ClassificationConfidence confidence,
    @NonNull List<@NonNull BorderlineEvidence> borderlineEvidence
) {

  public ClassificationResult {
    requireNonNull(profileCode, "classification profile code cannot be null");
    if (profileCode.isBlank()) {
      throw new DomainRuleViolationException("classification profile code cannot be blank");
    }
    requireNonNull(confidence, "classification confidence cannot be null");
    requireNonNull(borderlineEvidence, "borderline evidence cannot be null");

    borderlineEvidence = List.copyOf(borderlineEvidence);
  }

  public boolean classified() {
    return appleClass != null;
  }

  public boolean borderline() {
    return !borderlineEvidence.isEmpty();
  }

  public static <C extends Enum<C>> @NonNull ClassificationResult<C> classified(
      @NonNull String profileCode,
      @NonNull C appleClass,
      @NonNull ClassificationConfidence confidence,
      @NonNull List<@NonNull BorderlineEvidence> borderlineEvidence
  ) {
    return new ClassificationResult<>(
        profileCode,
        appleClass,
        confidence,
        borderlineEvidence
    );
  }

  public static <C extends Enum<C>> @NonNull ClassificationResult<C> unclassified(
      @NonNull String profileCode
  ) {
    return new ClassificationResult<>(
        profileCode,
        null,
        ClassificationConfidence.UNKNOWN,
        List.of()
    );
  }
}
