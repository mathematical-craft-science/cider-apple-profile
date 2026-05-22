package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;


@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    PostgresDataJdbcTestConfiguration.class,
    JdbcAppleVarietyRepository.class
})
class JdbcAppleVarietyRepositoryDataJdbcTest {

  @Autowired
  private SpringDataAppleVarietyJdbcRepository springDataRepository;

  @Autowired
  private JdbcAppleVarietyRepository repository;

  @Test
  void savesExistingAppleVarietyAsUpdate() {
    final var saved = repository.save(appleVariety());

    saved.rename(AppleVarietyName.of("Updated variety"));
    saved.changeChemicalProfile(ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.055"))))
        .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("6.1")))
        .build());

    final var updated = repository.save(saved);

    assertThat(springDataRepository.count()).isOne();
    assertThat(updated.name().value()).isEqualTo("Updated variety");
    assertThat(updated.chemicalProfile().titratableAcidity()).isPresent();
  }

  private static AppleVariety appleVariety() {
    return AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Initial variety"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))))
            .build()
    );
  }
}
