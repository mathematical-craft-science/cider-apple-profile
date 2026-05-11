package org.craft.mcs.cap.core.appleprofile.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyCreated;
import org.craft.mcs.cap.core.appleprofile.application.port.in.CreateAppleVarietyCommand;
import org.craft.mcs.cap.core.appleprofile.application.port.in.CreateAppleVarietyUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


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
          AppleVarietyName.of(command.name()),
          command.chemicalProfile()
      );

      final var saved = appleVarietyRepository.save(appleVariety);

      return new AppleVarietyCreated(
          saved.varietyId(),
          saved.name()
      );
    });
  }
}
