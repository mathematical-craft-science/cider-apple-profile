package org.craft.mcs.cap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class CapApplicationTests {

  @Test
  void declaresSpringBootApplicationEntryPoint() {
    assertThat(CapApplication.class).hasAnnotation(SpringBootApplication.class);
  }

}
