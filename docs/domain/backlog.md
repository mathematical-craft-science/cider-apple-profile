# Доменный backlog

## Приоритизация

Шкалы:

- Ценность: 1-10, насколько фича помогает сидроделу принимать практические
  решения.
- Сложность: 1-10, доменная и техническая сложность реализации.

### Высший приоритет

| Фича | Ценность | Сложность | Причина |
|---|---:|---:|---|
| FeedstockSample graph + warnings по неполному профилю | 9 | 4 | Работа с реальным сырьем, явные предупреждения и понимание, чего не хватает для оценки. |
| Potential ABV для FeedstockSample | 9 | 3 | Быстро отвечает на практический вопрос: какую крепость ожидать от конкретной партии. |
| Fermentation monitoring по партиям | 10 | 8 | Превращает систему в помощника, который следит за процессом и показывает динамику. |
| Alerts: stuck fermentation / выход за temperature/pH/ABV ranges | 10 | 8 | Помогает не потерять партию и вовремя вмешаться в процесс. |
| Yeast catalog + yeast-aware fermentation checks | 9 | 7 | Дрожжи влияют на FG/ABV, температурные риски, остановку ферментации и мониторинг. |

### Следующий сильный слой

| Фича | Ценность | Сложность | Причина |
|---|---:|---:|---|
| Blending для FeedstockSample с target area | 10 | 9 | Сильный decision support: подбор пропорций купажа под цель с учетом ограничений. |
| Available volume для FeedstockSample | 8 | 4 | Необходимая основа для blending и production planning. |
| Исследование каталога через выделенную область на graph | 8 | 6 | Делает каталог discovery-инструментом, а не статичным справочником. |
| Фильтрация по English/French/Spanish/Long Ashton classifications | 8 | 5 | Позволяет сидроделу работать в привычной системе терминов. |
| Query/filter/search model с cursor | 7 | 6 | Технический enabler для больших каталогов, graph exploration и стабильного API. |

### Средний приоритет

| Фича | Ценность | Сложность | Причина |
|---|---:|---:|---|
| Admin data-quality reports по AppleVariety | 7 | 5 | Важно для качества каталога, но косвенно полезно сидроделу. |
| Materialized projection/index для вычислимых профилей | 7 | 8 | Понадобится при росте каталога и сложных фильтрах, но можно отложить до нагрузки. |
| FeedstockSample season/batch model | 8 | 5 | Нужна для работы по сезонам и партиям, требует аккуратной доменной модели. |
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

1. Довести ABV prediction для `FeedstockSample`: response, method/confidence,
   source of assumed/predicted FG.
2. Добавить season/batch и available volume в `FeedstockSample`.
3. Начать `FermentationBatch`: source sample/blend, yeast optional, starting
   SG, measurements over time.
4. Добавить yeast catalog как источник рабочих диапазонов и constraints.
5. Добавить первые rule-based alerts: temperature out of yeast range, SG не
   снижается, ABV near yeast tolerance.
6. После этого переходить к blending optimization.

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

- Фильтр `sugarAcidityZone`.
- Дополнительные фильтры `sugarLevel`, `acidityLevel`, `tanninLevel`.
- `tanninLevel` полезен даже для 2D graph, потому что сидродел может искать,
  например, low-acidity/high-tannin кандидатов.

### Быстрый поиск сорта в большом каталоге

Как сидродел, я хочу быстро найти конкретный сорт по названию, чтобы посмотреть
его chemical profile, положение на graph и классификации.

Идея реализации:

- Фильтр `name`.
- Поиск по нормализованному названию.
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

- Как сидродел, я хочу создать sample с частичным набором измерений, чтобы
  начать вести данные до полного лабораторного профиля.
- Как сидродел, я хочу видеть предупреждение, что sample пока нельзя
  отобразить на sugar-acidity plot.
- Как сидродел, я хочу понимать, каких измерений не хватает для конкретного
  графика или оценки.
- Как сидродел, я хочу фильтровать samples по полноте данных, сезону, партии
  или cider maker.

### Сезоны и партии для FeedstockSample

Для `FeedstockSample` нужно предусмотреть возможность указывать сезон и партию.
Это важно для выборки точек на графике: химический профиль сырья зависит не
только от сорта, но и от конкретного урожая, поставки, хранения и момента
измерения.

Возможные характеристики:

- Сезон урожая.
- Партия или batch.
- Cider maker / владелец сырья.
- Дата сбора.
- Дата прессования или измерения.
- Условия хранения перед измерением.
- Источник сырья: свой сад, поставщик, ферма, рынок.

