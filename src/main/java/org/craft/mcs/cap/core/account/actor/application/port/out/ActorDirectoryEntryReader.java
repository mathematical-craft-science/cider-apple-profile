package org.craft.mcs.cap.core.account.actor.application.port.out;

import java.util.Optional;
import java.util.UUID;
import org.craft.mcs.cap.core.account.actor.application.contract.ActorDisplayView;
import org.craft.mcs.cap.shared.kernel.identity.ActorType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Provider of actor display entries for one actor type. */
public interface ActorDirectoryEntryReader {

  @NonNull ActorType actorType();

  @NonNull Optional<@NonNull ActorDisplayView> findById(
      @NonNull WorkspaceId workspaceId,
      @NonNull UUID actorId
  );
}
