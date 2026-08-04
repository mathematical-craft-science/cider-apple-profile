package org.craft.mcs.cap.core.traceability.application.model;

import java.util.HashSet;
import java.util.List;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Ordered immutable material-state ancestry for one current trace subject. */
public record MaterialStateLineage(
    @NonNull BatchTraceSubject subject,
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull List<@NonNull MaterialStateLineageEntry> states
) {

  public MaterialStateLineage {
    requireNonNull(subject, "lineage subject cannot be null");
    requireNonNull(currentMaterialStateId, "lineage current material state id cannot be null");
    states = List.copyOf(requireNonNull(states, "lineage states cannot be null"));
    if (states.isEmpty() || !isCurrentStateFirst(states, currentMaterialStateId)) {
      throw new ApplicationValidationException(
          "lineage must start with its current material state at depth zero"
      );
    }
    if (!hasUniqueStates(states)) {
      throw new ApplicationValidationException("lineage cannot contain duplicate material states");
    }
    if (!isOrderedByDepth(states)) {
      throw new ApplicationValidationException("lineage states must be ordered by ascending depth");
    }
  }

  private static boolean isCurrentStateFirst(
      @NonNull List<@NonNull MaterialStateLineageEntry> states,
      @NonNull MaterialStateId currentMaterialStateId
  ) {
    final var firstState = states.getFirst();
    return firstState.depth() == 0 && firstState.materialStateId().equals(currentMaterialStateId);
  }

  private static boolean hasUniqueStates(
      @NonNull List<@NonNull MaterialStateLineageEntry> states
  ) {
    final var uniqueStateIds = new HashSet<MaterialStateId>();
    return states.stream()
        .map(MaterialStateLineageEntry::materialStateId)
        .allMatch(uniqueStateIds::add);
  }

  private static boolean isOrderedByDepth(
      @NonNull List<@NonNull MaterialStateLineageEntry> states
  ) {
    for (var index = 1; index < states.size(); index++) {
      if (states.get(index - 1).depth() > states.get(index).depth()) {
        return false;
      }
    }
    return true;
  }
}