Открытый вопрос: что именно является season и batch в доменной модели, нужны ли
отдельные value objects или aggregate для партии.

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

Возможные способы задания target:

- Готовая `SugarAcidityZone`, например `PERFECT_APPLES` или
  `MEDIUM_SUGAR_APPLES`.
- Пользовательская rectangle selection на graph.
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

### Fermentation forecast and monitoring

Будущая большая область: прогноз и сопровождение ферментации по партиям.
Здесь `FeedstockSample` или blend превращается в fermentation batch, у которого
есть исходный chemical profile, выбранные дрожжи, температурный режим и
последовательность измерений во времени.

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

- `FermentationBatch`: партия ферментации.
- Исходное сырье: один `FeedstockSample` или blend нескольких samples.
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

### Query/filter model для apple variety graph batch

Сейчас batch endpoint для sugar-acidity graph концептуально остается операцией
"дай все". Для большого каталога сортов это не подходит. Нужно ввести явную
query model, например `AppleVarietySugarAcidityZonePointQuery`, и cursor-based
pagination.

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

### GET vs POST для search

GET удобен для простых read-only фильтров: URL можно сохранить, открыть заново
и проще отлаживать. Но исследовательский search быстро становится сложным:
plot selection, массивы classes, levels, cursor, future shapes.

Идея:

- Оставить простой `GET /apple-varieties/sugar-acidity-zone-points` для default
  сценария или простых фильтров.
- Основной исследовательский endpoint сделать как
  `POST /apple-varieties/sugar-acidity-zone-points/search` с request body.
- В application layer использовать одну query model независимо от transport.

### Cursor pagination

Курсор нужно добавить сразу. Он должен быть opaque для клиента. Минимальная
идея: Base64 JSON с последней позицией стабильной сортировки.

```json
{
  "lastName": "Kingston Black",
  "lastId": "..."
}
```

Сортировка по умолчанию: `normalizedName + varietyId`.

Ответ batch/search endpoint-а должен содержать page metadata:

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

- overfetch: читать больше кандидатов, пока не набран `limit` результатов или
  каталог не закончился;
- позже сделать materialized projection/index для вычисленных профилей,
  координат graph и классификаций;
- держать query model стабильной, чтобы перенос фильтров из application layer в
  persistence/projection слой не ломал API.

### Управление полнотой и актуальностью chemical profile

Неполный `ChemicalProfile` является допустимым состоянием данных. Система должна
позволять загружать и хранить профили, в которых отсутствуют отдельные
измерения: sugar, acidity, tannin, pH или другие будущие показатели.

Важно не вводить понятие "полный профиль" как универсальный флаг. Один и тот же
`ChemicalProfile` может быть пригоден для одной задачи и непригоден для другой.
Нужно мыслить через capability assessment: оценку пригодности профиля для
конкретного сценария.

Примеры capabilities:

- `SUGAR_ACIDITY_PLOT`: требует sugar и acidity.
- `NORTH_AMERICAN_PROFILE`: требует sugar, acidity и tannin.
- `SINGLE_VARIETY_CIDER_ASSESSMENT`: требует sugar, acidity и tannin, но
  сценарий пока не реализуем.
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

Для batch endpoint-а graph полезно возвращать не только отображаемые точки, но
и элементы, которые нельзя отобразить:

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

`SugarAcidityZoneAssessor` не должен решать задачу полноты данных. Его
ответственность - оценить профиль, который уже пригоден для sugar-acidity
assessment. Проверка пригодности профиля должна находиться перед assessor-ом, в
отдельной доменной или application service модели.

### REST error model

Сейчас добавлен `422 ProblemDetail` для `ChemicalProfileCapabilityNotMet`.
Нужно отдельно обсудить общий error strategy:

- not found;
- validation errors;
- unsupported classification system;
- malformed query/search body;
- capability not met;
- consistent `type`, `title`, `detail`, `properties`.

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
- Target: зона graph, пользовательская область или явные диапазоны
  sugar/acidity/tannin.
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

### Fermentation model and alerts

Для прогноза и мониторинга ферментации нужна отдельная модель процесса, которая
хранит не только исходный profile, но и временной ряд измерений.

Идеи реализации:

- Создать `FermentationBatch` как отдельный aggregate или bounded context.
- Связать batch с исходным `FeedstockSample` или blend recipe.
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
  историческим batches.

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

- `STANDARD_GRAVITY_DROP`: расчет по OG и predicted/assumed FG.

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

Открытый архитектурный вопрос: куда девать `CompositionRoot`. Нужно отдельно
решить, остается ли это отдельным application/bootstrap слоем, частью
configuration, или исчезает в пользу явной Spring configuration.
