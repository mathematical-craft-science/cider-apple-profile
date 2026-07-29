package org.craft.mcs.cap.core.traceability.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;


/**
 * Typed chemical characteristics retained for one immutable material state in traceability.
 *
 * <p>This is a trace snapshot projection, not a reusable chemical profile or an analysis input.</p>
 */
@ValueObject
public record BatchTraceMaterialCharacteristics(
    @Nullable BatchTraceCharacteristicSnapshot<SpecificGravityValue> specificGravity,
    @Nullable BatchTraceCharacteristicSnapshot<TitratableAcidityValue> titratableAcidity,
    @Nullable BatchTraceCharacteristicSnapshot<TanninValue> tannin,
    @Nullable BatchTraceCharacteristicSnapshot<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
) {

  public BatchTraceMaterialCharacteristics {
    if (specificGravity == null
        && titratableAcidity == null
        && tannin == null
        && yeastAssimilableNitrogen == null) {
      throw new DomainRuleViolationException(
          "batch trace material characteristics must contain at least one value"
      );
    }
  }
}
