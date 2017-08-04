package com.timeyang.jkes.core.util;

import com.timeyang.jkes.core.elasticsearch.annotation.Document;
import com.timeyang.jkes.core.support.Config;

/**
 * Document Utils
 *
 * @author chaokunyang
 */
public class DocumentUtils {

    public static String getIndexName(Class<?> clazz) {
        Document document = clazz.getAnnotation(Document.class);
        String indexName = document.indexName();

        if(!StringUtils.hasText(indexName))
            indexName = StringUtils.addUnderscores(clazz.getSimpleName());

        String prefix = Config.getJkesProperties().getEsIndexPrefix();
        if(StringUtils.hasText(prefix)) {
            return prefix + "_" + indexName;
        }

        return indexName;
    }

    public static String getTypeName(Class<?> clazz) {
        Document document = clazz.getAnnotation(Document.class);
        String type = document.type();
        if(!StringUtils.hasText(type))
            type = StringUtils.addUnderscores(clazz.getSimpleName());

        return type;
    }

    public static String getAlias(Class<?> clazz) {
        Document document = clazz.getAnnotation(Document.class);
        String alias = document.alias();
        if(!StringUtils.hasText(alias))
            alias = StringUtils.addUnderscores(clazz.getSimpleName()) + "_alias";

        String prefix = Config.getJkesProperties().getEsIndexPrefix();
        if(StringUtils.hasText(prefix)) {
            return prefix + "_" + alias;
        }

        return alias;
    }
}
