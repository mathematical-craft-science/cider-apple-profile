package org.craft.mcs.cap.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.NonNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;


@AnalyzeClasses(
    packages = "org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class JdbcPersistenceModelArchitectureTest {

  @ArchTest
  static final ArchRule jdbc_entities_are_spring_data_jdbc_aggregate_rows =
      classes()
          .that()
          .haveSimpleNameEndingWith("JdbcEntity")
          .should()
          .beAnnotatedWith(Table.class)
          .andShould(haveFieldAnnotatedWith(Id.class))
          .andShould(haveFieldAnnotatedWith(CreatedDate.class))
          .andShould(haveFieldAnnotatedWith(LastModifiedDate.class))
          .andShould(haveFieldAnnotatedWith(Version.class));

  @ArchTest
  static final ArchRule jdbc_rows_are_manual_sql_rows_without_spring_data_mapping =
      classes()
          .that()
          .haveSimpleNameEndingWith("JdbcRow")
          .should()
          .notBeAnnotatedWith(Table.class)
          .andShould(notHaveFieldsAnnotatedWith(
              Id.class,
              Column.class,
              CreatedDate.class,
              LastModifiedDate.class,
              Version.class
          ));

  @ArchTest
  static final ArchRule spring_data_jdbc_repositories_do_not_expose_read_model_queries =
      classes()
          .that()
          .haveSimpleNameStartingWith("SpringData")
          .and()
          .haveSimpleNameEndingWith("JdbcRepository")
          .should(notDeclareReadModelQueryMethods());

  @ArchTest
  static final ArchRule jdbc_repository_adapters_remain_proxyable =
      classes()
          .that()
          .areAnnotatedWith(Repository.class)
          .should()
          .notHaveModifier(JavaModifier.FINAL);

  private static @NonNull ArchCondition<JavaClass> haveFieldAnnotatedWith(
      @NonNull Class<? extends Annotation> annotationType
  ) {
    return new ArchCondition<>("have a field annotated with @" + annotationType.getSimpleName()) {
      @Override
      public void check(
          @NonNull JavaClass item,
          @NonNull ConditionEvents events
      ) {
        final var hasAnnotatedField = item.getAllFields()
            .stream()
            .anyMatch(field -> field.isAnnotatedWith(annotationType));

        events.add(new SimpleConditionEvent(
            item,
            hasAnnotatedField,
            "%s should declare a field annotated with @%s".formatted(
                item.getName(),
                annotationType.getSimpleName()
            )
        ));
      }
    };
  }

  @SafeVarargs
  private static @NonNull ArchCondition<JavaClass> notHaveFieldsAnnotatedWith(
      @NonNull Class<? extends Annotation>... annotationTypes
  ) {
    return new ArchCondition<>("not have Spring Data JDBC mapping annotations on fields") {
      @Override
      public void check(
          @NonNull JavaClass item,
          @NonNull ConditionEvents events
      ) {
        final var violations = item.getAllFields()
            .stream()
            .flatMap(field -> fieldViolations(field, List.of(annotationTypes)).stream())
            .toList();

        events.add(new SimpleConditionEvent(
            item,
            violations.isEmpty(),
            "%s must not use Spring Data JDBC mapping annotations: %s".formatted(
                item.getName(),
                String.join(", ", violations)
            )
        ));
      }
    };
  }

  private static @NonNull List<@NonNull String> fieldViolations(
      @NonNull JavaField field,
      @NonNull List<@NonNull Class<? extends Annotation>> annotationTypes
  ) {
    return annotationTypes
        .stream()
        .filter(field::isAnnotatedWith)
        .map(annotationType -> "%s.@%s".formatted(field.getName(), annotationType.getSimpleName()))
        .toList();
  }

  private static @NonNull ArchCondition<JavaClass> notDeclareReadModelQueryMethods() {
    return new ArchCondition<>("not declare read-model query methods") {
      @Override
      public void check(
          @NonNull JavaClass item,
          @NonNull ConditionEvents events
      ) {
        final var violations = item.getMethods()
            .stream()
            .filter(method -> method.getOwner().equals(item))
            .filter(JdbcPersistenceModelArchitectureTest::hasReadModelQueryName)
            .map(JavaMethod::getName)
            .sorted()
            .toList();

        events.add(new SimpleConditionEvent(
            item,
            violations.isEmpty(),
            "%s must keep read-model query methods out of Spring Data aggregate repositories: %s"
                .formatted(item.getName(), String.join(", ", violations))
        ));
      }
    };
  }

  private static boolean hasReadModelQueryName(@NonNull JavaMethod method) {
    final var name = method.getName().toLowerCase(Locale.ROOT);
    return name.contains("candidate")
        || name.contains("point")
        || name.contains("summary")
        || name.contains("projection")
        || name.contains("score")
        || name.contains("current")
        || name.contains("latest");
  }
}
