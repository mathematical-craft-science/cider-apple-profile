package org.craft.mcs.cap.appleprofile.analysis.application.classification.contract;

import java.util.ArrayList;
import java.util.List;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyClassification;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppleVarietyClassificationTest {

  @Test
  void derivesBorderlineFromImmutableEvidence() {
    final var evidence = new ArrayList<AppleVarietyClassification.BorderlineEvidence>();
    evidence.add(new AppleVarietyClassification.BorderlineEvidence(
        AppleVarietyClassification.Characteristic.ACIDITY,
        AppleVarietyClassification.Reason.CLOSE_TO_BOUNDARY
    ));

    final var classification = new AppleVarietyClassification(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Kingston Black"),
        "FRENCH",
        true,
        "BITTERSHARP",
        AppleVarietyClassification.Confidence.MEDIUM,
        evidence
    );
    evidence.clear();

    assertThat(classification)
        .returns(true, AppleVarietyClassification::borderline)
        .extracting(value -> value.borderlineEvidence().size())
        .isEqualTo(1);
  }

  @Test
  void rejectsClassificationFlagThatDisagreesWithAppleClass() {
    assertThatThrownBy(() -> new AppleVarietyClassification(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Kingston Black"),
        "FRENCH",
        false,
        "BITTERSHARP",
        AppleVarietyClassification.Confidence.UNKNOWN,
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("flag");
  }

  @Test
  void rejectsBlankClassifiedAppleClass() {
    assertThatThrownBy(() -> new AppleVarietyClassification(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Kingston Black"),
        "FRENCH",
        true,
        " ",
        AppleVarietyClassification.Confidence.HIGH,
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessageContaining("class cannot be blank");
  }

  @Test
  void rejectsNullBorderlineCharacteristic() {
    assertThatNullPointerException()
        .isThrownBy(() -> new AppleVarietyClassification.BorderlineEvidence(
            null,
            AppleVarietyClassification.Reason.CLOSE_TO_BOUNDARY
        ));
  }

  @Test
  void rejectsNullBorderlineReason() {
    assertThatNullPointerException()
        .isThrownBy(() -> new AppleVarietyClassification.BorderlineEvidence(
            AppleVarietyClassification.Characteristic.ACIDITY,
            null
        ));
  }
}
