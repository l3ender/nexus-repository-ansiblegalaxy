package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer;

import com.fasterxml.jackson.databind.JsonNode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Given an input, will load the content as a JSON object and find the property with the given name.
 * The given value will then be prepended to the current value for the property.
 * <p>
 * Example:
 *
 * <pre>
 * String json; // {"foo": "abc"}
 *
 * String updated = new JsonPostpendReplacer("foo", "123").getReplacedContent(json);
 * // output: {"foo": "abc123"}
 * </pre>
 * <p>
 * All values in JSON (regardless of location in object) will be updated.
 */
public class JsonPostpendReplacer
        extends JsonReplacer {

    private final String postpend;

    public JsonPostpendReplacer(String jsonFieldName, String postpend) {
        super(jsonFieldName);
        this.postpend = checkNotNull(postpend);
    }

    @Override
    protected String getUpdatedContent(JsonNode field) {
        return field.asText() + postpend;
    }

    @Override
    public String toString() {
        return "postpend=" + postpend + ", " + super.toString();
    }

}
