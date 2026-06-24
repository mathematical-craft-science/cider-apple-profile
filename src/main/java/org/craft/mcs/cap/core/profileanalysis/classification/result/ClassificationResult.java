package org.craft.mcs.cap.core.profileanalysis.classification.result;

import java.util.List;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public record ClassificationResult<C extends Enum<C>>(
    @NonNull String profileCode,
    @Nullable C appleClass,
    boolean classified,
    @NonNull ClassificationConfidence confidence,
    boolean borderline,
    @NonNull List<@NonNull BorderlineEvidence> borderlineEvidence,
    @NonNull ChemicalProfileAxes axes
) {

  public ClassificationResult {
    requireNonNull(profileCode, "classification profile code cannot be null");
    requireNonNull(confidence, "classification confidence cannot be null");
    requireNonNull(borderlineEvidence, "borderline evidence cannot be null");
    requireNonNull(axes, "chemical profile axes cannot be null");

    borderlineEvidence = List.copyOf(borderlineEvidence);
  }

  public static <C extends Enum<C>> @NonNull ClassificationResult<C> classified(
      @NonNull String profileCode,
      @NonNull C appleClass,
      @NonNull ClassificationConfidence confidence,
      boolean borderline,
      @NonNull List<@NonNull BorderlineEvidence> borderlineEvidence,
      @NonNull ChemicalProfileAxes axes
  ) {
    return new ClassificationResult<>(
        profileCode,
        appleClass,
        true,
        confidence,
        borderline,
        borderlineEvidence,
        axes
    );
  }

  public static <C extends Enum<C>> @NonNull ClassificationResult<C> unclassified(
      @NonNull String profileCode,
      @NonNull ChemicalProfileAxes axes
  ) {
    return new ClassificationResult<>(
        profileCode,
        null,
        false,
        ClassificationConfidence.UNKNOWN,
        true,
        List.of(),
        axes
    );
  }
}
