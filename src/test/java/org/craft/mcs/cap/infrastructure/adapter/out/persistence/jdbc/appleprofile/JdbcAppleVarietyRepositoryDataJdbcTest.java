package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.appleprofile;

import org.craft.mcs.cap.core.appleprofile.application.catalog.AppleVarietyNameConflictException;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.testing.jdbc.PostgresDataJdbcTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


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
    final var created = repository.create(appleVariety());
    final var saved = repository.findVersionedById(created.varietyId()).orElseThrow();

    saved.aggregate().rename(AppleVarietyName.of("Updated variety"));
    saved.aggregate().changeReferenceProfile(new AppleVarietyReferenceProfile(
        PointCharacteristicValue.of(SpecificGravityValue.of("1.055")),
        PointCharacteristicValue.of(TitratableAcidityValue.of("6.1")),
        null,
        null
    ));

    final var updated = repository.update(saved).aggregate();

    assertThat(springDataRepository.count()).isOne();
    assertThat(updated.name().value()).isEqualTo("Updated variety");
    assertThat(updated.referenceProfile().titratableAcidity()).isNotNull();
  }

  @Test
  void rejectsAppleVarietyStateLoadedBeforeAnotherUpdate() {
    final var created = repository.create(appleVariety());
    final var first = repository.findVersionedById(created.varietyId()).orElseThrow();
    final var stale = repository.findVersionedById(created.varietyId()).orElseThrow();

    first.aggregate().rename(AppleVarietyName.of("First update"));
    repository.update(first);
    stale.aggregate().rename(AppleVarietyName.of("Stale update"));

    assertThatThrownBy(() -> repository.update(stale))
        .isInstanceOf(ConcurrentResourceModificationException.class)
        .hasMessage("resource was modified concurrently: apple variety");
    assertThat(repository.findVersionedById(created.varietyId()))
        .get()
        .extracting(VersionedAggregate::aggregate)
        .extracting(AppleVariety::name)
        .isEqualTo(AppleVarietyName.of("First update"));
  }

  @Test
  void preservesMeaningfulCatalogReferenceProfilePrecision() {
    final var variety = AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Precision variety"),
        new AppleVarietyReferenceProfile(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.0524")),
            PointCharacteristicValue.of(TitratableAcidityValue.of("6.517")),
            PointCharacteristicValue.of(PhValue.of("3.456")),
            PointCharacteristicValue.of(TanninValue.of("1200.5"))
        )
    );

    repository.create(variety);

    assertThat(repository.findVersionedById(variety.varietyId()))
        .get()
        .extracting(VersionedAggregate::aggregate)
        .extracting(AppleVariety::referenceProfile)
        .satisfies(profile -> assertThat(profile)
            .returns(variety.referenceProfile().specificGravity(), AppleVarietyReferenceProfile::specificGravity)
            .returns(variety.referenceProfile().titratableAcidity(),
                AppleVarietyReferenceProfile::titratableAcidity)
            .returns(variety.referenceProfile().ph(), AppleVarietyReferenceProfile::ph)
            .returns(variety.referenceProfile().tannin(), AppleVarietyReferenceProfile::tannin));
  }

  @Test
  void translatesCaseInsensitiveNameUniquenessToApplicationConflict() {
    repository.create(appleVariety());
    final var duplicate = AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("INITIAL VARIETY"),
        new AppleVarietyReferenceProfile(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.050")),
            null,
            null,
            null
        )
    );

    assertThatThrownBy(() -> repository.create(duplicate))
        .isInstanceOf(AppleVarietyNameConflictException.class)
        .hasMessage("apple variety already exists: INITIAL VARIETY");
  }

  private static AppleVariety appleVariety() {
    return AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Initial variety"),
        new AppleVarietyReferenceProfile(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.052")),
            null,
            null,
            null
        )
    );
  }
}
