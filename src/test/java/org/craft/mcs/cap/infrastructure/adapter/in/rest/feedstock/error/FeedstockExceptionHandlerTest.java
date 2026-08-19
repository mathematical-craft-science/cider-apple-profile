package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.error;


import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.InsufficientFeedstockCompositionSourcesException;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;

import static org.assertj.core.api.Assertions.assertThat;


class FeedstockExceptionHandlerTest {

  @Test
  void mapsFeedstockBatchNotFoundExceptionToProblemDetail() {
    final var batchId = FeedstockBatchId.of("00000000-0000-0000-0000-000000000001");
    final var response = new FeedstockExceptionHandler().handleFeedstockBatchNotFound(
        new FeedstockBatchNotFoundException(batchId)
    );

    final var problem = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));
    assertThat(problem).isNotNull();
    assertThat(problem.getType()).hasToString("urn:problem-type:feedstock-batch-not-found");
    assertThat(problem.getTitle()).isEqualTo("Feedstock batch not found");
    assertThat(problem.getDetail()).contains(batchId.value().toString());
    assertThat(problem.getProperties()).containsEntry("batchId", batchId.value().toString());
  }

  @Test
  void mapsInsufficientFeedstockCompositionSourcesExceptionToProblemDetail() {
    final var response = new FeedstockExceptionHandler().handleInsufficientFeedstockCompositionSources(
        new InsufficientFeedstockCompositionSourcesException(1, 2)
    );

    final var problem = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(422));
    assertThat(problem).isNotNull();
    assertThat(problem.getType()).hasToString("urn:problem-type:insufficient-feedstock-composition-sources");
    assertThat(problem.getTitle()).isEqualTo("Insufficient feedstock composition sources");
    assertThat(problem.getDetail()).contains("at least 2 usable current batch profiles");
    assertThat(problem.getProperties())
        .containsEntry("sourceCount", 1)
        .containsEntry("minimumSourceCount", 2);
  }
}
