package org.craft.mcs.cap.shared.platform.query;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ChunkedCursorPageCollectorTest {

  @Test
  void fillsPageAcrossChunksAfterFilteringCandidates() {
    final var page = ChunkedCursorPageCollector.collect(
        null,
        2,
        1,
        ChunkedCursorPageCollectorTest::candidatesAfter,
        Candidate::id,
        candidate -> candidate.accepted() ? Optional.of(candidate.name()) : Optional.empty(),
        String::valueOf
    );

    assertThat(page.items()).containsExactly("two", "four");
    assertThat(page.nextCursor()).isEqualTo("4");
  }

  private static List<Candidate> candidatesAfter(
      Integer afterCursor,
      int limit
  ) {
    return Stream.of(
            new Candidate(1, "one", false),
            new Candidate(2, "two", true),
            new Candidate(3, "three", false),
            new Candidate(4, "four", true),
            new Candidate(5, "five", true)
        )
        .filter(candidate -> afterCursor == null || candidate.id() > afterCursor)
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
