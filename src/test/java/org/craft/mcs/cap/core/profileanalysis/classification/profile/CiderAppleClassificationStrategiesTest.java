package org.craft.mcs.cap.core.profileanalysis.classification.profile;

import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.core.profileanalysis.classification.engine.ChemicalProfileClassifier;
import org.craft.mcs.cap.core.profileanalysis.classification.engine.ClassificationConfidencePolicy;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.french.FrenchCiderAppleClass;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.french.FrenchClassificationStrategy;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.longashton.LongAshton4CiderAppleClass;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.longashton.LongAshton4ClassificationStrategy;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.longashton.LongAshton7CiderAppleClass;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.longashton.LongAshton7ClassificationStrategy;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.spanish.SpanishCiderAppleClass;
import org.craft.mcs.cap.core.profileanalysis.classification.profile.spanish.SpanishClassificationStrategy;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.classification.profile.BuiltInCiderClassificationThresholds.french;
import static org.craft.mcs.cap.core.profileanalysis.classification.profile.BuiltInCiderClassificationThresholds.longAshton4;
import static org.craft.mcs.cap.core.profileanalysis.classification.profile.BuiltInCiderClassificationThresholds.longAshton7;
import static org.craft.mcs.cap.core.profileanalysis.classification.profile.BuiltInCiderClassificationThresholds.spanish;


class CiderAppleClassificationStrategiesTest {

  private final ChemicalProfileClassifier classifier = new ChemicalProfileClassifier(
      new ChemicalProfileAxisExtractor(),
      new ClassificationConfidencePolicy()
  );

