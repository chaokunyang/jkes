
## Schema Registry
```
curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"compatibility": "NONE"}' \
    http://k1:8081/config
  {"compatibility":"NONE"}
```
```
curl -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"compatibility": "BACKWARD"}' \
    http://k1:8081/config
```

## Mapping Admin
```
curl -XPUT 'localhost:9200/my_index?pretty' -H 'Content-Type: application/json' -d'
{
  "mappings": {
    "user": { 
      "_all":       { "enabled": false  }, 
      "properties": { 
        "title":    { "type": "text"  }, 
        "name":     { "type": "text"  }, 
        "age":      { "type": "integer" }  
      }
    },
    "blogpost": { 
      "_all":       { "enabled": false  }, 
      "properties": { 
        "title":    { "type": "text"  }, 
        "body":     { "type": "text"  }, 
        "user_id":  {
          "type":   "keyword" 
        },
        "created":  {
          "type":   "date", 
          "format": "strict_date_optional_time||epoch_millis"
        }
      }
    }
  }
}
'
```

## index creation strategy
Strategies in production environments
It is strongly recommended to use either NONE or VALIDATE in a production environment. RECREATE and RECREATE_DELETE are obviously unsuitable in this context (unless you want to reindex everything upon every startup), and MERGE may leave your mapping half-merged in case of conflict.

To be precise, if your mapping changed in an incompatible way, such as a field having its type changed, merging may be impossible. In this case, the MERGE strategy will prevent Hibernate Search from starting, but it may already have successfully merged another index, making a rollback difficult at best.

Also, when updating analyzer definitions, Hibernate Search will stop the affected indexes during the update. This means the MERGE strategy should be used with caution when multiple clients use Elasticsearch indexes managed by Hibernate Search: those clients should be synchronized in such a way that while Hibernate Search is starting, no other client tries to use the index.

For these reasons, migrating your mapping should be considered a part of your deployment process and be planned cautiously.
Mapping validation is as permissive as possible. Fields or mappings that are unknown to Hibernate Search will be ignored, and settings that are more powerful than required (e.g. a field annotated with @Field(index = Index.NO) in Search but marked as "index": analyzed in Elasticsearch) will be deemed valid.

One exception should be noted, though: date formats must match exactly the formats specified by Hibernate Search, due to implementation constraints.


## Kafka Connect
- create connector or update connector config
- ensure all task is up, otherwise restart relevant task