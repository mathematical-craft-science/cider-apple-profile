package org.craft.mcs.cap.feedstock.planning.application.composition.contract;

import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FeedstockCompositionOptimizationSettingsTest {

  @Test
  void providesFeedstockPlanningDefaults() {
    assertThat(FeedstockCompositionOptimizationRequest.Settings.defaults())
        .satisfies(settings -> {
          assertThat(settings.stepPercent()).isEqualTo(5);
          assertThat(settings.limit()).isEqualTo(10);
        });
  }

  @Test
  void acceptsSupportedBoundarySettings() {
    assertThat(new FeedstockCompositionOptimizationRequest.Settings(1, 1))
        .isNotNull();
    assertThat(new FeedstockCompositionOptimizationRequest.Settings(50, 100))
        .isNotNull();
  }

  @Test
  void rejectsUnsupportedStepPercent() {
    assertThatThrownBy(() -> new FeedstockCompositionOptimizationRequest.Settings(0, 10))
        .isInstanceOf(ApplicationValidationException.class);
    assertThatThrownBy(() -> new FeedstockCompositionOptimizationRequest.Settings(51, 10))
        .isInstanceOf(ApplicationValidationException.class);
    assertThatThrownBy(() -> new FeedstockCompositionOptimizationRequest.Settings(3, 10))
        .isInstanceOf(ApplicationValidationException.class);
  }

  @Test
  void rejectsUnsupportedCandidateLimit() {
    assertThatThrownBy(() -> new FeedstockCompositionOptimizationRequest.Settings(5, 0))
        .isInstanceOf(ApplicationValidationException.class);
    assertThatThrownBy(() -> new FeedstockCompositionOptimizationRequest.Settings(5, 101))
        .isInstanceOf(ApplicationValidationException.class);
  }
}