  @NonNull
  private static ChemicalProfile profile(
      @NonNull String acidity,
      @NonNull String tanninity
  ) {
    return ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of(acidity)))
        .tanninity(ExactMeasurement.of(TanninityValue.of(tanninity)))
        .build();
  }

  @Test
  void classifiesLongAshton4Sweet() {
    final var result = classifier.classify(profile("4.49", "1999"),
        new LongAshton4ClassificationStrategy(longAshton4()));

    assertThat(result.classified()).isTrue();
    assertThat(result.appleClass()).isEqualTo(LongAshton4CiderAppleClass.SWEET);
  }

  @Test
  void classifiesLongAshton4Sharp() {
    final var result = classifier.classify(profile("4.50", "1999"),
        new LongAshton4ClassificationStrategy(longAshton4()));

    assertThat(result.classified()).isTrue();
    assertThat(result.appleClass()).isEqualTo(LongAshton4CiderAppleClass.SHARP);
  }

  @Test
  void classifiesLongAshton4Bittersweet() {
    final var result = classifier.classify(profile("4.49", "2000"),
        new LongAshton4ClassificationStrategy(longAshton4()));

    assertThat(result.classified()).isTrue();
    assertThat(result.appleClass()).isEqualTo(LongAshton4CiderAppleClass.BITTERSWEET);
  }

  @Test
  void classifiesLongAshton4Bittersharp() {
    final var result = classifier.classify(profile("4.50", "2000"),
        new LongAshton4ClassificationStrategy(longAshton4()));

    assertThat(result.classified()).isTrue();
    assertThat(result.appleClass()).isEqualTo(LongAshton4CiderAppleClass.BITTERSHARP);
  }

  @Test
  void classifiesLongAshton7Classes() {
    assertThat(classifier.classify(profile("4.49", "999"),
        new LongAshton7ClassificationStrategy(longAshton7())).appleClass())
        .isEqualTo(LongAshton7CiderAppleClass.SWEET);

    assertThat(classifier.classify(profile("4.49", "1000"),
        new LongAshton7ClassificationStrategy(longAshton7())).appleClass())
        .isEqualTo(LongAshton7CiderAppleClass.MILD_BITTERSWEET);

    assertThat(classifier.classify(profile("4.49", "1500"),
        new LongAshton7ClassificationStrategy(longAshton7())).appleClass())
        .isEqualTo(LongAshton7CiderAppleClass.MEDIUM_BITTERSWEET);

    assertThat(classifier.classify(profile("4.49", "2000"),
        new LongAshton7ClassificationStrategy(longAshton7())).appleClass())
        .isEqualTo(LongAshton7CiderAppleClass.FULL_BITTERSWEET);

    assertThat(classifier.classify(profile("4.50", "1999"),
        new LongAshton7ClassificationStrategy(longAshton7())).appleClass())
        .isEqualTo(LongAshton7CiderAppleClass.MEDIUM_SHARP);

    assertThat(classifier.classify(profile("4.50", "2000"),
        new LongAshton7ClassificationStrategy(longAshton7())).appleClass())
        .isEqualTo(LongAshton7CiderAppleClass.FULL_SHARP);

    assertThat(classifier.classify(profile("4.50", "3000"),
        new LongAshton7ClassificationStrategy(longAshton7())).appleClass())
        .isEqualTo(LongAshton7CiderAppleClass.BITTERSHARP);
  }

  @Test
  void classifiesFrenchClasses() {
    assertThat(classifier.classify(profile("3.99", "1999"),
        new FrenchClassificationStrategy(french())).appleClass())
        .isEqualTo(FrenchCiderAppleClass.DOUCE);

    assertThat(classifier.classify(profile("3.99", "2000"),
        new FrenchClassificationStrategy(french())).appleClass())
        .isEqualTo(FrenchCiderAppleClass.DOUCE_AMERE);

    assertThat(classifier.classify(profile("3.99", "3000"),
        new FrenchClassificationStrategy(french())).appleClass())
        .isEqualTo(FrenchCiderAppleClass.AMERE);

    assertThat(classifier.classify(profile("4.00", "1999"),
        new FrenchClassificationStrategy(french())).appleClass())
        .isEqualTo(FrenchCiderAppleClass.ACIDULEE);

    assertThat(classifier.classify(profile("6.00", "1999"),
        new FrenchClassificationStrategy(french())).appleClass())
        .isEqualTo(FrenchCiderAppleClass.AIGRE);

    assertThat(classifier.classify(profile("6.00", "3000"),
        new FrenchClassificationStrategy(french())).appleClass())
        .isEqualTo(FrenchCiderAppleClass.AIGRE_AMERE);
  }

  @Test
  void leavesFrenchGapsUnclassified() {
    assertThat(classifier.classify(profile("4.50", "2000"),
        new FrenchClassificationStrategy(french())).classified())
        .isFalse();

    assertThat(classifier.classify(profile("6.00", "2000"),
        new FrenchClassificationStrategy(french())).classified())
        .isFalse();
  }

  @Test
  void classifiesSpanishClasses() {
    assertThat(classifier.classify(profile("6.60", "1449"),
        new SpanishClassificationStrategy(spanish())).appleClass())
        .isEqualTo(SpanishCiderAppleClass.ACIDA);

    assertThat(classifier.classify(profile("4.90", "1449"),
        new SpanishClassificationStrategy(spanish())).appleClass())
        .isEqualTo(SpanishCiderAppleClass.SEMIACIDA);

    assertThat(classifier.classify(profile("4.89", "1449"),
        new SpanishClassificationStrategy(spanish())).appleClass())
        .isEqualTo(SpanishCiderAppleClass.DULCE);

    assertThat(classifier.classify(profile("4.89", "1450"),
        new SpanishClassificationStrategy(spanish())).appleClass())
        .isEqualTo(SpanishCiderAppleClass.DULCE_AMARGA);

    assertThat(classifier.classify(profile("4.89", "2000"),
        new SpanishClassificationStrategy(spanish())).appleClass())
        .isEqualTo(SpanishCiderAppleClass.AMARGA);
  }

  @Test
  void leavesSpanishGapsUnclassified() {
    assertThat(classifier.classify(profile("4.90", "1450"),
        new SpanishClassificationStrategy(spanish())).classified())
        .isFalse();

    assertThat(classifier.classify(profile("6.60", "1450"),
        new SpanishClassificationStrategy(spanish())).classified())
        .isFalse();
  }
}
