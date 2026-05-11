package org.craft.mcs.cap.shared.platform.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public final class ChunkedCursorPageCollector {

  private ChunkedCursorPageCollector() {
  }

  public static <C extends @NonNull Object, T extends @NonNull Object, R extends @NonNull Object>
  @NonNull CursorPage<R> collect(
      @Nullable C initialCursor,
      int limit,
      int lookahead,
      @NonNull CandidateFetcher<C, T> candidateFetcher,
      @NonNull CandidateCursorExtractor<C, T> cursorExtractor,
      @NonNull CandidateResultMapper<T, R> resultMapper,
      @NonNull CursorEncoder<C> cursorEncoder
  ) {
    requireNonNull(candidateFetcher, "candidate fetcher cannot be null");
    requireNonNull(cursorExtractor, "candidate cursor extractor cannot be null");
    requireNonNull(resultMapper, "candidate result mapper cannot be null");
    requireNonNull(cursorEncoder, "cursor encoder cannot be null");

    if (limit < 1) {
      throw new IllegalArgumentException("page limit must be positive");
    }

    if (lookahead < 1) {
      throw new IllegalArgumentException("page lookahead must be positive");
    }

    final var acceptedResults = new ArrayList<R>();
    final var acceptedCursors = new ArrayList<C>();
    final var candidateLimit = Math.addExact(limit, lookahead);
    var afterCursor = initialCursor;
    var exhausted = false;

    while (acceptedResults.size() <= limit && !exhausted) {
      final var candidates = candidateFetcher.fetch(afterCursor, candidateLimit);

      if (candidates.isEmpty()) {
        exhausted = true;
        continue;
      }

      for (final var candidate : candidates) {
        final var candidateCursor = cursorExtractor.cursorOf(candidate);
        afterCursor = candidateCursor;

        resultMapper.map(candidate).ifPresent(result -> {
          acceptedResults.add(result);
          acceptedCursors.add(candidateCursor);
        });

        if (acceptedResults.size() > limit) {
          break;
        }
      }

      exhausted = candidates.size() < candidateLimit;
    }

    final var visibleResults = acceptedResults.stream()
        .limit(limit)
        .toList();
    final var nextCursor = acceptedResults.size() > limit
        ? cursorEncoder.encode(acceptedCursors.get(limit - 1))
        : null;

    return new CursorPage<>(visibleResults, nextCursor);
  }

  @FunctionalInterface
  public interface CandidateFetcher<C extends @NonNull Object, T extends @NonNull Object> {

    @NonNull List<T> fetch(@Nullable C afterCursor, int limit);
  }

  @FunctionalInterface
  public interface CandidateCursorExtractor<C extends @NonNull Object, T extends @NonNull Object> {

    C cursorOf(T candidate);
  }

  @FunctionalInterface
  public interface CandidateResultMapper<T extends @NonNull Object, R extends @NonNull Object> {

    @NonNull Optional<R> map(T candidate);
  }

  @FunctionalInterface
  public interface CursorEncoder<C extends @NonNull Object> {

    @NonNull String encode(C cursor);
  }
}
