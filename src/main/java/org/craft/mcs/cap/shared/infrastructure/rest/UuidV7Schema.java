package org.craft.mcs.cap.shared.infrastructure.rest;


/** Shared OpenAPI constraints for RFC 9562 UUID version 7 identifiers. */
public final class UuidV7Schema {

  public static final String FORMAT = "uuid";

  public static final String PATTERN =
      "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-7[0-9a-fA-F]{3}-[89aAbB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";

  private UuidV7Schema() {
  }
}
