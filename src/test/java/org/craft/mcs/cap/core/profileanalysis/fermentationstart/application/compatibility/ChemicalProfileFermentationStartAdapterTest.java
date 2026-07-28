package org.craft.mcs.cap.core.profileanalysis.fermentationstart.application.compatibility;

import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartProfile;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ChemicalProfileFermentationStartAdapterTest {

  @Test
  void retainsOnlyFermentationStartEvidenceAndPreservesValueShapes() {
    final var profile = ChemicalProfile.builder()
        .sugar(IntervalCharacteristicValue.of(
            SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.048")),
            SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))
        ))
        .ph(PointCharacteristicValue.of(PhValue.of("3.45")))
        .tannin(PointCharacteristicValue.of(TanninValue.of("1200")))
        .yeastAssimilableNitrogen(PointCharacteristicValue.of(
            YeastAssimilableNitrogenValue.of("180")
        ))
        .build();

    final var result = ChemicalProfileFermentationStartAdapter.adapt(profile);

    assertThat(result)
        .returns(
            IntervalCharacteristicValue.of(
                SpecificGravityValue.of("1.048"),
                SpecificGravityValue.of("1.052")
            ),
            FermentationStartProfile::specificGravity
        )
        .returns(
            PointCharacteristicValue.of(PhValue.of("3.45")),
            FermentationStartProfile::ph
        )
        .returns(
            PointCharacteristicValue.of(YeastAssimilableNitrogenValue.of("180")),
            FermentationStartProfile::yeastAssimilableNitrogen
        );
  }
}
