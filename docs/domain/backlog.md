# Доменный backlog

## Приоритизация

Шкалы:

- Ценность: 1-10, насколько фича помогает сидроделу принимать практические
  решения.
- Сложность: 1-10, доменная и техническая сложность реализации.

### Высший приоритет

| Фича | Ценность | Сложность | Причина |
|---|---:|---:|---|
| ~~FeedstockSample graph + warnings по неполному профилю~~ | 9 | 4 | Реализованы graph points, unplottable records и warnings по missing axes. |
| ~~Potential ABV для FeedstockSample~~ | 9 | 3 | Реализован прогноз ABV по normalized SG с method/confidence/FG source. |
| ProductionBatch как производственная партия | 10 | 9 | Основа для учета объема, операций, ферментации, снятия с осадка, розлива и разных технологий; сложность повышена из-за интеграции с запасами, concurrency и операциями объема. |
| Fermentation monitoring по партиям | 10 | 8 | Превращает систему в помощника, который следит за процессом и показывает динамику. |
| Alerts: stuck fermentation / выход за temperature/pH/ABV ranges | 10 | 8 | Помогает не потерять партию и вовремя вмешаться в процесс. |
| Yeast catalog + yeast-aware fermentation checks | 9 | 7 | Дрожжи влияют на FG/ABV, температурные риски, остановку ферментации и мониторинг. |

### Следующий сильный слой

| Фича | Ценность | Сложность | Причина |
|---|---:|---:|---|
| Blending для FeedstockSample с target area | 10 | 10 | Сильный decision support: подбор пропорций купажа под цель с учетом ограничений; сложность высокая из-за target geometry, метрики расстояния, нормализации шкал и volume constraints. |
| Distance metric / target model для chemical profile | 9 | 7 | Обязательная основа для blending optimization, similarity search и сравнения samples с catalog varieties. |
| ~~Available volume для FeedstockBatch~~ | 8 | 4 | Реализовано как `FeedstockBatch.availableVolume`; sample создается внутри batch. |
| Исследование каталога через выделенную область на graph | 8 | 6 | Делает каталог discovery-инструментом, а не статичным справочником. |
| Фильтрация по English/French/Spanish/Long Ashton classifications | 8 | 5 | Позволяет сидроделу работать в привычной системе терминов. |
| ~~Query/filter/search model с cursor~~ | 7 | 6 | Реализованы POST query models, cursor pagination и malformed cursor ProblemDetail. |

### Средний приоритет

| Фича | Ценность | Сложность | Причина |
|---|---:|---:|---|
| Admin data-quality reports по AppleVariety | 7 | 5 | Важно для качества каталога, но косвенно полезно сидроделу. |
| Materialized projection/index для вычислимых профилей | 7 | 8 | Понадобится при росте каталога и сложных фильтрах, но можно отложить до нагрузки. |
| ~~FeedstockBatch season/batch model~~ | 8 | 5 | Реализован `FeedstockBatch` с season/source/availableVolume и sample внутри batch. |
| Сравнение FeedstockSample с AppleVariety каталогом | 7 | 7 | Полезный экспертный сценарий, но требует similarity metric. |
| Frontend graph contract: tooltips, selected area, linked rows | 7 | 5 | Делает экспертность видимой и удобной, зависит от frontend-слоя. |

### Низкий / отложенный приоритет

| Фича | Ценность | Сложность | Причина |
|---|---:|---:|---|
| typicalPotentialAbv для AppleVariety | 4 | 4 | Может помочь каталогу, но есть риск ложной точности. |
| Standalone cider suitability | 5 | 6 | Идея интересная, но user story еще недостаточно зрелая. |
| Orchard-related характеристики | 3 | 7 | Не cider-core, лучше не смешивать без отдельного orchard-домена. |
| Ellipse/lasso selection на graph | 5 | 6 | Удобно, но rectangle selection даст основную пользу проще. |
| Полный REST error model для всех ошибок | 6 | 4 | Нужно для зрелости API, но само по себе не делает систему экспертной. |

### Рекомендуемый порядок движения

1. ~~Довести ABV prediction для `FeedstockSample`: response, method/confidence,
   source of assumed/predicted FG.~~
2. ~~Добавить season/batch и available volume через `FeedstockBatch`.~~
3. Добавить distance metric / target model для sugar-acidity profile.
4. Добавить MVP blending plan: predefined zone/custom rectangle target,
   normalized weighted distance, one optimized option, volume constraints.
5. Начать `ProductionBatch` как принятие blend/source plan:
   списание объемов, текущий объем, операции, первая fermentation phase.
6. Добавить минимальный yeast catalog как источник рабочих диапазонов и constraints.
7. Добавить первые rule-based alerts: temperature out of yeast range, SG не
   снижается, ABV near yeast tolerance.

## Сценарии и user stories

### Исследование каталога сортов через sugar-acidity graph

Как сидродел, исследующий каталог сортов, я хочу выделить область на
sugar-acidity graph и получить список сортов, попадающих в эту область, чтобы
найти кандидатов с нужным балансом sugar/acidity, а затем уточнить выбор через
классификации, tannin, полноту данных и другие фильтры.

Практическая польза:

- Найти сорта в интересующем диапазоне sugar/acidity, а не только в заранее
  заданной зоне.
- Искать сорта около границ зон, например рядом с `PERFECT_APPLES` или между
  medium/high sugar.
