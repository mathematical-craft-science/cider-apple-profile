package org.craft.mcs.cap.appleprofile.application.usecase;

import java.util.function.Supplier;
import org.craft.mcs.cap.core.appleprofile.application.port.in.CreateAppleVarietyCommand;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.application.usecase.CreateAppleVarietyService;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CreateAppleVarietyServiceTest {

  private AppleVarietyRepository appleVarietyRepository;
  private TransactionHelper transactionHelper;
  private CreateAppleVarietyService service;

  @BeforeEach
  void setUp() {
    appleVarietyRepository = mock(AppleVarietyRepository.class);
    transactionHelper = mock(TransactionHelper.class);
    service = new CreateAppleVarietyService(appleVarietyRepository, transactionHelper);
  }

  @Test
  void createsAppleVarietyAndPassesItToRepository() {
    final var chemicalProfile = ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.052")
        )))
        .build();
    when(appleVarietyRepository.save(any(AppleVariety.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(transactionHelper.callInTransactionNonNull(any()))
        .thenAnswer(invocation -> executeSupplier(invocation.getArgument(0)));

    final var created = service.create(new CreateAppleVarietyCommand(
        "Yarlington Mill",
        chemicalProfile
    ));

    verify(transactionHelper).callInTransactionNonNull(any());
    final var savedCaptor = ArgumentCaptor.forClass(AppleVariety.class);
    verify(appleVarietyRepository).save(savedCaptor.capture());

    assertThat(created.varietyId()).isNotNull();
    assertThat(created.name().value()).isEqualTo("Yarlington Mill");
    assertThat(savedCaptor.getValue().chemicalProfile()).isEqualTo(chemicalProfile);
  }

  private static Object executeSupplier(Supplier<?> supplier) {
    return supplier.get();
  }
}
