# Cider Analytical Platform Roadmap

## Strategy

Система развивается как decision-support инструмент для сидродела: от каталога
и оценки сырья к подбору технологических решений, затем к созданию
производственной партии, сопровождению ферментации, alerts и учету операций.

Шкалы из backlog:

- Ценность: 1-10, практическая помощь сидроделу в принятии решений.
- Сложность: 1-10, доменная и техническая сложность.

## Current Baseline

Уже сделано:

- Sugar-acidity graph points для `AppleVariety` и `FeedstockSample`.
- Unplottable records и warnings по неполному chemical profile.
- Capability model: `SUGAR_ACIDITY_PLOT`, `NORTH_AMERICAN_PROFILE`,
  `SINGLE_VARIETY_CIDER_ASSESSMENT`.
- North American capability check с `ProblemDetail`.
- Feedstock batch model: season, source, available volume, samples внутри batch.
- Potential ABV для `FeedstockSample` по normalized SG.
- POST search/query model и cursor pagination для graph points.
- Разделение `shared` на `shared.kernel` / `shared.platform`.
- `core.cideranalysis` и `core.fermentation` как отдельные направления.
- Разделенные REST exception handlers.
- Явные Spring configuration classes вместо общего composition root.

## Roadmap Dependency

`ProductionBatch` может формироваться не только из одного `FeedstockBatch`, но и
из нескольких сырьевых партий по результату blending optimization. Поэтому
production foundation зависит от аналитического слоя:

1. Distance/target model для chemical profiles.
2. Blending MVP, который строит `ChemicalProfileComposition`.
3. ProductionBatch, который принимает composition и атомарно списывает объемы.
4. Fermentation monitoring и yeast-aware intelligence.

## Phase 1 - Distance And Target Model

Цель: создать математическую основу для similarity search, rectangle selection
и будущего blending.

| Фича | Ценность | Сложность | Статус |
|---|---:|---:|---|
| Sugar-acidity target model: predefined zone или custom rectangle | 9 | 5 | Next |
| Normalized profile coordinates | 8 | 5 | Next |
| Normalized weighted Euclidean distance | 9 | 7 | Next |
| Rectangle selection на graph | 8 | 6 | Next |
| Дополнительные filters: sugar/acidity/tannin levels | 7 | 4 | Planned |
| Graph export CSV/Excel | 7 | 4 | Planned |

Минимальный implementation slice:

1. Ввести target model с двумя вариантами: predefined `SugarAcidityZone` и custom rectangle `[sugarMin..sugarMax, acidityMin..acidityMax]`.
2. Нормализовать sugar/acidity шкалы перед расчетом расстояния.
3. Реализовать weighted Euclidean distance до target point или target area.
4. Подключить rectangle selection к existing graph search для `AppleVariety` и `FeedstockSample`.
5. Добавить CSV export как низкую по сложности полезную функцию.

## Phase 2 - Blending Planning MVP

Цель: предложить сидроделу практичный blend plan из реального доступного сырья,
который затем может стать источником production batch.

| Фича | Ценность | Сложность | Статус |
|---|---:|---:|---|
| Blending для FeedstockBatch с target area | 10 | 10 | Next |
| ChemicalProfileComposition | 10 | 8 | Next |
| One optimized blend option | 9 | 7 | Next |
| Учет available volume constraints | 10 | 8 | Next |
| Explain unreachable target | 9 | 8 | Planned |
| Multiple blend options ranking | 9 | 9 | Later |

Рекомендуемый порядок:

1. Начать с target model: predefined zone из уже реализованных sugar-acidity zones или custom rectangle через API.
2. Использовать normalized weighted Euclidean distance из Phase 1.
3. Сначала вернуть один optimized blend option через bounded/discretized search.
4. Учитывать ограничения по доступному объему каждого source batch/sample.
5. Возвращать структурированный `ChemicalProfileComposition`: source, volume, calculated profile, distance, total volume, warnings.
6. Tannin, ellipse/polygon/lasso targets, multiple ranked options и Mahalanobis оставить на следующие итерации.

## Phase 3 - Production Foundation

Цель: принять composition и превратить его в beverage-neutral
производственную партию с учетом объемов, операций, потерь, типа продукта и
будущих технологий.

| Фича | Ценность | Сложность | Статус |
|---|---:|---:|---|
| ProductionBatch | 10 | 9 | Planned |
| Product type / intent: cider, perry, distillate base, calvados, pommeau | 9 | 5 | Planned |
| Atomic volume reservation/write-off from multiple FeedstockBatch sources | 10 | 8 | Planned |
| Optimistic locking для FeedstockBatch.availableVolume | 9 | 6 | Planned |
| ProductionOperation history | 9 | 7 | Planned |
| START_PRODUCTION / START_FERMENTATION / RACKING / TRANSFER / BOTTLING | 9 | 7 | Planned |
| VOLUME_ADJUSTMENT / SAMPLE_TAKEN с reason | 8 | 5 | Planned |
| Domain events: volume reserved, production started, operation recorded | 8 | 6 | Planned |
| First fermentation phase state | 9 | 6 | Planned |

