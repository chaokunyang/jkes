package com.timeyang.jkes.core.elasticsearch.indices;

import com.timeyang.jkes.core.annotation.Document;
import com.timeyang.jkes.core.elasticsearch.mapping.MappingBuilder;
import com.timeyang.jkes.core.util.Asserts;
import com.timeyang.jkes.core.util.DocumentUtils;
import org.json.JSONObject;

/**
 * Indices Builder
 * @author chaokunyang
 */
public class IndicesBuilder {

    private static final IndicesBuilder indicesBuilder = new IndicesBuilder();

    private IndicesBuilder() {
    }

    public JSONObject buildIndex(Class<?> clazz) {
        Asserts.check(clazz.isAnnotationPresent(Document.class), "The class " + clazz.getCanonicalName() + " must be annotated with " + Document.class.getCanonicalName());

        // settings
        JSONObject settings = new JSONObject();
        settings.put("index.mapper.dynamic", false); // Disable automatic type creation
        settings.put("number_of_shards", 11);
        settings.put("number_of_replicas", 2);

        // mappings
        JSONObject mappings = new JSONObject();
        JSONObject mapping = new MappingBuilder().buildMapping(clazz);
        String type = DocumentUtils.getTypeName(clazz);
        mappings.put(type, mapping);

        // aliases
        JSONObject aliases = new JSONObject();

        aliases.put(DocumentUtils.getAlias(clazz), new JSONObject()); // alias to entity

        JSONObject indices = new JSONObject();
        indices.put("settings", settings);
        indices.put("mappings", mappings);
        indices.put("aliases", aliases);

        return indices;
    }

    public static IndicesBuilder getInstance() {
        return indicesBuilder;
    }
}
