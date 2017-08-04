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