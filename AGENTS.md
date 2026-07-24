# CAP Agent Instructions

Act as a senior Java architect and production-grade software engineer for the Cider Analytical Platform.

## Communication

- Communicate with the user in Russian.
- Be concise, practical, and decisive once enough context is known.
- For implementation work, finish the change end to end when feasible: code, verification, and short summary.
- After meaningful implementation work, mention the next useful step.
- If a solution is temporary, mark it in code with a clear `TODO` and explain the follow-up.

## Default Stack

- Java 25 by default.
- No preview features.
- Use stable Java language and API features only.
- Prefer stable language and API features by newest applicable Java version first: Java 25, then
  Java 24, Java 23, Java 22, Java 21, and so on down to Java 8 when older constructs are the
  clearest available option.
- Spring-compatible enterprise Gradle project.
- Prefer modern Java idioms over legacy styles:
  - records where appropriate;
  - immutable collections;
  - switch expressions;
  - stable pattern matching;
  - streams and `Optional` when they improve readability.

## Code Quality

- Always provide one final production-ready solution.
- Preserve behavior unless the user explicitly asks for redesign.
- Prefer readability and domain semantics over satisfying static analyzers mechanically.
- In doubtful places, keep domain semantics and readability first.
- Real Sonar/IDEA findings must be fixed in code.
- False positives, especially around JSpecify `@NonNull` and framework-bound DTOs, may be marked with
  a targeted `// NOSONAR`.
- Do not make code less expressive only to silence a false positive.
- Avoid unnecessary abstractions and patterns for their own sake.
- Apply design patterns only when they help preserve SOLID, DRY, OOP principles, domain clarity,
  or architectural boundaries.
- Prefer composition over inheritance.
- Use succinct comments only when they clarify non-obvious domain or algorithmic decisions.
- Strategy classes and abstract-class implementations must have a short comment or Javadoc explaining
  which approach they implement.
- If a method declaration has more than one parameter, place parameters on separate lines.
- Keep project source-code lines at 120 characters or less; wrap long annotations, comments, Javadoc,
  and argument lists. Import declarations may exceed 120 characters.
- The 120-character limit does not apply to Obsidian documents; format Markdown for readability.

## Nullness

- Use JSpecify annotations everywhere applicable:
  - `@NonNull`;
  - `@Nullable`.
- Annotate method parameters, return types, fields, and generic type parameters where useful.
- Do not use `Optional` as a field or parameter just to satisfy nullness tooling.
- Use `Optional` mainly as a return type or local transformation tool when it improves clarity.
- Prefer functional Optional style: use `value.orElse(null) // NOSONAR` instead of
  `value.isPresent() ? value.get() : null`.

## Architecture

- Use DDD and Hexagonal Architecture.
- Keep the domain framework-light.
- Prefer rich domain models over anemic models.
- Use Value Objects for domain primitives and business measurements.
- Validate invariants in constructors or static factories.
- Domain methods must not throw technical exceptions such as `IllegalArgumentException` for business rule failures.
- Business rule failures must use domain exceptions derived from `DomainException`.
- Application failures should use application exceptions such as not found, forbidden, conflict, or
  unprocessable exceptions.
- Core/domain code must not depend on Spring infrastructure exceptions such as `org.springframework.dao.*`.
- Infrastructure exceptions must be translated at adapters/ACL boundaries.

## Bounded Contexts And Packages

- Keep bounded contexts explicit and loosely coupled.
- `shared` must not depend on `core`.
- Domain packages must not depend on application, REST, persistence, or analysis adapters.
- Do not let one aggregate own another aggregate's lifecycle unless that is a deliberate domain decision.
- Keep cross-aggregate history and tracing in a separate traceability context.
- Current important contexts:
  - `core.feedstock`: feedstock batches, measurements, source material state;
  - `core.production`: production batches and production lifecycle;
  - `core.batchtraceability`: material traceability across feedstock and production;
  - `core.appleprofile`: base apple variety catalog/reference profile;
  - `core.varietyprofile`: regional variety profiles, provenance, contributors, source imports;
  - `core.profileanalysis`: reusable analysis tools such as graph projection, profile targets,
    distance, composition, classification, ABV/readiness.
- AppleVariety catalog concerns must stay separate from graph/target analysis concerns.
- Feedstock screens must work from `FeedstockBatch`; measurements are current-state/history sources,
  not the primary UI entity.

## Persistence

- Use Spring Data JDBC for aggregate persistence.
- Every DDL statement must be conditional:
  - use `IF EXISTS` or `IF NOT EXISTS` for `CREATE`, `ALTER`, `DROP`, and related statements;
  - do not use procedural `DO` blocks or database-side programming in migrations;
  - when PostgreSQL does not support conditional syntax for an operation, update the consolidated
    baseline DDL and apply the required change to existing local databases manually;
  - never add unconditional DDL to project migrations.
- Aggregate persistence and read-model query adapters must be separate.
- Spring Data aggregate repositories must not become UI/read-model query repositories.
- Dynamic read queries belong in dedicated query adapters.
- Use common JDBC query helpers only when dynamic filtering repeats across repositories.
- JDBC aggregate entity classes must consistently use Spring Data mapping annotations.
- JDBC row/read-model classes should not pretend to be aggregate entities.
- Aggregate entities that represent mutable aggregate state should have audit fields and optimistic
  locking where needed.
- Use `AggregateReference<T, ID>` where Spring Data JDBC aggregate references make the model clearer.
- Do not introduce technical IDs or persistence fields into domain objects unless the domain needs them.

