package com.timeyang.jkes.core.annotation;

/**
 * ELasticSearch Data Type
 *
 * @author chaokunyang
 */
public enum FieldType {
    /**
     * For now, primitive type, boxed primitive type, Date is supported
     */
    Auto,
    /**
     * <p>
     *    A field to index full-text values, such as the body of an email or the description of a product. These fields are analyzed, that is they are passed through an analyzer to convert the string into a list of individual terms before being indexed.
     * </p>
     * <p>
     *    If you need to index structured content such as email addresses, hostnames, status codes, or tags, it is likely that you should rather use a Keyword field.
     * </p>
     */
    Text,
    /**
     * <p>
     *     A field to index structured content such as email addresses, hostnames, status codes, zip codes or tags.
     * </p>
     <p>
     They are typically used for filtering (Find me all blog posts where status is published), for sorting, and for aggregations. Keyword fields are only searchable by their exact value.</p>
     <p>
     If you need to index full text content such as email bodies or product descriptions, it is likely that you should rather use a text field.</p>
     */
    Keyword,
    Date,
    Byte, Short, Integer, Long, Float, Double,

    /**
     * Usage of any value other than false, "false", true and "true" is deprecated.
     */
    Boolean,
    /**
     * the type which supports the hierarchical nature of JSON, used in object
     */
    Object,
    /**
     * the type which supports the hierarchical nature of JSON, used in object array
     */
    Nested,

    // /**
    //  * The completion suggester provides auto-complete/search-as-you-type functionality
    //  */
    // Completion,

    // IP, geo_point, geo_shape
}
