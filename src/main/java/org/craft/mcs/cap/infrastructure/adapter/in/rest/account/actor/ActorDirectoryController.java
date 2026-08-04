package org.craft.mcs.cap.infrastructure.adapter.in.rest.account.actor;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.actor.application.port.in.GetActorDisplayUseCase;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.actor.dto.ActorDisplayResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.account.actor.mapper.ActorDirectoryRestMapper;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.ActorType;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/** Thin REST adapter for provider-neutral actor display resolution. */
@RestController
@RequiredArgsConstructor
public final class ActorDirectoryController implements ActorDirectoryApi {

  private final @NonNull GetActorDisplayUseCase getActorDisplayUseCase;

  @Override
  @GetMapping(
      value = "/account/actors/{actorType}/{actorId}",
      produces = APPLICATION_JSON_VALUE
  )
  public @NonNull ResponseEntity<@NonNull ActorDisplayResponse> get(
      @PathVariable @NonNull ActorType actorType,
      @PathVariable @NonNull UUID actorId
  ) {
    return ResponseEntity.ok(ActorDirectoryRestMapper.toResponse(
        getActorDisplayUseCase.get(new ActorReference(actorType, actorId))
    ));
  }
}
