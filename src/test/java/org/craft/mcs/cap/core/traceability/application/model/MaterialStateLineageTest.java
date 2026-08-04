package org.craft.mcs.cap.core.traceability.application.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MaterialStateLineageTest {

  @Test
  void preservesOrderedUniqueAncestry() {
    final var currentStateId = MaterialStateId.newId();
    final var previousStateId = MaterialStateId.newId();
    final var subject = BatchTraceSubject.of(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        UUID.fromString("019faecd-034f-7000-8000-000000000001")
    );
    final var expectedStates = List.of(
        entry(currentStateId, 0),
        new MaterialStateLineageEntry(
            previousStateId,
            1,
            Set.of(MeasurementCharacteristic.PH)
        )
    );
    final var lineage = new MaterialStateLineage(
        subject,
        currentStateId,
        expectedStates
    );

    assertThat(lineage)
        .returns(subject, MaterialStateLineage::subject)
        .returns(currentStateId, MaterialStateLineage::currentMaterialStateId)
        .returns(expectedStates, MaterialStateLineage::states);
  }

  @Test
  void requiresCurrentStateAtDepthZero() {
    final var currentStateId = MaterialStateId.newId();

    assertThatThrownBy(() -> new MaterialStateLineage(
        BatchTraceSubject.of(
            BatchTraceSubjectType.PRODUCTION_BATCH,
            UUID.fromString("019faecd-034f-7000-8000-000000000002")
        ),
        currentStateId,
        List.of(entry(currentStateId, 1))
    )).isInstanceOf(ApplicationValidationException.class);
  }

  @Test
  void rejectsDuplicateOrUnorderedStates() {
    final var currentStateId = MaterialStateId.newId();
    final var previousStateId = MaterialStateId.newId();
    final var subject = BatchTraceSubject.of(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        UUID.fromString("019faecd-034f-7000-8000-000000000003")
    );

    assertThatThrownBy(() -> new MaterialStateLineage(
        subject,
        currentStateId,
        List.of(entry(currentStateId, 0), entry(currentStateId, 1))
    )).isInstanceOf(ApplicationValidationException.class);
    assertThatThrownBy(() -> new MaterialStateLineage(
        subject,
        currentStateId,
        List.of(
            entry(currentStateId, 0),
            entry(previousStateId, 2),
            entry(MaterialStateId.newId(), 1)
        )
    )).isInstanceOf(ApplicationValidationException.class);
  }

  private static MaterialStateLineageEntry entry(
      MaterialStateId materialStateId,
      int depth
  ) {
    return new MaterialStateLineageEntry(materialStateId, depth, Set.of());
  }
}
