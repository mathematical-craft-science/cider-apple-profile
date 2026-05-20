package org.craft.mcs.cap.infrastructure.adapter.in.rest.support;

import java.net.URI;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;


/**
 * Small REST helper for building RFC 9457 problem responses consistently across exception
 * handlers.
 */
public final class RestProblem {

  private RestProblem() {
  }

  public static @NonNull ProblemDetail create(
      int status,
      @Nullable URI type,
      @NonNull String title,
      @Nullable String detail
  ) {
    final var problem = ProblemDetail.forStatus(status);
    if (type != null) {
      problem.setType(type);
    }
    problem.setTitle(title);
    problem.setDetail(detail);

    return problem;
  }

  public static @NonNull ResponseEntity<@NonNull ProblemDetail> response(
      int status,
      @Nullable URI type,
      @NonNull String title,
      @Nullable String detail
  ) {
    return response(status, create(status, type, title, detail));
  }

  public static @NonNull ResponseEntity<@NonNull ProblemDetail> response(
      int status,
      @NonNull ProblemDetail problem
  ) {
    return ResponseEntity.status(status).body(problem);
  }
}
