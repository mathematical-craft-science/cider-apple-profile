package org.craft.mcs.cap.infrastructure.adapter.out.measurement;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.measurement.application.port.out.MeasurementActorProvider;
import org.craft.mcs.cap.shared.kernel.identity.ActorReference;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** Maps the authenticated CAP identity to a stable provider-neutral measurement audit reference. */
@Component
@RequiredArgsConstructor
final class CurrentAccountMeasurementActorProvider implements MeasurementActorProvider {

  private final @NonNull GetCurrentAccountProfileUseCase currentAccountProfileUseCase;

  @Override
  public @NonNull ActorReference requireCurrentActorReference() {
    return ActorReference.accountUser(currentAccountProfileUseCase.get().userId().value());
  }
}