- Найти кандидатов рядом с известным сортом.
- Найти сорта, которые закрывают пустую область относительно имеющихся
  `FeedstockSample`.
- Использовать graph как инструмент исследования каталога, а не только
  визуализацию уже найденных сортов.

Важно: пользовательская область на графике - это не то же самое, что
`SugarAcidityZone`. `SugarAcidityZone` задается моделью/книгой, а plot
selection задается пользователем в момент исследования.

Идея реализации:

- Сначала поддержать rectangle selection, соответствующий drag-to-select на
  графике.
- Добавить в query object диапазоны координат: `sugarMin`, `sugarMax`,
  `acidityMin`, `acidityMax`.
- Применять selection к вычисленным координатам:
  `assessment.point().sugar().value()` и
  `assessment.point().acidity().value()`.
- Позволить комбинировать selection с фильтрами по classification system,
  zone, levels, completeness и name.

### Работа сидродела в привычной системе классификации

Как сидродел, привыкший к English/French/Spanish/Long Ashton терминам, я хочу
фильтровать сорта по привычной classification system, чтобы искать компоненты в
понятном мне языке профилей.

Идея реализации:

- Добавить фильтры `classificationSystem` и `classificationClass`.
- Начать с одной пары system/class в запросе, например
  `LONG_ASHTON_4 + BITTERSWEET` или `FRENCH + DOUCE_AMERE`.
- Позже решить, нужны ли multiple systems/classes в одном запросе.

Открытый вопрос: как унифицировать разные enum/class names между English,
French, Spanish и Long Ashton, чтобы transport не стал хрупким.

### Поиск сортов по роли в sugar-acidity graph

Как сидродел, я хочу искать сорта по роли в sugar-acidity graph, чтобы найти
кандидатов для конкретного технологического вклада: sugar-rich, low-acidity,
medium-sugar и т.д.

Идея реализации:

- ~~Фильтр `sugarAcidityZone`.~~
- Дополнительные фильтры `sugarLevel`, `acidityLevel`, `tanninLevel`.
- `tanninLevel` полезен даже для 2D graph, потому что сидродел может искать,
  например, low-acidity/high-tannin кандидатов.

### Быстрый поиск сорта в большом каталоге

Как сидродел, я хочу быстро найти конкретный сорт по названию, чтобы посмотреть
его chemical profile, положение на graph и классификации.

Идея реализации:

- ~~Фильтр `name`.~~
- ~~Поиск по нормализованному названию.~~
- Позже добавить поиск по синонимам и локальным названиям.

### Управление качеством каталога AppleVariety

Как администратор каталога, я хочу найти сорта с неполным chemical profile,
чтобы дополнить данные, проверить источники или исключить сорт из отдельных
представлений.

Идея реализации:

- Отчет по сортам с неполным chemical profile.
- Фильтры `capable`, `missingAxes`, возможно явный `capability`.
- Возможность увидеть, каких измерений не хватает для `SUGAR_ACIDITY_PLOT`,
  `NORTH_AMERICAN_PROFILE` или другой capability.
- Будущие статусы вроде "ожидает уточнения", "исключен из graph",
  "скрыт из публичного каталога".

### Постепенное заполнение FeedstockSample

Для `FeedstockSample` неполный профиль является нормальным рабочим состоянием.
Сидродел может сначала знать только sugar, затем добавить acidity, позже tannin
или pH.

User stories:

- ~~Как сидродел, я хочу создать sample с частичным набором измерений, чтобы
  начать вести данные до полного лабораторного профиля.~~
- ~~Как сидродел, я хочу видеть предупреждение, что sample пока нельзя
  отобразить на sugar-acidity plot.~~
- ~~Как сидродел, я хочу понимать, каких измерений не хватает для конкретного
  графика или оценки.~~
- ~~Как сидродел, я хочу фильтровать samples по полноте данных, сезону, партии
  или cider maker.~~

### Сезоны и партии для FeedstockSample

Для `FeedstockSample` нужно предусмотреть возможность указывать сезон и партию.
Это важно для выборки точек на графике: химический профиль сырья зависит не
только от сорта, но и от конкретного урожая, поставки, хранения и момента
измерения.

Возможные характеристики:

- ~~Сезон урожая.~~
- ~~Партия или batch.~~
- ~~Cider maker / владелец сырья.~~
- Дата сбора.
- Дата прессования или измерения.
- Условия хранения перед измерением.
- ~~Источник сырья: свой сад, поставщик, ферма, рынок.~~

~~Открытый вопрос: что именно является season и batch в доменной модели, нужны ли
отдельные value objects или aggregate для партии.~~ Решено через отдельный
aggregate `FeedstockBatch` и value object `FeedstockSeason`.

### Возможные характеристики AppleVariety в cider-домене

Для `AppleVariety` стоит отделять устойчивые сортовые характеристики от
характеристик конкретного урожая или партии. В контексте cider-домена здесь
нужны признаки, которые помогают оценивать сидровой потенциал сорта, искать
кандидатов для graph и приводить chemical profile к локальным терминам.

Возможные характеристики:

- Синонимы и локальные названия.
- Страна, регион или сидровая традиция происхождения.
- Сезон созревания: ранний, средний, поздний.
- Рекомендуемый период хранения перед прессованием.
- Основное назначение в контексте сидроделия: single-variety cider, blending,
  fresh juice, cooking cider, vinegar, distillation.
- Cider classification по локальным системам: English, French, Spanish,
  North American.
