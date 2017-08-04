# Kafka Connect
## Get the worker’s version information
```
curl k1-test.com:8083/ | jq
{
  "version": "0.10.0.1-cp1",
  "commit": "ea5fcd28195f168b"
}
```
## List the connector plugins available on this worker
```
curl k1-test.com:8083/connector-plugins | jq
```
## Listing active connectors on a worker
```
curl k1-test.com:8083/connectors
```
## create connector
```shell
curl -XPOST http://k1-test.com:8083/connectors -H 'Content-Type: application/json' -d'
{
  "name": "person_group_es_sink",
  "config": {
  	"connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
	"tasks.max": "10",
	"topics": "person_group",
	"connection.url": "http://es1-test.com:9200,http://es2-test.com:9200,http://es3-test.com:9200",
	"batch.size": "2000",
	"linger.ms": "5",
	"max.in.flight.requests": "11",
	"type.name": "person_group",
	"key.ignore": "false",
	"schema.ignore": "true"
  }
}
'
```
## delete a connector
```
curl -XDELETE http://k1-test.com:8083/connectors/person_group_es_sink
```
## restart a connector
```
curl -X POST k1-test.com:8083/connectors/person_group_es_sink/restart
```
## pause a connector
```
curl -X PUT k1-test.com:8083/connectors/person_group_es_sink/pause
```
## Resuming a connector
```
curl -X PUT k1-test.com:8083/connectors/person_group_es_sink/resume
```
## Updating connector configuration
```
curl -X PUT -H "Content-Type: application/json" --data '{"connector.class":"FileStreamSinkConnector","file":"test.sink.txt","tasks.max":"2","topics":"connect-test","name":"local-file-sink"}' k1-test.com:8083/connectors/local-file-sink/config
```
## Getting connector status
```
curl k1-test.com:8083/connectors/person_group_es_sink/status | jq
```
## Getting tasks for a connector
```
curl k1-test.com:8083/connectors/person_group_es_sink/tasks | jq
```
## Restarting a task
```
curl -X POST k1-test.com:8083/connectors/person_group_es_sink/tasks/0/restart
(no response printed if success)
```
## Getting connector info
```
curl k1-test.com:8083/connectors/person_group_es_sink | jq
``` 
## Getting connector config
```
curl k1-test.com:8083/connectors/person_group_es_sink/config | jq
``` 






