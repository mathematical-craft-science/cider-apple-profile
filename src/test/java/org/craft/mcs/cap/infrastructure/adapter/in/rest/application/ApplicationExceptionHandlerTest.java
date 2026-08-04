package org.craft.mcs.cap.infrastructure.adapter.in.rest.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationConflictException;
import org.craft.mcs.cap.shared.application.ApplicationForbiddenException;
import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.craft.mcs.cap.shared.application.ApplicationUnprocessableException;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ApplicationExceptionHandlerTest {

  private final @NonNull ApplicationExceptionHandler handler = new ApplicationExceptionHandler();

  @Test
  void mapsApplicationNotFoundToNotFoundProblem() {
    final var response = handler.handleNotFound(new TestNotFoundException("missing resource"));

    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getType()).hasToString("urn:problem-type:application-resource-not-found");
    assertThat(response.getBody().getTitle()).isEqualTo("Application resource not found");
    assertThat(response.getBody().getDetail()).isEqualTo("missing resource");
  }

  @Test
  void mapsApplicationForbiddenToForbiddenProblem() {
    final var response = handler.handleForbidden(new TestForbiddenException("not your batch"));

    assertThat(response.getStatusCode().value()).isEqualTo(403);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getType()).hasToString("urn:problem-type:application-forbidden");
    assertThat(response.getBody().getTitle()).isEqualTo("Application action is forbidden");
  }

  @Test
  void mapsConcurrentResourceModificationToConflictProblem() {
    final var response = handler.handleConcurrentResourceModification(
        new ConcurrentResourceModificationException("feedstock batch", null)
    );

    assertThat(response.getStatusCode().value()).isEqualTo(409);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getType()).hasToString("urn:problem-type:concurrent-resource-modification");
    assertThat(response.getBody().getTitle()).isEqualTo("Resource was modified concurrently");
    assertThat(response.getBody().getProperties()).containsEntry("resource", "feedstock batch");
  }

  @Test
  void mapsApplicationConflictToConflictProblem() {
    final var response = handler.handleConflict(new TestConflictException("state changed"));

    assertThat(response.getStatusCode().value()).isEqualTo(409);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getType()).hasToString("urn:problem-type:application-conflict");
    assertThat(response.getBody().getDetail()).isEqualTo("state changed");
  }

  @Test
  void mapsApplicationUnprocessableToUnprocessableProblem() {
    final var response = handler.handleUnprocessable(new TestUnprocessableException("profile is incomplete"));

    assertThat(response.getStatusCode().value()).isEqualTo(422);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getType()).hasToString("urn:problem-type:application-unprocessable");
    assertThat(response.getBody().getDetail()).isEqualTo("profile is incomplete");
  }

  @Test
  void mapsApplicationValidationToBadRequestProblem() {
    final var response = handler.handleValidation(new ApplicationValidationException("limit must be positive"));

    assertThat(response.getStatusCode().value()).isEqualTo(400);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getType()).hasToString("urn:problem-type:application-validation-failed");
    assertThat(response.getBody().getTitle()).isEqualTo("Application request validation failed");
  }

  private static final class TestNotFoundException extends ApplicationNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    private TestNotFoundException(@NonNull String message) {
      super(message);
    }
  }

  private static final class TestForbiddenException extends ApplicationForbiddenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private TestForbiddenException(@NonNull String message) {
      super(message);
    }
  }

  private static final class TestConflictException extends ApplicationConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    private TestConflictException(@NonNull String message) {
      super(message);
    }
  }

  private static final class TestUnprocessableException extends ApplicationUnprocessableException {

    @Serial
    private static final long serialVersionUID = 1L;

    private TestUnprocessableException(@NonNull String message) {
      super(message);
    }
  }

}
