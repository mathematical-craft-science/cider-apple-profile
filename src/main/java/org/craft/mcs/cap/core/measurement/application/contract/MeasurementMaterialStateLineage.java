package org.craft.mcs.cap.core.measurement.application.contract;

import java.util.HashSet;
import java.util.List;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Measurement-owned lineage view used to select evidence applicable to one current material state. */
public record MeasurementMaterialStateLineage(
    @NonNull MeasurementSubjectReference subject,
    @NonNull MaterialStateId currentMaterialStateId,
    @NonNull List<@NonNull MeasurementMaterialStateLineageEntry> states
) {

  public MeasurementMaterialStateLineage {
    requireNonNull(subject, "measurement lineage subject cannot be null");
    requireNonNull(currentMaterialStateId, "lineage current material state id cannot be null");
    states = List.copyOf(requireNonNull(states, "measurement lineage states cannot be null"));
    if (states.isEmpty()
        || states.getFirst().depth() != 0
        || !states.getFirst().materialStateId().equals(currentMaterialStateId)) {
      throw new ApplicationValidationException(
          "measurement lineage must start with its current material state at depth zero"
      );
    }
    final var uniqueStateIds = new HashSet<MaterialStateId>();
    if (!states.stream()
        .map(MeasurementMaterialStateLineageEntry::materialStateId)
        .allMatch(uniqueStateIds::add)) {
      throw new ApplicationValidationException("measurement lineage cannot contain duplicate states");
    }
    for (var index = 1; index < states.size(); index++) {
      if (states.get(index - 1).depth() > states.get(index).depth()) {
        throw new ApplicationValidationException("measurement lineage must be ordered by depth");
      }
    }
  }
}