- Типичный sugar-acidity zone.
- Типичный диапазон sugar, acidity, tannin и pH.
- Надежность chemical profile: единичное измерение, диапазон, статистика,
  источник данных.
- Описание вкусового/технологического вклада в сидр: body, sharpness,
  bitterness/astringency, aroma intensity.
- Роль в профиле сидра: sugar contributor, acid contributor, tannin
  contributor, balancing apple.

Характеристики вроде урожайности, силы роста дерева, времени цветения,
опылителей, устойчивости к болезням, зимостойкости и пригодности к климату
скорее относятся к orchard-домену. Их не стоит смешивать с cider-моделью
`AppleVariety`, пока в системе явно не появится область планирования сада.

Низкий приоритет: рассмотреть `typicalPotentialAbv` для `AppleVariety`.
Это должно быть не производственным прогнозом, а справочной оценкой по
типичному chemical profile сорта. Для сорта нельзя честно обещать конкретный
ABV, потому что реальная крепость зависит от сезона, климата, зрелости,
хранения и конкретного `FeedstockSample`.

Возможная форма:

```json
{
  "varietyId": "...",
  "varietyName": "Kingston Black",
  "typicalPotentialAbv": {
    "min": 6.1,
    "max": 7.3,
    "basis": "TYPICAL_CHEMICAL_PROFILE"
  }
}
```

User story для будущего обсуждения:

> Как сидродел, исследующий каталог сортов, я хочу примерно понимать
> потенциальную крепость сидра по типичному профилю сорта, чтобы сравнивать
> кандидатов для закупки, посадки или исследования.

### Standalone cider suitability

Идею `SINGLE_VARIETY_CIDER_ASSESSMENT` пока не реализуем. Нужно дополнительно
понять практическую user story, чтобы функционал не стал академической оценкой
"идеальности сорта" без явной пользы для сидродела.

Возможная полезная постановка:

> Как сидродел, у меня есть партия яблок или сока с измеренными sugar, acidity
> и tannin. Я хочу понять, стоит ли ферментировать эту партию отдельно как
> самостоятельный сидр, или лучше сразу планировать ее как компонент для
> купажа.

В таком виде сценарий может быть полезнее не только для `AppleVariety`, но и
для `FeedstockSample`, потому что сидродел принимает решение по конкретной
партии сырья, сезону и измеренному профилю.

Возможные вопросы:

- Можно ли ожидать сбалансированный самостоятельный сидр без купажа.
- Какие риски есть у профиля: низкая кислотность, высокая кислотность, низкий
  tannin, чрезмерная tannin/astringency, недостаточный sugar.
- Какую роль профиль может играть в купаже.
- Стоит ли держать партию отдельно или сразу искать blending companion.

Возможные будущие названия:

- `STANDALONE_CIDER_SUITABILITY`
- `SINGLE_LOT_CIDER_SUITABILITY`

Минимальные требования к данным: sugar, acidity и tannin. Но результат не
должен быть простым boolean. Нужны объяснение, риски и рекомендуемая роль
профиля.

### FeedstockSample graph exploration

Пока подробно не обсуждали. Похожий discovery-сценарий нужен не только для
каталога сортов, но и для samples конкретного сидродела.

Возможные user stories:

- Как сидродел, я хочу увидеть samples за сезон или партию на graph, чтобы
  сравнить сырье между собой.
- Как сидродел, я хочу сравнить мои `FeedstockSample` с каталогом
  `AppleVariety`, чтобы понять, на какие сортовые профили похожа партия.
- Как сидродел, я хочу найти сорта или samples, которые могут дополнить текущую
  партию по acidity/tannin/sugar.

### Blending для FeedstockSample

Будущая большая область: подбор купажа из имеющихся `FeedstockSample`.
Сидродел работает не с абстрактными сортами, а с конкретными партиями сырья,
у каждой из которых есть chemical profile и ограниченный доступный объем.

Возможные user stories:

- Как сидродел, я хочу выбрать несколько `FeedstockSample` и задать целевую
  область на sugar-acidity graph, чтобы система предложила пропорции купажа,
  максимально приближающие итоговый профиль к цели.
- Как сидродел, я хочу задать target не только через готовую зону, но и через
  диапазоны значений sugar/acidity/tannin, чтобы подбирать купаж под конкретный
  стиль или технологическую задачу.
- Как сидродел, я хочу учитывать доступный объем каждой партии, чтобы система
  не предлагала использовать больше сырья, чем реально есть.
- Как сидродел, я хочу видеть несколько вариантов купажа, а не один ответ,
  чтобы выбрать между точностью попадания в цель, расходом редкого сырья и
  практичностью производства.
- Как сидродел, я хочу понимать, почему предложенный купаж не попал точно в
  цель, если доступные samples физически не позволяют достичь нужного профиля.
- Как сидродел, я хочу принять предложенный blend plan и создать из него
  производственную партию, чтобы система автоматически списала объемы из всех
  участвующих сырьевых партий.

Возможные способы задания target:

- Готовая `SugarAcidityZone`, например `PERFECT_APPLES` или
  `MEDIUM_SUGAR_APPLES`.
- Пользовательская rectangle selection на graph.
- Custom rectangle через API, заданный технологом:
  `sugarMin`, `sugarMax`, `acidityMin`, `acidityMax`.
- Явные диапазоны значений:
  - sugar/SG min-max;
  - acidity/TA min-max;
  - tannin min-max;
  - позже pH или другие показатели.
