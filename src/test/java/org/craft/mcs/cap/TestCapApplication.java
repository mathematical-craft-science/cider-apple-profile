package org.craft.mcs.cap;

import org.springframework.boot.SpringApplication;

public class TestCapApplication {

  static void main(String[] args) {
    SpringApplication.from(CapApplication::main).with(TestcontainersConfiguration.class).run(args);
  }

}
