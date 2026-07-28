package org.craft.mcs.cap.core.profileanalysis.compatibility.application;

import org.craft.mcs.cap.core.profileanalysis.classification.application.compatibility.ChemicalProfileClassificationInputAdapter;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ClassificationCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.core.profileanalysis.graph.application.service.JolicoeurSugarAcidityGraphEvaluator;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.sugar.SugarCharacteristicValues;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Compatibility service for interpreting a ChemicalProfile on the sugar-acidity graph.
 *
 * <p>The projector bridges the transitional {@link ChemicalProfile} model to the typed
 * {@link JolicoeurSugarAcidityGraphEvaluator}. Optional tannin styling is explicitly interpreted through
 * the North American profile and remains outside the two-dimensional graph projection.
 */
public final class ChemicalProfileAssessmentAdapter {

  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull JolicoeurSugarAcidityGraphEvaluator graphEvaluator;
  private final @NonNull NorthAmericanProfileCalculator northAmericanProfileCalculator;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;

  public ChemicalProfileAssessmentAdapter(
      @NonNull ChemicalProfileAxisExtractor axisExtractor,
      @NonNull JolicoeurSugarAcidityGraphEvaluator graphEvaluator,
      @NonNull NorthAmericanProfileCalculator northAmericanProfileCalculator,
      @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor
  ) {
    this.axisExtractor = requireNonNull(axisExtractor, "chemical profile axis extractor cannot be null");
    this.graphEvaluator = requireNonNull(
        graphEvaluator,
        "Jolicoeur sugar-acidity graph evaluator cannot be null"
    );
    this.northAmericanProfileCalculator = requireNonNull(
        northAmericanProfileCalculator,
        "North American profile calculator cannot be null"
    );
    this.capabilityAssessor = requireNonNull(
        capabilityAssessor,
        "chemical profile capability assessor cannot be null"
    );
  }

  public @NonNull ChemicalProfileAssessment project(@NonNull ChemicalProfile profile) {
    requireNonNull(profile, "chemical profile cannot be null");

    final var axes = axisExtractor.extract(profile);
    final var specificGravity = profile.sugar()
        .map(SugarCharacteristicValues::asSpecificGravity)
        .orElse(null); // NOSONAR
    final var graphEvaluation = graphEvaluator.evaluate(
        specificGravity,
        profile.titratableAcidity().orElse(null) // NOSONAR
    );
    final var classificationInput = ChemicalProfileClassificationInputAdapter.adapt(profile);
    final var northAmericanTanninLevel = !graphEvaluation.plottable()
        || !classificationInput.has(ClassificationCharacteristic.TANNIN)
        ? null
        : northAmericanProfileCalculator.assessTannin(classificationInput);

    return new ChemicalProfileAssessment(
        axes,
        graphEvaluation,
        northAmericanTanninLevel,
        capabilityAssessor.assess(axes, ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH)
    );
  }
}
