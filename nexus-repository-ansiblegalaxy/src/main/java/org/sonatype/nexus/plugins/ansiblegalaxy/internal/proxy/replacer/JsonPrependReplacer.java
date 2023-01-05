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
 * String updated = new JsonPrependReplacer("foo", "123").getReplacedContent(json);
 * // output: {"foo": "123abc"}
 * </pre>
 * <p>
 * All values in JSON (regardless of location in object) will be updated.
 */
public class JsonPrependReplacer
        extends JsonReplacer {

    private final String prepend;

    public JsonPrependReplacer(String jsonFieldName, String prepend) {
        super(jsonFieldName);
        this.prepend = checkNotNull(prepend);
    }

    @Override
    protected String getUpdatedContent(JsonNode field) {
        return prepend + field.asText();
    }

    @Override
    public String toString() {
        return "prepend=" + prepend + ", " + super.toString();
    }

}