- Центр целевой области как target point для оптимизации.

Ключевое доменное требование: в `FeedstockSample` нужно добавить объем
доступного сырья. Без этого blending-алгоритм сможет предложить математически
красивые, но физически невозможные пропорции.

Возможные характеристики объема:

- `availableVolume`;
- unit: liters/gallons, позже нужно решить единицы и конвертации;
- возможно distinction между measured volume, usable volume и reserved volume;
- дата актуальности объема, если объем может уменьшаться после использования.

### ProductionBatch как производственная партия

`FermentationBatch` как главный aggregate слишком узкий термин. Ферментация -
это важная фаза производства, но не вся жизнь партии напитка. Полный процесс
может включать старт ферментации, снятие с осадка, перелив, выдержку,
вторичную ферментацию, кивинг, купажирование, розлив, дображивание в бутылке,
дегоржаж, dosage и другие операции в зависимости от технологии и типа продукта.

Более устойчивое доменное понятие:

- `ProductionBatch`: живая производственная партия напитка;
- `Fermentation`: фаза/процесс внутри производственной партии;
- `ProductionOperation`: факт технологической операции над партией.

`ProductionBatch` должен быть beverage-neutral. Он может описывать будущий
сидр, перри, основу для дистиллята/кальвадоса, помо или другой продукт. Тип
продукта должен быть отдельным атрибутом/intent, например:

- `CIDER`;
- `PERRY`;
- `DISTILLATE_BASE`;
- `CALVADOS`;
- `POMMEAU`;
- `OTHER`.

Это не должны быть отдельные aggregate roots на первом этапе. Различия между
продуктами лучше выражать через `productType`, production profile/policy и
доступные операции.

`FeedstockBatch` представляет доступный запас сырья/сока до производства.
`ProductionBatch` представляет часть этого сырья, которая уже ушла в
производственный процесс. При создании `ProductionBatch` объем должен быть
зарезервирован или списан из `FeedstockBatch.availableVolume`.

Риск реализации: сложность находится не в создании сущности, а в интеграции с
учетом сырья и конкурентным доступом к объему. Production batch может
создаваться из одного `FeedstockBatch`, нескольких feedstock batches или
результата blending optimization. Поэтому production-модель не должна
начинаться раньше, чем появится минимальный `BlendPlan` / `ProductionSourcePlan`.

Зависимость roadmap: сначала нужны distance function и target model, затем
blending algorithm, который строит source plan, и только потом
`ProductionBatch`, который принимает этот source plan и атомарно списывает
объемы из участвующих `FeedstockBatch`.

Требования к резервированию/списанию объема:

- создание production batch и уменьшение/reservation объемов по всем source
  batches должны быть атомарны в одной transaction boundary;
- `FeedstockBatch.availableVolume` должен защищаться optimistic locking, чтобы
  параллельные создания production batches не могли использовать один и тот же
  объем дважды;
- нужно явно решить модель: сразу списывать объем или резервировать его до
  подтверждения `START_PRODUCTION`;
- если выбран reserve-flow, нужны операции cancel/release reservation и
  возможно change reserved volume;
- недостаточный объем в любом source batch должен возвращать application-level error, а не падать
  поздно в persistence layer.

Важное правило: поле внутри `ProductionBatch` не должно называться
`availableVolume`. Это уже не доступный запас, а текущий объем партии в
процессе. Лучше использовать:

- `currentVolume`;
- `initialVolume`;
- позже возможно `usableVolume`, если нужно отдельно учитывать технологические
  потери.

User stories:

- Как производитель, я хочу взять часть объема из `FeedstockBatch` и запустить
  из нее производственную партию, чтобы отделить сырье в запасе от продукта в
  процессе.
- Как сидродел, я хочу видеть текущий объем партии, чтобы понимать, сколько
  продукта реально осталось после операций и потерь.
- Как сидродел, я хочу фиксировать снятие с осадка, переливы и розлив, чтобы
  сохранять историю технологических решений.
- Как сидродел, я хочу видеть, из какого сырья или blend-а была создана партия,
  чтобы анализировать результат по исходным профилям.
- Как производитель, я хочу вести разные технологии производства, не ломая
  базовую модель партии: тихий сидр, perry, pet-nat, ancestral, keeving,
  traditional method, distillate base/calvados, pommeau.

#### Снятие с осадка / racking

Снятие с осадка - это операция внутри той же производственной партии, если
идентичность продукта сохраняется. Новый batch создавать не нужно, когда партия
просто переливается в другую емкость и теряет часть объема с осадком.

Пример:

```text
ProductionBatch A
currentVolume: 80 L
vessel: Tank 1

Operation: RACKING
inputVolume: 80 L
outputVolume: 74 L
lossVolume: 6 L
lossReason: LEES
targetVessel: Tank 2

ProductionBatch A
currentVolume: 74 L
vessel: Tank 2
```

Новый `ProductionBatch` нужен, когда меняется идентичность партии:

- партия разделена на две независимые части;
- несколько партий смешаны в новый blend;
- часть партии ушла на отдельную технологию;
- часть разлита, а часть оставлена на выдержку;
- часть партии получила отдельные добавки/дрожжи/режим и дальше живет отдельно.

#### ProductionOperation

Операции лучше моделировать как историю фактов, а не только как изменение
полей aggregate. Это позволит позже объяснять пользователю, почему текущий
объем именно такой и какие технологические действия привели к результату.

