package org.craft.mcs.cap.shared.application.query;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record TextSearchCriteria(@NonNull String query) {

  public TextSearchCriteria {
    requireNonNull(query, "text search query cannot be null");
    query = query.strip();

    if (query.isBlank()) {
      throw new ApplicationValidationException("text search query cannot be blank");
    }
  }
}
