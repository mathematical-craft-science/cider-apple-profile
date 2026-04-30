package org.craft.mcs.cap.shared.converter;

import java.math.MathContext;
import java.util.List;

public final class CompositionRoot {

  private CompositionRoot() {
  }

  public static SugarConversionService sugarConversionService() {

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