Минимальная модель операции:

```java
record ProductionOperation(
    ProductionOperationId id,
    ProductionOperationType type,
    Instant performedAt,
    VolumeValue inputVolume,
    VolumeValue outputVolume,
    VolumeValue lossVolume,
    String note
) {
}
```

Минимальные типы операций:

- `START_PRODUCTION`;
- `START_FERMENTATION`;
- `MEASUREMENT`;
- `RACKING`;
- `TRANSFER`;
- `VOLUME_ADJUSTMENT`;
- `SAMPLE_TAKEN`;
- `BOTTLING`.

`VOLUME_ADJUSTMENT` нужен как явная операция коррекции текущего объема, чтобы
учетный объем не расходился с физическим. Причины должны быть типизированы:

- `EVAPORATION`;
- `SAMPLING`;
- `SPILL`;
- `RACKING_LOSS`;
- `MEASUREMENT_CORRECTION`;
- `OTHER`.

Будущие типы:

- `KEEVING`;
- `BLENDING`;
- `SECONDARY_FERMENTATION`;
- `BOTTLE_CONDITIONING`;
- `RIDDLING`;
- `DISGORGING`;
- `DOSAGE`;
- `STABILIZATION`;
- `SULFITE_ADDITION`;
- `NUTRIENT_ADDITION`.

#### Технологические профили

Разные стили производства можно рассматривать как технологические профили или
маршруты, но не нужно зашивать их слишком рано в жесткий enum workflow.
Сидродел может следовать классической технологии или творчески применять
операции в нестандартном порядке.

Возможная будущая модель:

- `ProductionProfile`: рекомендуемый/ожидаемый набор фаз и операций;
- `ProductionPhase`: fermentation, maturation, bottle conditioning и т.д.;
- `AllowedOperationPolicy`: какие операции допустимы в текущем состоянии;
- `ProductionPlan`: планируемый маршрут;
- `ProductionTimeline`: фактическая история операций и измерений.

На первом этапе лучше поддержать свободную историю операций с базовыми
инвариантами:

- нельзя списать из `FeedstockBatch` больше доступного объема;
- операция не может увеличить объем без явного источника добавления;
- `outputVolume + lossVolume` не должен превышать `inputVolume`, если операция
  не содержит добавление;
- текущий объем партии не может быть отрицательным;
- завершенную или разлитую партию нельзя менять обычными технологическими
  операциями без отдельной correction/audit-модели.

Доменные события для future audit/notifications/integration:

- `FeedstockVolumeReserved`;
- `FeedstockVolumeReservationReleased`;
- `ProductionBatchStarted`;
- `FermentationStarted`;
- `ProductionOperationRecorded`;
- `ProductionBatchVolumeAdjusted`;
- `ProductionBatchSplit`;
- `ProductionBatchBlended`.

Тестирование первого slice:

- unit tests на инварианты объема;
- unit tests на операции `START_PRODUCTION`, `RACKING`, `VOLUME_ADJUSTMENT`,
  `SAMPLE_TAKEN`;
- application tests на атомарное создание production batch и изменение
  `FeedstockBatch.availableVolume`;
- persistence/concurrency test на optimistic locking для параллельного
  списания/reservation одного объема.

### Fermentation forecast and monitoring

Будущая большая область: прогноз и сопровождение ферментации по партиям.
Здесь `ProductionBatch` находится в fermentation phase: у него есть исходный
chemical profile, выбранные дрожжи, температурный режим и последовательность
измерений во времени.

Возможные user stories:

- Как сидродел, я хочу получить прогноз ABV по доступному SG/сахару в сырье,
  чтобы заранее понимать ожидаемую крепость будущего сидра.
- Как сидродел, я хочу отслеживать процесс ферментации по SG, pH, acidity,
  температуре и другим показателям, чтобы видеть динамику партии во времени.
- Как сидродел, я хочу видеть fermentation batch на графиках, чтобы понимать,
  как быстро снижается SG, как меняется температура и не выходит ли процесс за
  безопасные диапазоны.
- Как сидродел, я хочу получать предупреждения о риске остановки ферментации,
  чтобы успеть вмешаться до потери партии.
- Как сидродел, я хочу получать предупреждения о признаках возможного заражения
  или другого отклонения процесса, если chemical/temperature динамика выглядит
  подозрительно.
- Как сидродел, я хочу вести наблюдения по партиям, чтобы сравнивать разные
  сезоны, samples, blends, дрожжи и температурные режимы.

Минимальные сущности/понятия для обсуждения:

- `ProductionBatch`: производственная партия.
- `FermentationState`: состояние фазы ферментации внутри партии.
- Исходное сырье: один `FeedstockBatch`, один `FeedstockSample` или blend
  нескольких samples/batches.
- Стартовый SG/sugar и прогнозируемый ABV.
- Измерения во времени: SG, temperature, pH, acidity, возможно tannin,
  nutrients, notes.
- Состояние процесса: active, slowing, stuck risk, completed, abnormal.
- Связь с выбранными yeast strain.

### Yeast catalog

Будущая база дрожжей и их характеристик. Характеристики дрожжей нужны не как
справочник ради справочника, а как входные данные для прогноза ферментации,
контроля процесса и предупреждений.

Возможные user stories:

- Как сидродел, я хочу выбрать дрожжи для fermentation batch, чтобы система
  могла учитывать их рабочие диапазоны и ограничения.
