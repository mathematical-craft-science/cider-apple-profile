package org.craft.mcs.cap.account.actor.application.usecase;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.account.actor.application.ActorNotFoundException;
import org.craft.mcs.cap.core.account.actor.application.contract.ActorDisplayView;
import org.craft.mcs.cap.core.account.actor.application.contract.ActorLifecycleStatus;
import org.craft.mcs.cap.core.account.actor.application.port.out.ActorDirectoryEntryReader;
import org.craft.mcs.cap.core.account.actor.application.usecase.GetActorDisplayService;
import org.craft.mcs.cap.core.account.context.application.contract.ResolvedWorkspaceContext;
import org.craft.mcs.cap.core.account.context.application.port.in.WorkspaceMembershipView;
import org.craft.mcs.cap.core.account.context.application.usecase.CurrentWorkspaceContextResolver;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.workspace.application.port.in.WorkspaceView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.craft.mcs.cap.shared.kernel.identity.ActorType;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.shared.kernel.identity.UuidV7Generator.newUuid;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class GetActorDisplayServiceTest {

  @Test
  void resolvesActorThroughReaderRegisteredForItsType() {
    final var accountReader = mock(ActorDirectoryEntryReader.class);
    final var machineReader = mock(ActorDirectoryEntryReader.class);
    final var workspaceId = WorkspaceId.newId();
    final var contextResolver = contextResolver(workspaceId);
    final var actor = ActorReference.accountUser(newUuid());
    final var expected = new ActorDisplayView(
        actor,
        DisplayName.of("CAP user"),
        null,
        ActorLifecycleStatus.ACTIVE
    );
    when(accountReader.actorType()).thenReturn(ActorType.ACCOUNT_USER);
    when(machineReader.actorType()).thenReturn(ActorType.MACHINE_IDENTITY);
    when(accountReader.findById(workspaceId, actor.id())).thenReturn(Optional.of(expected));
    final var service = new GetActorDisplayService(
        contextResolver,
        List.of(machineReader, accountReader)
    );

    assertThat(service.get(actor)).isEqualTo(expected);
    verify(accountReader).findById(workspaceId, actor.id());
  }

  @Test
  void reportsUnknownActorWhenNoMatchingDirectoryEntryExists() {
    final var accountReader = mock(ActorDirectoryEntryReader.class);
    final var actor = ActorReference.machineIdentity(newUuid());
    when(accountReader.actorType()).thenReturn(ActorType.ACCOUNT_USER);
    final var service = new GetActorDisplayService(
        contextResolver(WorkspaceId.newId()),
        List.of(accountReader)
    );

    assertThatThrownBy(() -> service.get(actor))
        .isInstanceOf(ActorNotFoundException.class)
        .hasMessage("CAP actor not found: MACHINE_IDENTITY/" + actor.id());
  }

  @Test
  void rejectsAmbiguousReadersForOneActorType() {
    final var firstReader = mock(ActorDirectoryEntryReader.class);
    final var secondReader = mock(ActorDirectoryEntryReader.class);
    when(firstReader.actorType()).thenReturn(ActorType.ACCOUNT_USER);
    when(secondReader.actorType()).thenReturn(ActorType.ACCOUNT_USER);

    assertThatThrownBy(() -> new GetActorDisplayService(
        contextResolver(WorkspaceId.newId()),
        List.of(firstReader, secondReader)
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("only one actor directory reader can be registered for ACCOUNT_USER");
  }

  private static CurrentWorkspaceContextResolver contextResolver(WorkspaceId workspaceId) {
    final var resolver = mock(CurrentWorkspaceContextResolver.class);
    final var context = mock(ResolvedWorkspaceContext.class);
    final var membership = mock(WorkspaceMembershipView.class);
    final var workspace = mock(WorkspaceView.class);
    when(resolver.resolve()).thenReturn(context);
    when(context.activeMembership()).thenReturn(membership);
    when(membership.workspace()).thenReturn(workspace);
    when(workspace.workspaceId()).thenReturn(workspaceId);
    return resolver;
  }
}
