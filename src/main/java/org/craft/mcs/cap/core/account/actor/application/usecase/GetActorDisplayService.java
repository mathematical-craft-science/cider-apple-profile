package org.craft.mcs.cap.core.account.actor.application.usecase;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.craft.mcs.cap.core.account.actor.application.ActorNotFoundException;
import org.craft.mcs.cap.core.account.actor.application.model.ActorDisplayView;
import org.craft.mcs.cap.core.account.actor.application.port.in.GetActorDisplayUseCase;
import org.craft.mcs.cap.core.account.actor.application.port.out.ActorDirectoryEntryReader;
import org.craft.mcs.cap.core.account.context.application.usecase.CurrentWorkspaceContextResolver;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.ActorType;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Resolves a human or machine actor through the reader registered for its actor type. */
@Component
public final class GetActorDisplayService implements GetActorDisplayUseCase {

  private final @NonNull CurrentWorkspaceContextResolver currentWorkspaceContextResolver;
  private final @NonNull Map<@NonNull ActorType, @NonNull ActorDirectoryEntryReader> readersByType;

  public GetActorDisplayService(
      @NonNull CurrentWorkspaceContextResolver currentWorkspaceContextResolver,
      @NonNull List<@NonNull ActorDirectoryEntryReader> readers
  ) {
    this.currentWorkspaceContextResolver = requireNonNull(
        currentWorkspaceContextResolver,
        "current workspace context resolver cannot be null"
    );
    this.readersByType = readersByType(readers);
  }

  @Override
  public @NonNull ActorDisplayView get(@NonNull ActorReference actor) {
    requireNonNull(actor, "actor reference cannot be null");
    final var workspaceId = currentWorkspaceContextResolver.resolve()
        .activeMembership()
        .workspace()
        .workspaceId();
    return java.util.Optional.ofNullable(readersByType.get(actor.type()))
        .flatMap(reader -> reader.findById(workspaceId, actor.id()))
        .orElseThrow(() -> new ActorNotFoundException(actor));
  }

  private static @NonNull Map<@NonNull ActorType, @NonNull ActorDirectoryEntryReader> readersByType(
      @NonNull List<@NonNull ActorDirectoryEntryReader> readers
  ) {
    requireNonNull(readers, "actor directory readers cannot be null");
    final Map<ActorType, ActorDirectoryEntryReader> readersByType = new EnumMap<>(ActorType.class);
    for (final var reader : readers) {
      final var verifiedReader = requireNonNull(reader, "actor directory reader cannot be null");
      final var previous = readersByType.putIfAbsent(verifiedReader.actorType(), verifiedReader);
      if (previous != null) {
        throw new ApplicationValidationException(
            "only one actor directory reader can be registered for " + verifiedReader.actorType()
        );
      }
    }
    return Map.copyOf(readersByType);
  }
}
