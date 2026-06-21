package org.craft.mcs.cap.core.production.application;

import java.io.Serial;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.shared.application.ApplicationForbiddenException;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@SuppressWarnings("java:S110")
public final class ProductionSourceOwnershipMismatchException
    extends ApplicationForbiddenException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final transient @NonNull ProductionSourceReference sourceReference;
  private final transient @NonNull CiderMakerId expectedCiderMakerId;
  private final transient @NonNull CiderMakerId actualCiderMakerId;

  public ProductionSourceOwnershipMismatchException(
      @NonNull ProductionSourceReference sourceReference,
      @NonNull CiderMakerId expectedCiderMakerId,
      @NonNull CiderMakerId actualCiderMakerId
  ) {
    super("production source belongs to another cider maker: "
        + requireNonNull(sourceReference, "production source reference cannot be null").id());
    this.sourceReference = sourceReference;
    this.expectedCiderMakerId = requireNonNull(expectedCiderMakerId, "expected cider maker id cannot be null");
    this.actualCiderMakerId = requireNonNull(actualCiderMakerId, "actual cider maker id cannot be null");
  }

  public @NonNull ProductionSourceReference sourceReference() {
    return sourceReference;
  }

  public @NonNull CiderMakerId expectedCiderMakerId() {
    return expectedCiderMakerId;
  }

  public @NonNull CiderMakerId actualCiderMakerId() {
    return actualCiderMakerId;
  }
}
