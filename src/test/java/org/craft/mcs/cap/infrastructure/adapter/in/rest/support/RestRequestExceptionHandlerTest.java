package org.craft.mcs.cap.infrastructure.adapter.in.rest.support;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;


class RestRequestExceptionHandlerTest {

  @Test
  void mapsInvalidUuidPathVariableToProblemDetail() {
    final var response = new RestRequestExceptionHandler().handleMethodArgumentTypeMismatch(
        new MethodArgumentTypeMismatchException(
            "fake",
            UUID.class,
            "batchId",
            null,
            new IllegalArgumentException("Invalid UUID string: fake")
        )
    );

    final var problem = response.getBody();

    assertThat(response.getStatusCode().value()).isEqualTo(400);
    assertThat(problem).isNotNull();
    assertThat(problem.getType()).hasToString("urn:problem-type:invalid-request-parameter");
    assertThat(problem.getTitle()).isEqualTo("Invalid request parameter");
    assertThat(problem.getDetail()).isEqualTo("Request parameter 'batchId' has an invalid value.");
    assertThat(problem.getProperties())
        .containsEntry("parameter", "batchId")
        .containsEntry("value", "fake")
        .containsEntry("expectedType", "UUID");
  }
}
