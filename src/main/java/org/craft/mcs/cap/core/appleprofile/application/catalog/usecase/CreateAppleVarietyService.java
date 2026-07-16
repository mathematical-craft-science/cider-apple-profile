package org.craft.mcs.cap.core.appleprofile.application.catalog.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.in.AppleVarietyCreated;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.in.CreateAppleVarietyCommand;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.in.CreateAppleVarietyUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for creating catalog apple varieties.
 *
 * <p>The service owns the write-side use case boundary: it creates a new aggregate id, delegates
 * invariant checks to {@link AppleVariety}, persists through the outbound repository port, and
 * returns an immutable application result instead of exposing the mutable aggregate to REST
 * adapters.
 */
@Component
@RequiredArgsConstructor
public final class CreateAppleVarietyService
    implements CreateAppleVarietyUseCase {

  private final @NonNull AppleVarietyRepository appleVarietyRepository;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull AppleVarietyCreated create(@NonNull CreateAppleVarietyCommand command) {
    requireNonNull(command, "create apple variety command cannot be null");

    return transactionHelper.callInTransactionNonNull(() -> {
      final var appleVariety = AppleVariety.create(
          AppleVarietyId.newId(),
          command.name(),
          command.chemicalProfile()
      );

      final var saved = appleVarietyRepository.create(appleVariety);

      return new AppleVarietyCreated(
          saved.varietyId(),
          saved.name()
      );
    });
  }
}