- Как сидродел, я хочу видеть, подходит ли выбранный yeast strain для текущего
  профиля сырья: pH, expected ABV, sugar level, temperature plan.
- Как сидродел, я хочу получать предупреждение, если текущая температура,
  pH или ABV приближается к пределам выбранных дрожжей.
- Как сидродел, я хочу сравнивать дрожжи по tolerance и recommended ranges,
  чтобы выбрать strain под конкретный стиль или условия ферментации.

Возможные характеристики yeast strain:

- Производитель и название.
- Тип: cider, wine, ale, wild/mixed culture и т.д.
- Recommended temperature range.
- Alcohol tolerance / max ABV.
- pH tolerance или recommended pH range.
- Attenuation / expected dryness, если применимо.
- Nutrient requirements.
- Fermentation speed.
- Aroma/flavor descriptors.
- Flocculation/sedimentation behavior.
- SO2 tolerance, если важно.
- Notes по применению.

## Архитектурные и технические идеи

### ~~Query/filter model для apple variety graph batch~~

~~Сейчас batch endpoint для sugar-acidity graph концептуально остается операцией
"дай все". Для большого каталога сортов это не подходит. Нужно ввести явную
query model, например `AppleVarietySugarAcidityZonePointQuery`, и cursor-based
pagination.~~

Возможная форма query object:

```java
record AppleVarietySugarAcidityZonePointQuery(
    Optional<String> name,
    Optional<Boolean> capable,
    Set<ChemicalAxis> missingAxes,

    Optional<SugarAcidityZone> sugarAcidityZone,
    Optional<SugarAcidityLevel> sugarLevel,
    Optional<SugarAcidityLevel> acidityLevel,
    Optional<SugarAcidityLevel> tanninLevel,

    Optional<AppleVarietyClassificationSystem> classificationSystem,
    Optional<String> classificationClass,

    Optional<SugarAcidityPlotSelection> plotSelection,

    int limit,
    Optional<String> cursor
) {
}
```

### ~~GET vs POST для search~~

~~GET удобен для простых read-only фильтров: URL можно сохранить, открыть заново
и проще отлаживать. Но исследовательский search быстро становится сложным:
plot selection, массивы classes, levels, cursor, future shapes.~~

Идея:

- ~~Оставить простой `GET /apple-varieties/sugar-acidity-zone-points` для default
  сценария или простых фильтров.~~
- ~~Основной исследовательский endpoint сделать как
  `POST /apple-varieties/sugar-acidity-zone-points/search` с request body.~~
- ~~В application layer использовать одну query model независимо от transport.~~

### ~~Cursor pagination~~

~~Курсор нужно добавить сразу. Он должен быть opaque для клиента. Минимальная
идея: Base64 JSON с последней позицией стабильной сортировки.~~

```json
{
  "lastName": "Kingston Black",
  "lastId": "..."
}
```

~~Сортировка по умолчанию: `normalizedName + varietyId`.~~

~~Ответ batch/search endpoint-а должен содержать page metadata:~~

```json
{
  "points": [],
  "unplottable": [],
  "page": {
    "limit": 100,
    "nextCursor": "...",
    "hasNext": true
  }
}
```

### Вычислимые фильтры и projection/index

Часть фильтров лежит в данных напрямую: name, later synonyms/origin/tradition.
Часть фильтров вычислимая: sugar-acidity zone, sugar/acidity/tannin levels,
capable, missingAxes, локальные cider classifications, координаты plot
selection.

На первом этапе можно применять вычислимые фильтры в application layer. Но при
большом каталоге это может конфликтовать с cursor pagination: из первых N
кандидатов после фильтра может остаться слишком мало результатов.

Возможные решения:

- ~~overfetch: читать больше кандидатов, пока не набран `limit` результатов или
  каталог не закончился;~~
- позже сделать materialized projection/index для вычисленных профилей,
  координат graph и классификаций;
- держать query model стабильной, чтобы перенос фильтров из application layer в
  persistence/projection слой не ломал API.

### ~~Управление полнотой и актуальностью chemical profile для graph/capability scenarios~~

Неполный `ChemicalProfile` является допустимым состоянием данных. Система должна
позволять загружать и хранить профили, в которых отсутствуют отдельные
измерения: sugar, acidity, tannin, pH или другие будущие показатели.

Важно не вводить понятие "полный профиль" как универсальный флаг. Один и тот же
`ChemicalProfile` может быть пригоден для одной задачи и непригоден для другой.
Нужно мыслить через capability assessment: оценку пригодности профиля для
конкретного сценария.

~~Примеры capabilities:~~

- ~~`SUGAR_ACIDITY_PLOT`: требует sugar и acidity.~~
- ~~`NORTH_AMERICAN_PROFILE`: требует sugar, acidity и tannin.~~
- ~~`SINGLE_VARIETY_CIDER_ASSESSMENT`: требует sugar, acidity и tannin, но
  сценарий пока не реализуем.~~
- `CIDER_CLASSIFICATION`: требования зависят от выбранной классификационной
  системы.

Форма модели:

```java
record ChemicalProfileCapabilityAssessment(
    ChemicalProfileCapability capability,
    boolean capable,
    Set<ChemicalProfileMeasurement> required,
    Set<ChemicalProfileMeasurement> present,
    Set<ChemicalProfileMeasurement> missing
) {
}
```

