# Mapping Example
## Disabling automatic type creation
### Disable automatic type creation for the index named "data"
```
curl -XPUT 'localhost:9200/data/_settings?pretty' -H 'Content-Type: application/json' -d'
{
  "index.mapper.dynamic":false 
}
'
```

### Automatic type creation can also be disabled for all indices by setting an index template:
```
curl -XPUT 'localhost:9200/_template/template_all?pretty' -H 'Content-Type: application/json' -d'
{
  "template": "*",
  "order":0,
  "settings": {
    "index.mapper.dynamic": false 
  }
}
'
```

## Nested
```json
{
  "my_index": {
    "mappings": {
      "my_type": {
        "properties": {
          "group": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "user": {
            "type": "nested",
            "properties": {
              "first": {
                "type": "text",
                "fields": {
                  "keyword": {
                    "type": "keyword",
                    "ignore_above": 256
                  }
                }
              },
              "last": {
                "type": "text",
                "fields": {
                  "keyword": {
                    "type": "keyword",
                    "ignore_above": 256
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
```

## Object
You are not required to set the field type to object explicitly, as this is the default value.
```shell
curl -XPUT 'localhost:9200/my_index?pretty' -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "my_type": { 
      "properties": {
        "region": {
          "type": "keyword"
        },
        "manager": { 
          "properties": {
            "age":  { "type": "integer" },
            "name": { 
              "properties": {
                "first": { "type": "text" },
                "last":  { "type": "text" }
              }
            }
          }
        }
      }
    }
  }
}
'
```

## dynamic mapping
The dynamic setting may be set at the mapping type level, and on each inner object. Inner objects inherit the setting from their parent object or from the mapping type. For instance:
```
curl -XPUT 'localhost:9200/my_index?pretty' -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "my_type": {
      "dynamic": false, 
      "properties": {
        "user": { 
          "properties": {
            "name": {
              "type": "text"
            },
            "social_networks": { 
              "dynamic": true,
              "properties": {}
            }
          }
        }
      }
    }
  }
}
'
```








