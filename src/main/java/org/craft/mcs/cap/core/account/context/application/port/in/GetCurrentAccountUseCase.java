package org.craft.mcs.cap.core.account.context.application.port.in;

import org.jspecify.annotations.NonNull;

/**
 * Returns the provisioned CAP account for the active workspace selected by request context.
 *
 * <p>A missing selection is accepted only for accounts with exactly one active workspace.
 */
public interface GetCurrentAccountUseCase {

  @NonNull AccountMeView get();
}
