# Jkes Search Service
## api/v1
```
/api/v1/b2c_test_product/product/_search
```
```
http://localhost:9000/api/v1/b2c_test_product/product/_search
```
```
integration_test_person_group/person_group/_search?from=3&size=10
{
  "query": {
    "nested": {
      "path": "persons",
      "score_mode": "avg",
      "query": {
        "bool": {
          "must": [
            {
              "range": {
                "persons.age": {
                  "gt": 5
                }
              }
            }
          ]
        }
      }
    }
  }
}
```

## Docker
windows
```
@FOR /f "tokens=*" %i IN ('docker-machine env default') DO @%i
```

unix
```
eval $(docker-machine env default)
```

maven
```
mvn clean package docker:build
mvn clean package docker:build -DpushImage
mvn clean package docker:build -DpushImageTag

```

run
```
docker run chaokunyang/jkes-search-service --env APP_ARGS="--spring.profiles.active=test"
```