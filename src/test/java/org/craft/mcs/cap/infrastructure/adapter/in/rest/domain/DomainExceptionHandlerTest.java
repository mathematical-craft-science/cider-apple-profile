package org.craft.mcs.cap.infrastructure.adapter.in.rest.domain;

import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.sugar.converter.UnsupportedSugarConversionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class DomainExceptionHandlerTest {

  @Test
  void mapsDomainRuleViolationToProblemDetail() {
    final var response = new DomainExceptionHandler().handleDomainRuleViolation(
        new DomainRuleViolationException("invalid domain state")
    );

    assertThat(response.getStatusCode().value()).isEqualTo(422);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Domain rule violation");
    assertThat(response.getBody().getDetail()).isEqualTo("invalid domain state");
  }

  @Test
  void mapsDomainConflictToProblemDetail() {
    final var response = new DomainExceptionHandler().handleDomainConflict(
        new DomainConflictException("volume changed")
    );

    assertThat(response.getStatusCode().value()).isEqualTo(409);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Domain state conflict");
    assertThat(response.getBody().getDetail()).isEqualTo("volume changed");
  }

  @Test
  void mapsUnsupportedSugarConversionToBadRequest() {
    final var response = new DomainExceptionHandler().handleUnsupportedSugarConversion(
        new UnsupportedSugarConversionException(String.class, Integer.class)
    );

    assertThat(response.getStatusCode().value()).isEqualTo(400);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Unsupported sugar conversion");
  }

}
