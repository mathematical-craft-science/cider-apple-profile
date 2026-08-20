package org.craft.mcs.cap.account.context.application.usecase;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.craft.mcs.cap.core.account.context.application.contract.ResolvedWorkspaceContext;
import org.craft.mcs.cap.core.account.context.application.port.out.ActiveWorkspaceProvider;
import org.craft.mcs.cap.core.account.context.application.port.out.CurrentAccountWorkspaceQuery;
import org.craft.mcs.cap.core.account.context.application.port.out.CurrentWorkspaceContextRequestCache;
import org.craft.mcs.cap.core.account.context.application.usecase.ActiveWorkspaceSelectionPolicy;
import org.craft.mcs.cap.core.account.context.application.usecase.CurrentAccountMembershipReader;
import org.craft.mcs.cap.core.account.context.application.usecase.CurrentWorkspaceContextResolver;
import org.craft.mcs.cap.core.account.membership.domain.model.WorkspaceMembership;
import org.craft.mcs.cap.core.account.shared.domain.value.DisplayName;
import org.craft.mcs.cap.core.account.shared.domain.value.EmailAddress;
import org.craft.mcs.cap.core.account.shared.domain.value.IdentityProviderCode;
import org.craft.mcs.cap.core.account.user.application.port.out.AccountUserRepository;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentAccountUserRequestCache;
import org.craft.mcs.cap.core.account.user.application.port.out.CurrentActorProvider;
import org.craft.mcs.cap.core.account.user.application.usecase.CurrentAccountUserResolver;
import org.craft.mcs.cap.core.account.user.domain.model.AccountUser;
import org.craft.mcs.cap.core.account.user.domain.model.value.ExternalUserSubject;
import org.craft.mcs.cap.core.account.workspace.domain.model.Workspace;
import org.craft.mcs.cap.core.account.workspace.domain.model.WorkspaceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.account.support.WorkspaceMembershipSnapshotFixtures.snapshot;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrentWorkspaceContextResolverTest {

  @Test
  void resolvesWorkspaceContextOncePerRequest() {
    final var user = AccountUser.create(
        IdentityProviderCode.CLERK,
        ExternalUserSubject.of("user_123"),
        EmailAddress.of("technologist@example.com"),
        DisplayName.of("CAP Technologist"),
        null
    );
    final var workspace = Workspace.create(
        DisplayName.of("My cidery"),
        WorkspaceType.CIDERY
    );
    final var membership = WorkspaceMembership.createOwner(workspace.workspaceId(), user.userId());
    final var userCache = mock(CurrentAccountUserRequestCache.class);
    final var workspaceQuery = mock(CurrentAccountWorkspaceQuery.class);
    final var activeWorkspaceProvider = mock(ActiveWorkspaceProvider.class);
    final var workspaceContextCache = mock(CurrentWorkspaceContextRequestCache.class);
    final var rememberedContext = new AtomicReference<ResolvedWorkspaceContext>();

    when(userCache.current()).thenReturn(Optional.of(user));
    when(workspaceQuery.findActiveByUserId(user.userId()))
        .thenReturn(List.of(snapshot(membership, workspace)));
    when(activeWorkspaceProvider.selectedWorkspaceId()).thenReturn(Optional.of(workspace.workspaceId()));
    when(workspaceContextCache.current())
        .thenAnswer(_ -> Optional.ofNullable(rememberedContext.get()));
    doAnswer(invocation -> {
      rememberedContext.set(invocation.getArgument(0, ResolvedWorkspaceContext.class));
      return null;
    }).when(workspaceContextCache).remember(any());

    final var resolver = new CurrentWorkspaceContextResolver(
        new CurrentAccountUserResolver(
            mock(CurrentActorProvider.class),
            mock(AccountUserRepository.class),
            userCache
        ),
        new CurrentAccountMembershipReader(workspaceQuery),
        activeWorkspaceProvider,
        new ActiveWorkspaceSelectionPolicy(),
        workspaceContextCache
    );

    final var first = resolver.resolve();
    final var second = resolver.resolve();

    assertThat(second).isSameAs(first);
    verify(workspaceQuery, times(1)).findActiveByUserId(user.userId());
    verify(activeWorkspaceProvider, times(1)).selectedWorkspaceId();
  }
}
