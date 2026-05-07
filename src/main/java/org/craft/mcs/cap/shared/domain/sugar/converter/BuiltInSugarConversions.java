package org.craft.mcs.cap.shared.domain.sugar.converter;

import java.math.MathContext;
import java.util.List;
import org.jspecify.annotations.NonNull;


public final class BuiltInSugarConversions {

  private BuiltInSugarConversions() {
  }

  @NonNull
  public static SugarConversionService defaultService() {

    final var mc = MathContext.DECIMAL64;

    final var brixToSg = new BrixToSpecificGravityStrategy(mc);
    final var sgToBrix = new SpecificGravityToBrixStrategy(mc);
    final var platoToSg = new PlatoToSpecificGravityStrategy(mc);
    final var sgToPlato = new SpecificGravityToPlatoStrategy(mc);

    var registry = new SugarConversionRegistry(List.of(
        brixToSg,
        sgToBrix,
        platoToSg,
        sgToPlato,
        new BrixToPlatoStrategy(brixToSg, sgToPlato),
        new PlatoToBrixStrategy(platoToSg, sgToBrix)
    ));

    return new SugarConversionService(registry);
  }
}
