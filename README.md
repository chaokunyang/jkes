# Jkes
Jkes is a search framework and multi-tenant search platform based on java, kafka, kafka connect, elasticsearch

## Getting Started
The most easy to get started with jkes is reference [jkes-integration-test](https://github.com/chaokunyang/jkes/tree/master/jkes-integration-test) project.
The [jkes-integration-test](https://github.com/chaokunyang/jkes/tree/master/jkes-integration-test) project is a full-feathered spring boot project that we used for test.
So most functions are demonstrated in it.

### Prerequisite
- Install Kafka cluster, 3 node cluster is recommended
- Install Kafka connect, distributed mode is recommended. 
- Install [jkes-index-connector](https://github.com/confluentinc/kafka-connect-elasticsearch) and [jkes-delete-connector](https://github.com/chaokunyang/jkes/tree/master/jkes-services/jkes-delete-connector) to kafka connect
- Install ElasticSearch cluster, 3 node cluster is recommended. 
- Install corresponding analyze plugin.

### Index
Annotate following annotation on entity class
`@Document`,`@Field`,`@MultiFields`

Then the index will happen when you save data to database。

For example:
```java
@Entity
@Document
public class Person extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String gender;
    private Integer age;
    private String description;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    private PersonGroup personGroup;

    @Field(type = FieldType.Long)
    public Long getId() {
        return id;
    }

    @MultiFields(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "raw", type = FieldType.Keyword),
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english")
            }
    )
    public String getName() {
        return name;
    }

    @Field(type = FieldType.Keyword)
    public String getGender() {
        return gender;
    }

    @Field(type = FieldType.Integer)
    public Integer getAge() {
        return age;
    }

    /**
     * don't add @Field to test whether ignored
     */
    // @Field(type = FieldType.Text)
    public String getDescription() {
        return description;
    }

    @Field(type = FieldType.Object)
    public PersonGroup getPersonGroup() {
        return personGroup;
    }
}
```
Some details of the `Person` class is ignored.

When you call `personRepository.save(person)`, the index process will happen automatically.

### Analyze
Install `smartcn` analyze plugin first.
```
curl -XPOST 'localhost:9200/_analyze?pretty' -H 'Content-Type: application/json' -d'
{
  "analyzer": "smartcn",
  "text":     "Jkes搜索框架"
}
'
```
result:
```
{
  "tokens" : [
    {
      "token" : "jke",
      "start_offset" : 0,
      "end_offset" : 4,
      "type" : "word",
      "position" : 0
    },
    {
      "token" : "搜索",
      "start_offset" : 4,
      "end_offset" : 6,
      "type" : "word",
      "position" : 1
    },
    {
      "token" : "框架",
      "start_offset" : 6,
      "end_offset" : 8,
      "type" : "word",
      "position" : 2
    }
  ]
}
```

### Search
Jkes support all elasticsearch query, such as:

- URI query
```
curl -XPOST localhost:9000/api/v1/integration_test_person_group/person_group/_search?from=3&size=10
```

- Nested query
```
integration_test_person_group/person_group/_search?from=0&size=10
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
- match query
```
integration_test_person_group/person_group/_search?from=0&size=10
{
  "query": {
      "match": {
        "interests": "Hadoop"
      }
    }
}
```
- bool query
```
{
  "query": {
    "bool" : {
      "must" : {
        "match" : { "interests" : "Hadoop" }
      },
      "filter": {
        "term" : { "name.raw" : "name0" }
      },
      "should" : [
        { "match" : { "interests" : "Flink" } },
        {
            "nested" : {
                "path" : "persons",
                "score_mode" : "avg",

                "query" : {
                    "bool" : {
                        "must" : [
                        { "match" : {"persons.name" : "name40"} },
                        { "match" : {"persons.interests" : "interests"} }
                        ],
                        "must_not" : {
                            "range" : {
                              "age" : { "gte" : 50, "lte" : 60 }
                            }
                          }
                    }
                }
            }
        }

      ],
      "minimum_should_match" : 1,
      "boost" : 1.0
    }

  }

}
```

### Elasticsearch admin
Install [cerebro](https://github.com/lmenezes/cerebro) for elasticsearch admin web UI.
