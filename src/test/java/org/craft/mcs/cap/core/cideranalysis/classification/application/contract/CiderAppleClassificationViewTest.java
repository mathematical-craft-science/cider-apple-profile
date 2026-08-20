package org.craft.mcs.cap.core.cideranalysis.classification.application.contract;

import java.util.List;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CiderAppleClassificationViewTest {

  @Test
  void rejectsClassificationFlagThatDisagreesWithAppleClass() {
    assertThatThrownBy(() -> new CiderAppleClassificationView(
        "FRENCH",
        true,
        null,
        CiderAppleClassificationView.Confidence.HIGH,
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("classification flag must agree with the classified apple class");
  }

  @Test
  void rejectsConfidenceThatDisagreesWithClassificationState() {
    assertThatThrownBy(() -> new CiderAppleClassificationView(
        "FRENCH",
        false,
        null,
        CiderAppleClassificationView.Confidence.HIGH,
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("unclassified view must have unknown confidence and no borderline evidence");

    assertThatThrownBy(() -> new CiderAppleClassificationView(
        "FRENCH",
        true,
        "BITTERSHARP",
        CiderAppleClassificationView.Confidence.UNKNOWN,
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("classified view cannot have unknown confidence");
  }

  @Test
  void rejectsBlankClassifiedAppleClass() {
    assertThatThrownBy(() -> new CiderAppleClassificationView(
        "FRENCH",
        true,
        " ",
        CiderAppleClassificationView.Confidence.HIGH,
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("classified apple class cannot be blank");
  }
}
