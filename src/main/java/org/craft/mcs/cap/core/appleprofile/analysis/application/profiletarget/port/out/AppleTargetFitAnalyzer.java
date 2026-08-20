package org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.out;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleContributionView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetCharacteristics;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDefinition;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetDistanceView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract.AppleTargetFitConfiguration.Normalizer;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Consumer-owned port for Apple catalog target-fit analysis. */
public interface AppleTargetFitAnalyzer {

  @NonNull
  Scores score(@NonNull Request request);

  record Request(
      @NonNull AppleTargetDefinition target,
      @NonNull AppleTargetFitConfiguration configuration,
      @NonNull List<@NonNull Candidate> candidates
  ) {

    public Request {
      requireNonNull(target, "apple target definition cannot be null");
      requireNonNull(configuration, "apple target configuration cannot be null");
      candidates = List.copyOf(requireNonNull(candidates, "apple target candidates cannot be null"));
    }
  }

  record Candidate(
      @NonNull String reference,
      @NonNull AppleTargetCharacteristics characteristics
  ) {

    public Candidate {
      requireNonNull(reference, "apple target candidate reference cannot be null");
      requireNonNull(characteristics, "apple target candidate characteristics cannot be null");
      if (reference.isBlank()) {
        throw new ApplicationValidationException("apple target candidate reference cannot be blank");
      }
    }
  }

  record Scores(@NonNull Normalizer normalizer, @NonNull List<@NonNull Score> scores) {

    public Scores {
      requireNonNull(normalizer, "apple target normalizer cannot be null");
      scores = List.copyOf(requireNonNull(scores, "apple target scores cannot be null"));
    }
  }

  record Score(
      @NonNull String reference,
      @NonNull AppleTargetDistanceView distance,
      @NonNull AppleContributionView contribution
  ) {

    public Score {
      requireNonNull(reference, "apple target score reference cannot be null");
      requireNonNull(distance, "apple target distance cannot be null");
      requireNonNull(contribution, "apple target contribution cannot be null");
    }
  }
}
