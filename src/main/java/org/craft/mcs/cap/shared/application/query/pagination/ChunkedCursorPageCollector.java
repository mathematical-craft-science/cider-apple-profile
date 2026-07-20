package org.craft.mcs.cap.shared.application.query.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Collects one typed keyset page when application filtering may reject repository candidates. */
public final class ChunkedCursorPageCollector {

  private ChunkedCursorPageCollector() {
  }

  public static <P extends @NonNull Object, T extends @NonNull Object, R extends @NonNull Object>
  @NonNull CursorPage<R, P> collect(
      @NonNull CursorPageRequest<P> pageRequest,
      @NonNull CandidateFetcher<P, T> candidateFetcher,
      @NonNull CandidatePositionExtractor<P, T> positionExtractor,
      @NonNull CandidateResultMapper<T, R> resultMapper
  ) {
    requireNonNull(pageRequest, "cursor page request cannot be null");
    requireNonNull(candidateFetcher, "candidate fetcher cannot be null");
    requireNonNull(positionExtractor, "candidate position extractor cannot be null");
    requireNonNull(resultMapper, "candidate result mapper cannot be null");

    final var acceptedResults = new ArrayList<R>();
    final var acceptedPositions = new ArrayList<P>();
    final var candidateLimit = pageRequest.limit() + QueryLimits.PAGE_LOOKAHEAD;
    var afterPosition = pageRequest.after();
    var exhausted = false;

    while (acceptedResults.size() <= pageRequest.limit() && !exhausted) {
      final var candidates = candidateFetcher.fetch(afterPosition, candidateLimit);

      if (candidates.isEmpty()) {
        exhausted = true;
        continue;
      }

      for (final var candidate : candidates) {
        final var candidatePosition = positionExtractor.positionOf(candidate);
        afterPosition = candidatePosition;

        resultMapper.map(candidate).ifPresent(result -> {
          acceptedResults.add(result);
          acceptedPositions.add(candidatePosition);
        });

        if (acceptedResults.size() > pageRequest.limit()) {
          break;
        }
      }

      exhausted = candidates.size() < candidateLimit;
    }

    final var visibleResults = acceptedResults.stream()
        .limit(pageRequest.limit())
        .toList();
    final var nextPosition = acceptedResults.size() > pageRequest.limit()
        ? acceptedPositions.get(pageRequest.limit() - 1)
        : null;

    return new CursorPage<>(visibleResults, nextPosition);
  }

  @FunctionalInterface
  public interface CandidateFetcher<P extends @NonNull Object, T extends @NonNull Object> {

    @NonNull List<T> fetch(
        @Nullable P afterPosition,
        int limit
    );
  }

  @FunctionalInterface
  public interface CandidatePositionExtractor<P extends @NonNull Object, T extends @NonNull Object> {

    @NonNull P positionOf(@NonNull T candidate);
  }

  @FunctionalInterface
  public interface CandidateResultMapper<T extends @NonNull Object, R extends @NonNull Object> {

    @NonNull Optional<R> map(@NonNull T candidate);
  }
}
