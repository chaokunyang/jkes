# Indices Template

```shell
curl -XPUT 'localhost:9200/_template/template_1?pretty' -H 'Content-Type: application/json' -d'
{
  "template": "b2*",
  "settings": {
    "number_of_shards": 11,
    "number_of_replicas" : 2,
    "index.mapper.dynamic": false
  },
  "mappings": {
    "type1": {
      "_source": {
        "enabled": false
      },
      "properties": {
        "host_name": {
          "type": "keyword"
        },
        "created_at": {
          "type": "date",
          "format": "EEE MMM dd HH:mm:ss Z YYYY"
        }
      }
    }
  }
}
'

```

## Disable automatic type creation for the index named "data"
 ```
 curl -XPUT 'localhost:9200/data/_settings?pretty' -H 'Content-Type: application/json' -d'
 {
   "index.mapper.dynamic":false 
 }
 '
```

 
 
 