package org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile;

import org.jspecify.annotations.NonNull;

/**
 * Reports that the infrastructure seed resource could not be read or translated into catalog commands.
 */
public final class AppleVarietySeedImportException extends RuntimeException {

  public AppleVarietySeedImportException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