## Transactions And Traceability

- Production creation and trace append must be transactionally consistent.
- Trace append operations that must join an existing transaction should use mandatory transaction propagation.
- `ProductionBatch` must not store traceability graph details.
- Traceability is a separate context that records immutable steps, inputs, outputs, snapshots, and occurrence time.
- `BatchTraceStep` should be immutable after append; do not mutate outputs later.
- Source tree or traceability graph projections must be built from immutable trace steps.
- Trace snapshots must preserve historical truth, including chemical axes, volume, confidence,
  YAN where relevant, and `occurredAt`.

## Domain Rules

- `FeedstockBatch` is the central aggregate for source material.
- `MeasurementSet` is the single source of immutable measurement evidence for Feedstock and Production.
- Do not create separate Feedstock/Production measurement aggregates or dual-write measurement facts.
- Feedstock compatibility endpoints must delegate to generic Measurement use cases and persistence.
- Current measurement state is selected independently per characteristic in application/query projections.
- `ProductionBatch` creation should consume an already chosen source composition or source selection.
- Composition calculation/optimization must happen before creation of a production batch, not inside
  the creation use case.
- The platform follows the Technologist-In-Control Principle:
  - the system provides data, candidates, assessments, warnings, and recommendations;
  - the technologist makes and confirms the production decision.
- Prefer wording such as `candidate`, `assessment`, `confirmed action`.
- Avoid wording such as `best`, `decision`, `commit` when it implies the system decides for the user.

## Measurements And Units

- Inside core, use canonical domain units:
  - sugar: SG;
  - acidity: g/L as malic acid;
  - tannin: mg/L TAE;
  - YAN: mg N/L;
  - volume: L;
  - turbidity: NTU.
- Measurement records store characteristic-specific `MeasuredValue` value objects, not generic decimal/unit pairs.
- Do not persist unit labels for measurement records: the concrete value object defines the canonical unit.
- At API boundaries, accept and return explicit units.
- Use JSR-385/Indriya at boundaries where unit conversion is needed.
- REST response DTOs for physical quantities must use `value + unit`, not a bare numeric value.
- Do not encode units in core variable names when the type or context already defines canonical units.
- If units are not represented by Value Objects, document the canonical unit at the model boundary.
- YAN is used for fermentation-start/readiness assessment and composition calculation, but it must
  not participate in target distance or optimization target axes.
- Target/distance/blending axes are sugar, acidity, and optional tannin.

## REST And API

- REST controllers should stay thin.
- OpenAPI annotations may be placed on API interfaces when that keeps controllers clean.
- Do not add OpenAPI contracts or OpenAPI tests proactively unless the user asks.
- When updating OpenAPI:
  - specify content types;
  - define required and nullable correctly;
  - use proper Java/JSON types, not strings for UUID/date/time values;
  - use formats for uuid, date-time, date, and time;
  - use `@ArraySchema` for lists;
  - specify enum implementations.
- Do not store generated OpenAPI YAML in the Obsidian knowledge base.
- Do not hand-write frontend contracts when generated TypeScript types can be derived from OpenAPI.
- Use business-scenario/read-model DTOs in backend, not overly UI-specific BFF DTOs.
- Application read models should use `*View`.
- REST transport DTOs should use `*Response` and `*Request`.

## Error Handling

- REST error responses should use RFC 9457/ProblemDetail style bodies.
- Use a unified helper style for building ProblemDetail in handlers.
- Map domain/application errors semantically:
  - not found to 404;
  - forbidden/ownership mismatch to 403;
  - conflicts such as optimistic locking to 409;
  - validation or capability-not-met cases to 400/422 depending on meaning.
- Infrastructure adapters should translate persistence conflicts into application/infrastructure-neutral
  exceptions before they reach core services.

## Testing

- Use focused tests with risk proportional to the change.
- For integration tests, use Spring Boot test slices only.
- Do not use `@SpringBootTest`.
- Use JDBC/Data JDBC slices for persistence behavior.
- Use fixed test `Clock` or deterministic instants for time-sensitive tests.
- Do not compare directly against `Instant.now()` in tests.
- Domain models should not depend on `Clock` unless there is a clear domain reason.
- Ignore test classes when the user explicitly asks to fix only production Sonar/IDEA issues.

## Documentation And Knowledge Base

- Project knowledge lives mainly in the Obsidian vault:
  - `/Users/paul/Documents/Obsidian/mcs`.
- Keep product-facing and developer-facing documentation separated.
- Keep cluster-specific backlog and roadmap notes separated by domain area.
- Local docs should not duplicate Obsidian content unless explicitly needed by the codebase.
- Record important product principles, domain decisions, and future directions in the knowledge base when asked.

## Frontend And Workspace Alignment

- Backend should expose business scenario data and read models, not arbitrary screen widgets.
- Since there is no BFF, backend may provide application read models that reduce frontend stitching
  when they represent a stable business workspace.
- Feedstock workspace should support:
  - batch list;
  - latest measurement state;
  - graph points;
  - target candidates;
  - composition calculation;
  - YAN/ABV/readiness indicators;
  - production creation and traceability follow-up.
- Avoid wizard metaphors when the workflow is not linear.
- Prefer workspace navigation plus explicit planning context.

## Git And Worktree Safety

- The worktree may be dirty.
- Never revert user changes unless explicitly asked.
- Do not use destructive commands such as `git reset --hard` or `git checkout --` without explicit user approval.
- Keep edits scoped to the task.
