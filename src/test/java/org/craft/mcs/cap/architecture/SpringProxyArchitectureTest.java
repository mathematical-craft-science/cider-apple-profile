package org.craft.mcs.cap.architecture;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;


@AnalyzeClasses(
    packages = "org.craft.mcs.cap",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class SpringProxyArchitectureTest {

  @ArchTest
  static final ArchRule classes_with_transactional_methods_remain_proxyable =
      classes()
          .that()
          .containAnyMethodsThat(annotatedWith(Transactional.class))
          .should()
          .notHaveModifier(JavaModifier.FINAL);

  @ArchTest
  static final ArchRule transactional_methods_remain_overridable =
      methods()
          .that()
          .areAnnotatedWith(Transactional.class)
          .should()
          .notBeFinal()
          .andShould()
          .notBePrivate();
}
