package org.craft.mcs.cap.core.measurement.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MeasurementProvenanceTest {

  @Test
  void normalizesTypedExternalSourceMetadata() {
    assertThat(MeasurementSourceName.of("  CAP Lab  "))
        .returns("CAP Lab", MeasurementSourceName::value);
    assertThat(MeasurementMethod.of("  Reference method  "))
        .returns("Reference method", MeasurementMethod::value);
    assertThat(MeasurementSourceReference.of("  report:42  "))
        .returns("report:42", MeasurementSourceReference::value);
  }

  @Test
  void derivedProvenanceDoesNotDuplicateExternalSourceMetadata() {
    assertThat(MeasurementProvenance.derived())
        .returns(MeasurementSourceType.DERIVED, MeasurementProvenance::sourceType)
        .returns(null, MeasurementProvenance::sourceName)
        .returns(null, MeasurementProvenance::method)
        .returns(null, MeasurementProvenance::reference)
        .returns(null, MeasurementProvenance::confidence);
  }

  @Test
  void rejectsExternalMetadataForDerivedResult() {
    final var sourceName = MeasurementSourceName.of("Calculated result");

    assertThatThrownBy(() -> new MeasurementProvenance(
        MeasurementSourceType.DERIVED,
        sourceName,
        null,
        null,
        null
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("derived measurement provenance cannot declare external source metadata");
  }
}
