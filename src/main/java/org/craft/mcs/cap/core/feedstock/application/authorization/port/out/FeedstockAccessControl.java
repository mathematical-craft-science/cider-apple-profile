package org.craft.mcs.cap.core.feedstock.application.authorization.port.out;

import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;


/** Outbound authorization boundary for feedstock data owned by a cider-maker scope. */
public interface FeedstockAccessControl {

  void requireReadAccess(@NonNull CiderMakerId ciderMakerId);

  void requireWriteAccess(@NonNull CiderMakerId ciderMakerId);
}
