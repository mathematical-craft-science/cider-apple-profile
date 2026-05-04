package org.craft.mcs.cap.shared.domain.measurement.value;

public sealed interface SugarValue
    permits BrixValue, PlatoValue, SpecificGravityValue {
}