Минимальный implementation slice:

1. Создать `ProductionBatch` aggregate.
2. Создавать production batch из `ChemicalProfileComposition`; single-source case считать частным случаем plan-а.
3. Добавить `productType` / `productIntent`: cider, perry, distillate base, calvados, pommeau, other.
4. Атомарно списывать или резервировать объемы из всех source `FeedstockBatch`.
5. Добавить optimistic locking для защиты от параллельного использования одного объема.
6. Хранить `initialVolume`, `currentVolume`, composition reference и историю `ProductionOperation`.
7. Поддержать `START_PRODUCTION`, `START_FERMENTATION`, `RACKING`, `TRANSFER`, `BOTTLING`, `VOLUME_ADJUSTMENT`, `SAMPLE_TAKEN`.
8. Добавить domain events и unit/application/concurrency tests для volume invariants.
9. Не моделировать пока весь workflow технологий как жесткий enum.

## Phase 4 - Fermentation Intelligence

Цель: превратить production batch в наблюдаемый процесс, по которому система
может давать прогнозы и предупреждения.

| Фича | Ценность | Сложность | Статус |
|---|---:|---:|---|
| Minimal yeast catalog | 9 | 6 | Planned |
| Measurements time series: SG, temperature, pH, acidity | 10 | 7 | Planned |
| Fermentation monitoring по партиям | 10 | 8 | Planned |
| Rule-based alerts: stuck fermentation, temperature/pH/ABV risks | 10 | 8 | Planned |
| Yeast-aware FG / ABV prediction | 9 | 7 | Planned |

Рекомендуемый порядок:

1. Добавить минимальный `YeastStrain`: id, name, temperature range, pH range, alcohol tolerance, optional producer.
2. Добавить time-series measurements к production batch.
3. Добавить расчет текущего/ожидаемого ABV по starting SG и текущему SG.
4. Добавить первые alerts: temperature out of yeast range, SG не снижается, ABV near tolerance.
5. Позже расширить до fermentation-model adjusted прогнозов.

## Phase 5 - Catalog And Discovery Enrichment

Цель: повысить качество каталога сортов и полезность discovery-сценариев.

| Фича | Ценность | Сложность | Статус |
|---|---:|---:|---|
| Classification filters: English/French/Spanish/Long Ashton | 8 | 5 | Planned |
| Admin data-quality reports по AppleVariety | 7 | 5 | Planned |
| Frontend graph contract: tooltips, selected area, linked rows | 7 | 5 | Planned |
| AppleVariety synonyms/local names | 7 | 5 | Planned |
| Cider tradition/origin | 6 | 4 | Planned |
| Typical chemical profile confidence/source | 7 | 5 | Planned |
| Сравнение FeedstockSample с AppleVariety catalog | 7 | 7 | Planned |
| typicalPotentialAbv для AppleVariety | 4 | 4 | Low priority |
| Standalone cider suitability | 5 | 6 | Deferred |

Рекомендуемый порядок:

1. Добавить фильтры по classification system/class.
2. Добавить admin report по неполным AppleVariety profiles.
3. Стабилизировать frontend contract для graph metadata, points, tooltips и linked list.
4. Добавить synonyms/local names для поиска.
5. Добавить source/confidence для typical chemical profile.
6. Добавить comparison/similarity между feedstock sample и catalog varieties.
7. Вернуться к standalone cider suitability только после более ясной user story.

## Phase 6 - Scale And Projections

Цель: удержать search, graph и blending быстрыми при росте каталога и
вычислимых фильтров.

| Фича | Ценность | Сложность | Статус |
|---|---:|---:|---|
| Materialized graph/classification projection | 7 | 8 | Later |
| Computed profile indexes | 7 | 8 | Later |
| Полный REST error strategy | 6 | 4 | Partial |
| Validation error model | 6 | 4 | Later |
| OpenAPI cleanup по требованию | 4 | 4 | On demand |

Рекомендуемый порядок:

1. Не делать projections заранее.
2. Вернуться к ним, когда application-layer filtering/blending начнет конфликтовать с cursor pagination и объемом данных.
3. Error model расширять постепенно при появлении новых классов ошибок.
4. OpenAPI контракты править только по отдельному запросу.

## Next Recommended Step

Следующий лучший шаг: `Distance/Target Model` для sugar-acidity profile.

Причина: без distance function нельзя корректно сделать blending optimization,
а без blending/source plan нельзя корректно спроектировать `ProductionBatch`,
который может формироваться из нескольких `FeedstockBatch`.
