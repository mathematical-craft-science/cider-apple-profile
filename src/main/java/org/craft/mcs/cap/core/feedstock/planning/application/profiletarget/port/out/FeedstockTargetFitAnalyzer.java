package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.out;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockContributionView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetCharacteristics;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetDistanceView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.Normalizer;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Consumer-owned port for stateless CiderAnalysis target-fit scoring. */
public interface FeedstockTargetFitAnalyzer {

  @NonNull
  Scores score(@NonNull Request request);

  record Request(
      @NonNull FeedstockProfileTargetSelection target,
      @Nullable MeasuredValueRange<TanninValue> tanninTarget,
      @NonNull FeedstockTargetFitConfiguration configuration,
      @NonNull List<@NonNull Candidate> candidates
  ) {

    public Request {
      requireNonNull(target, "feedstock target selection cannot be null");
      requireNonNull(configuration, "feedstock target configuration cannot be null");
      candidates = List.copyOf(requireNonNull(candidates, "feedstock target candidates cannot be null"));
    }
  }

  record Candidate(
      @NonNull String reference,
      @NonNull FeedstockTargetCharacteristics characteristics
  ) {

    public Candidate {
      requireNonNull(reference, "feedstock target candidate reference cannot be null");
      requireNonNull(characteristics, "feedstock target candidate characteristics cannot be null");
      if (reference.isBlank()) {
        throw new ApplicationValidationException("feedstock target candidate reference cannot be blank");
      }
    }
  }

  record Scores(
      @NonNull Normalizer normalizer,
      @NonNull List<@NonNull Score> scores
  ) {

    public Scores {
      requireNonNull(normalizer, "feedstock target normalizer cannot be null");
      scores = List.copyOf(requireNonNull(scores, "feedstock target scores cannot be null"));
    }
  }

  record Score(
      @NonNull String reference,
      @NonNull FeedstockTargetDistanceView distance,
      @NonNull FeedstockContributionView contribution
  ) {

    public Score {
      requireNonNull(reference, "feedstock target score reference cannot be null");
      requireNonNull(distance, "feedstock target distance cannot be null");
      requireNonNull(contribution, "feedstock target contribution cannot be null");
    }
  }
}