~~Для batch endpoint-а graph полезно возвращать не только отображаемые точки, но
и элементы, которые нельзя отобразить:~~

```json
{
  "points": [],
  "unplottable": [
    {
      "subjectId": "...",
      "subjectType": "FEEDSTOCK_SAMPLE",
      "displayName": "Late season batch A",
      "capability": "SUGAR_ACIDITY_PLOT",
      "requiredMeasurements": ["SUGAR", "ACIDITY"],
      "presentMeasurements": ["SUGAR"],
      "missingMeasurements": ["ACIDITY"]
    }
  ]
}
```

~~`SugarAcidityZoneAssessor` не должен решать задачу полноты данных. Его
ответственность - оценить профиль, который уже пригоден для sugar-acidity
assessment. Проверка пригодности профиля должна находиться перед assessor-ом, в
отдельной доменной или application service модели.~~

### REST error model

~~Сейчас добавлен `422 ProblemDetail` для `ChemicalProfileCapabilityNotMet`.~~
Нужно отдельно обсудить общий error strategy:

- ~~not found для `FeedstockBatchNotFoundException`;~~
- validation errors;
- unsupported classification system;
- malformed query/search body;
- ~~malformed cursor;~~
- ~~capability not met;~~
- consistent `type`, `title`, `detail`, `properties`.

Выполнено техническое разделение REST exception handlers:

- ~~`ChemicalProfileExceptionHandler` для capability errors;~~
- ~~`FeedstockExceptionHandler` для feedstock-specific errors;~~
- ~~`QueryExceptionHandler` для cursor/query errors.~~

### Classification filter contract

Нужно решить transport для фильтрации по локальным cider classifications.

Открытые вопросы:

- Принимать одну пару `classificationSystem + classificationClass` или массив.
- Как валидировать несовместимые пары system/class.
- Нужен ли общий словарь class codes поверх enum-ов конкретных систем.
- Как возвращать ошибку, если class не существует в выбранной системе.

### Frontend graph contract

Нужно отдельно обсудить финальный контракт для graph UI:

- один endpoint или несколько;
- metadata caching;
- shape rendering;
- selected area;
- tooltips;
- enriched list рядом с graph;
- как связывать graph points и list rows.

### Blending optimization model

Для будущего blending по `FeedstockSample` нужна отдельная application/domain
модель оптимизационной задачи.

Идея:

- Вход: набор доступных `FeedstockSample`, их chemical profile и доступный
  объем.
- Target: predefined sugar-acidity zone, пользовательская область graph или
  custom rectangle/ranges через API.
- Целевая функция: минимизировать расстояние итогового профиля купажа до центра
  target area или до заданного target point.
- Ограничения:
  - доля каждого sample неотрицательная;
  - используемый объем sample не превышает `availableVolume`;
  - итоговый объем может быть задан или вычисляться;
  - samples с неполным профилем нельзя использовать для target, который требует
    отсутствующие измерения.
- Выход: один или несколько вариантов купажа с пропорциями, итоговым расчетным
  chemical profile, расстоянием до цели и объяснением ограничений.

Открытые технические вопросы:

- Какие показатели смешиваются линейно, а какие требуют осторожности.
- Нужно ли учитывать uncertainty/range/statistical measurements при
  оптимизации.
- Как учитывать tannin и pH, если target задан только на 2D sugar-acidity
  graph.
- Как ранжировать несколько решений: точность, экономия редкого сырья,
  минимальное число компонентов, максимальный итоговый объем.
- Нужна ли внешняя optimization library или достаточно собственного простого
  solver-а на первом этапе.

MVP blending должен быть уже и проще полной задачи:

- target model поддерживает два варианта:
  - predefined `SugarAcidityZone` из уже реализованных зон;
  - custom rectangle `[sugarMin..sugarMax, acidityMin..acidityMax]`, заданный
    технологом через API;
- distance: взвешенная евклидова метрика с нормализацией шкал sugar/acidity;
- tannin добавить позже как третью координату/constraint;
- shape support для ellipse/polygon/lasso не включать в первый solver;
- результат: сначала один оптимизированный вариант;
- алгоритм: простой bounded search / discretized proportions с отсечением по
  available volume;
- multiple ranked options добавить после доказанного MVP.

Выход blending MVP должен быть не только текстовой рекомендацией, а
структурированным `BlendPlan` / `ProductionSourcePlan`:

- source `FeedstockBatch` / `FeedstockSample`;
- requested volume per source;
- expected blended chemical profile;
- target distance;
- target source: predefined zone или custom rectangle;
- total volume;
- warnings, если target недостижим или использованы profiles с разной
  confidence.

Этот plan позже должен становиться входом для создания `ProductionBatch`.
Именно поэтому blending/distance стоят перед production foundation.

Открытые вопросы для более поздней версии:

- поддержка target ellipse/polygon/zone shape;
- Mahalanobis или другая метрика с учетом covariance/uncertainty;
- учет range/statistical measurements;
- explainability для trade-off между точностью цели и расходом редкого сырья.

### Graph export

Низковисящий сценарий для graph discovery: экспорт отфильтрованных результатов
в CSV/Excel.

User stories:

- Как сидродел, я хочу выгрузить отфильтрованные точки graph в CSV/Excel,
  чтобы анализировать их вне системы или делиться подборкой.
- Как администратор каталога, я хочу экспортировать выборку сортов с неполными
  профилями, чтобы искать недостающие данные и сверять источники.

