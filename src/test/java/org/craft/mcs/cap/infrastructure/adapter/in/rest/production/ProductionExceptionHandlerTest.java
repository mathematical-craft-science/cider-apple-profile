package org.craft.mcs.cap.infrastructure.adapter.in.rest.production;

import java.net.URI;
import java.util.UUID;
import org.craft.mcs.cap.core.production.application.ProductionSourceInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.production.application.ProductionSourceNotFoundException;
import org.craft.mcs.cap.core.production.application.ProductionSourceOwnershipMismatchException;
import org.craft.mcs.cap.core.production.application.ProductionSourceProfileUnavailableException;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;


class ProductionExceptionHandlerTest {

  private static final @NonNull ProductionSourceReference SOURCE =
      ProductionSourceReference.feedstockBatch(UUID.fromString("10000000-0000-0000-0000-000000000001"));

  private final ProductionExceptionHandler handler = new ProductionExceptionHandler();

  @Test
  void mapsMissingProductionSourceToContextSpecificProblem() {
    final var response = handler.handleSourceNotFound(new ProductionSourceNotFoundException(SOURCE));

    assertThat(response)
        .returns(HttpStatus.NOT_FOUND, ResponseEntity::getStatusCode)
        .extracting(responseEntity -> requireNonNull(responseEntity.getBody(), "problem detail cannot be null"))
        .satisfies(problem -> assertSourceProblem(
            problem,
            "urn:problem-type:production-source-not-found"
        ));
  }

  @Test
  void mapsUnavailableProductionSourceProfileToContextSpecificProblem() {
    final var response = handler.handleSourceProfileUnavailable(
        new ProductionSourceProfileUnavailableException(SOURCE)
    );

    assertThat(response)
        .returns(HttpStatus.UNPROCESSABLE_CONTENT, ResponseEntity::getStatusCode)
        .extracting(responseEntity -> requireNonNull(responseEntity.getBody(), "problem detail cannot be null"))
        .satisfies(problem -> assertSourceProblem(
            problem,
            "urn:problem-type:production-source-profile-unavailable"
        ));
  }

  @Test
  void mapsProductionSourceOwnershipMismatchWithWorkspaceFacts() {
    final var expectedWorkspaceId = WorkspaceId.newId();
    final var actualWorkspaceId = WorkspaceId.newId();
    final var response = handler.handleOwnershipMismatch(new ProductionSourceOwnershipMismatchException(
        SOURCE,
        expectedWorkspaceId,
        actualWorkspaceId
    ));

    assertThat(response)
        .returns(HttpStatus.FORBIDDEN, ResponseEntity::getStatusCode)
        .extracting(responseEntity -> requireNonNull(responseEntity.getBody(), "problem detail cannot be null"))
        .satisfies(problem -> assertThat(requireNonNull(problem.getProperties(), "problem properties cannot be null"))
            .containsEntry("sourceType", "FEEDSTOCK_BATCH")
            .containsEntry("sourceId", SOURCE.id().toString())
            .containsEntry("expectedWorkspaceId", expectedWorkspaceId.value().toString())
            .containsEntry("actualWorkspaceId", actualWorkspaceId.value().toString()));
  }

  @Test
  void mapsInsufficientProductionSourceVolumeToUnprocessableProblem() {
    final var response = handler.handleInsufficientVolume(
        new ProductionSourceInsufficientAvailableVolumeException(
            SOURCE,
            VolumeValue.ofLiters("100"),
            VolumeValue.ofLiters("80")
        )
    );

    assertThat(response)
        .returns(HttpStatus.UNPROCESSABLE_CONTENT, ResponseEntity::getStatusCode)
        .extracting(responseEntity -> requireNonNull(responseEntity.getBody(), "problem detail cannot be null"))
        .satisfies(problem -> assertSourceProblem(
            problem,
            "urn:problem-type:production-source-insufficient-available-volume"
        ));
  }

  private static void assertSourceProblem(
      @NonNull ProblemDetail problem,
      @NonNull String expectedType
  ) {
    assertThat(problem)
        .returns(URI.create(expectedType), ProblemDetail::getType)
        .satisfies(value -> assertThat(requireNonNull(value.getProperties(), "problem properties cannot be null"))
            .containsEntry("sourceType", "FEEDSTOCK_BATCH")
            .containsEntry("sourceId", SOURCE.id().toString()));
  }
}
