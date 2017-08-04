# Jkes Delete Service
## Road map
Currently we use kafka consumer to delete Elasticsearch document. In future release, we will write a kafka elasticsearch delete consumer to do delete. In this way, we can use kafka connect concurrence, convenient rest admin and more separate concern(each project can have its own delete topic, not all project in the same kafka cluster and es cluster share the same delete topic ). 

## Issues
Currently all project use the same kafka cluster must use the same elasticsearch cluster and share the same delete topic, because we don't know what projects is using our delete services, and we can't handle all delete event if we don't share the same fixed topic. **This will be addressed when we finished kafka elasticsearch delete connector, because we can use kafka connect rest api to create a connector for every project incrementally.** 