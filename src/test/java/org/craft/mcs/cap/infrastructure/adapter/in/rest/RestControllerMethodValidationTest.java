package org.craft.mcs.cap.infrastructure.adapter.in.rest;

import jakarta.validation.Validation;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.contributor.ContributorProfileController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.invitation.WorkspaceInvitationController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.membership.WorkspaceMembershipController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.workspace.WorkspaceLifecycleController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.FeedstockBatchController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.ProductionBatchController;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.VesselController;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;


class RestControllerMethodValidationTest {

  private static final List<Class<?>> VALIDATED_CONTROLLER_TYPES = List.of(
      ContributorProfileController.class,
      WorkspaceInvitationController.class,
      WorkspaceMembershipController.class,
      WorkspaceLifecycleController.class,
      FeedstockBatchController.class,
      ProductionBatchController.class,
      VesselController.class
  );

  @Test
  void keepsMethodValidationCompatibleWithApiInterfaces() {
    try (final var validatorFactory = Validation.buildDefaultValidatorFactory()) {
      final var validator = validatorFactory.getValidator();

      assertThatCode(() -> VALIDATED_CONTROLLER_TYPES.forEach(validator::getConstraintsForClass))
          .doesNotThrowAnyException();
    }
  }
}
