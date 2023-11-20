package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer;

import com.fasterxml.jackson.databind.JsonNode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Given an input, will load the content as a JSON object and find the property with the given name.
 * The given value will then replace all "search" string occurrences with "replace" string value for property's value.
 * <p>
 * Example:
 *
 * <pre>
 * String json; // {"foo": "123 abc"}
 *
 * String updated = new JsonSearchReplacer("foo", "abc", "xyz").getReplacedContent(json);
 * // output: {"foo": "123 xyz"}
 * </pre>
 * <p>
 * All values in JSON (regardless of location in object) will be updated.
 */
public class JsonSearchReplacer
        extends JsonReplacer {

    private final String search;

    private final String replace;

    public JsonSearchReplacer(String jsonFieldName, String search, String replace) {
        super(jsonFieldName);
        this.search = checkNotNull(search);
        this.replace = checkNotNull(replace);
    }

    public JsonSearchReplacer(String jsonFieldName, String replace) {
        super(jsonFieldName);
        this.search = null;
        this.replace = checkNotNull(replace);
    }

    @Override
    protected String getUpdatedContent(JsonNode field) {
        if (search == null) {
            return replace;
        }
        return field.asText().replaceAll(search, replace);
    }

    @Override
    public String toString() {
        return "search=" + search + ", replace=" + replace + ", " + super.toString();
    }

}
