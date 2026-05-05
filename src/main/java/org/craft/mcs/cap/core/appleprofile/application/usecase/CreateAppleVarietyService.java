package org.craft.mcs.cap.core.appleprofile.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.in.CreateAppleVarietyCommand;
import org.craft.mcs.cap.core.appleprofile.application.port.in.CreateAppleVarietyUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.application.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class CreateAppleVarietyService
    implements CreateAppleVarietyUseCase {

  private final AppleVarietyRepository appleVarietyRepository;
  private final TransactionHelper transactionHelper;

  @Override
  @NonNull
  public AppleVariety create(@NonNull CreateAppleVarietyCommand command) {
    requireNonNull(command, "create apple variety command cannot be null");

    final var appleVariety = AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of(command.name()),
        command.chemicalProfile()
    );

    return transactionHelper.callInTransactionNonNull(() -> appleVarietyRepository.save(appleVariety));
  }
}
