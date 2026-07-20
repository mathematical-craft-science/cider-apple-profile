package org.craft.mcs.cap.shared.application.query.pagination;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ChunkedCursorPageCollectorTest {

  @Test
  void fillsPageAcrossChunksAfterFilteringCandidates() {
    final var page = ChunkedCursorPageCollector.collect(
        CursorPageRequest.firstPage(2),
        ChunkedCursorPageCollectorTest::candidatesAfter,
        Candidate::id,
        candidate -> candidate.accepted() ? Optional.of(candidate.name()) : Optional.empty()
    );

    assertThat(page)
        .returns(List.of("two", "four"), CursorPage::items)
        .returns(4, CursorPage::nextPosition);
  }

  private static List<Candidate> candidatesAfter(
      Integer afterPosition,
      int limit
  ) {
    return Stream.of(
            new Candidate(1, "one", false),
            new Candidate(2, "two", true),
            new Candidate(3, "three", false),
            new Candidate(4, "four", true),
            new Candidate(5, "five", true)
        )
        .filter(candidate -> afterPosition == null || candidate.id() > afterPosition)
        .limit(limit)
        .toList();
  }

  private record Candidate(
      Integer id,
      String name,
      boolean accepted
  ) {
  }
}
