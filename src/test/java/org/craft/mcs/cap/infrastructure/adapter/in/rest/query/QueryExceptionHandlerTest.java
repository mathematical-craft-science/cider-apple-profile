package org.craft.mcs.cap.infrastructure.adapter.in.rest.query;


import org.craft.mcs.cap.shared.platform.query.MalformedCursorException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;

import static org.assertj.core.api.Assertions.assertThat;


class QueryExceptionHandlerTest {

  @Test
  void mapsMalformedCursorExceptionToProblemDetail() {
    final var response = new QueryExceptionHandler().handleMalformedCursor(
        new MalformedCursorException()
    );

    final var problem = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    assertThat(problem).isNotNull();
    assertThat(problem.getType()).hasToString("urn:problem-type:malformed-cursor");
    assertThat(problem.getTitle()).isEqualTo("Malformed cursor");
    assertThat(problem.getDetail()).contains("pagination cursor");
    assertThat(problem.getProperties()).containsEntry("parameter", "after");
  }
}