Идея реализации:

- использовать тот же search/query object, что и для graph points;
- отдельный endpoint export, возвращающий CSV на первом этапе;
- поля: id, name, sugar/SG, acidity, tannin, zone, capability status,
  missing axes, source/batch/season для feedstock;
- ценность: 7;
- сложность: 3-4.

### Fermentation model and alerts

Для прогноза и мониторинга ферментации нужна отдельная модель процесса, которая
хранит не только исходный profile, но и временной ряд измерений.

Идеи реализации:

- Не создавать `FermentationBatch` как главный aggregate. Главный aggregate -
  `ProductionBatch`, а fermentation - фаза и набор операций
  внутри него.
- Связать production batch с исходным `FeedstockBatch`, `FeedstockSample` или
  blend recipe.
- Хранить time series measurements:
  - SG/sugar;
  - temperature;
  - pH;
  - acidity;
  - optional notes/events/interventions.
- Рассчитывать expected ABV по starting SG и текущему/final SG.
- Отображать fermentation progress на графиках:
  - SG over time;
  - temperature over time;
  - pH/acidity over time;
  - projected ABV.
- Добавить rule-based alerts на первом этапе:
  - SG перестал снижаться дольше заданного времени;
  - temperature вышла за recommended yeast range;
  - pH вышел за безопасный или yeast-specific range;
  - expected/current ABV приближается к alcohol tolerance yeast strain;
  - резкие изменения показателей выглядят подозрительно.
- Позже заменить или дополнить rule-based alerts статистической моделью по
  историческим production batches.

Минимальный yeast catalog лучше добавить до содержательных alerts. Без yeast
metadata alerts смогут проверять только generic thresholds, но не рабочий
диапазон конкретных дрожжей.

Минимальная модель `YeastStrain` для первой версии:

- `id`;
- `name`;
- `temperatureRange`;
- `phRange`;
- `alcoholTolerance`;
- optional `producer`.

Открытые вопросы:

- Как часто сидродел реально будет вносить измерения.
- Какие показатели обязательны, а какие опциональны.
- ABV prediction всегда должен работать по нормализованному SG. Brix/Plato
  могут быть входными форматами sugar domain, но до fermentation/ABV слоя они
  должны быть приведены к SG.
- Как учитывать температурные единицы.
- Какие warning-и должны быть hard alerts, а какие advisory hints.
- Как хранить intervention events: nutrient addition, temperature correction,
  racking, yeast pitching, restart attempt.

### ABV prediction strategies

ABV prediction должен работать по нормализованному SG. Brix/Plato могут быть
входными форматами sugar domain, но до fermentation/ABV слоя они должны быть
приведены к `SpecificGravityValue`.

Текущий базовый вариант:

- ~~`STANDARD_GRAVITY_DROP`: расчет по OG и predicted/assumed FG.~~

Будущие стратегии:

- `YEAST_AWARE`: прогноз учитывает выбранный yeast strain, его alcohol
  tolerance, expected attenuation, recommended temperature range, pH range и
  nutrient requirements. Такая стратегия может влиять прежде всего на
  predicted FG и риск недоброда.
- `FERMENTATION_MODEL_ADJUSTED`: прогноз учитывает параметры fermentation
  batch: температурный режим, pH, исходный chemical profile, выбранные дрожжи,
  планируемые или фактические условия процесса.
- `FERMENTATION_MEASUREMENTS_BASED`: более сложная модель на основе временного
  ряда измерений fermentation batch. Может уточнять expected final ABV по
  динамике SG, temperature, pH/acidity и событиям процесса.

Открытые вопросы:

- Будут ли это разные `AbvPredictionStrategy` или разные стратегии расчета FG,
  используемые одной ABV formula.
- Как выбирать стратегию: configuration, request parameter, yeast/batch
  context или автоматический selection по доступным данным.
- Как показывать пользователю confidence прогноза: baseline estimate,
  yeast-aware estimate, in-progress adjusted estimate.
- Как учитывать incomplete data: если нет yeast или time series, использовать
  fallback к `STANDARD_GRAVITY_DROP`.

### Yeast characteristics model

База дрожжей должна быть связана с fermentation forecast/monitoring, а не жить
изолированным справочником.

Идеи реализации:

- Создать `YeastStrain` model/catalog.
- Хранить рабочие диапазоны:
  - temperature min/max;
  - pH min/max или recommended range;
  - alcohol tolerance;
  - nutrient requirements.
- Использовать yeast characteristics при создании fermentation batch:
  - проверить совместимость сырья и выбранных дрожжей;
  - рассчитать риск превышения ABV tolerance;
  - настроить alert thresholds для конкретной партии.
- Использовать yeast characteristics в мониторинге:
  - temperature out of range;
  - pH out of range;
  - ABV near tolerance;
  - fermentation speed отличается от expected profile.
- Позже добавить пользовательские yeast strains и overrides, потому что
  сидродел может работать с локальными или смешанными культурами.

### CompositionRoot

~~Открытый архитектурный вопрос: куда девать `CompositionRoot`. Нужно отдельно
решить, остается ли это отдельным application/bootstrap слоем, частью
configuration, или исчезает в пользу явной Spring configuration.~~

Решено через явные Spring configuration classes:

- ~~`SharedKernelConfiguration`;~~
- ~~`CiderAnalysisConfiguration`;~~
- ~~`FermentationConfiguration`.~~
