### Общее описание

Библиотека для запросов к хранилищу сущностей. Поддержаны выборка, создание, изменение, удаление сущностей, работа с транзакциями.

### Подключение

```
io.extremum:ground-client:<version>
// и вспомогательные библиотеки
io.extremum:extremum-shared-models:<version>
de.cronn:reflection-util:2.14.0
```

#### Отключение автоконфигурации

Если необходимо отключить автоконфигурацию, нужно задать следующий параметр в application.properties:

```properties
extremum.ground.client.autoconfiguration=false
```

### Добавление параметров в application.properties

#### Основные параметры

```properties
extremum.functions.api.xAppId=
extremum.functions.api.baseUrl=
```
```extremum.ground.client.xAppId``` - id приложения (далее ```xAppId```)

```extremum.ground.client.baseUrl``` - основной url приложения (далее ```baseUrl```). Например, ```https://api.aj84.y.extremum.io```

Если их не указывать, то они заполнятся из переменных среды ```xAppId``` и ```apiBaseUrl``` соответственно.


#### Параметры путей graphql api

```properties
extremum.ground.client.uri=
extremum.ground.client.path=
extremum.ground.client.graphql.path=
extremum.ground.client.tx.path=
```

```extremum.ground.client.uri``` - uri api. Необязательный параметр. Если не указывать, то он будет сформирован из ```baseUrl``` и ```xAppId```.
Пример значений: ```https://api.aj84.y.extremum.io```, ```http://localhost:8080```

```extremum.ground.client.path``` - дополнительный путь в uri. Добавляется к uri. По умолчанию ```/v3```.

```extremum.ground.client.graphql.path``` - дополнительный путь для запросов к graphql. По умолчанию ```/graphql```.

```extremum.ground.client.tx.path``` - дополнительный путь для запросов по транзакциям. По умолчанию ```/tx```.


### Использование

#### Определение моделей

Все модели должны быть наследованы от

```
io.extremum.sharedmodels.basic.BasicModel<ID extends Serializable>
```