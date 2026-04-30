package org.craft.mcs.cap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CapApplicationTests {

  @Test
  void contextLoads() {
  }

}
