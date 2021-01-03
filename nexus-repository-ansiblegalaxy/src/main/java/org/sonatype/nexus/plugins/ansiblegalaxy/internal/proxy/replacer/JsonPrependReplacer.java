package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer;

import com.fasterxml.jackson.databind.JsonNode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Given an input stream will load it as a JSON object and find the property at the given path (JSON expression path).
 * The property value will then replace all "search" string occurrences with "replace" string value for the provided
 * input stream.
 * 
 * Example:
 * 
 * <pre>
 * String json; // {"foo": "abc"}
 * 
 * String updated = new JsonPrependReplacer("foo", "123").getReplacedContent(json);
 * // output: {"foo": "123abc"}
 * </pre>
 * 
 * All values in JSON (regardless of where field nested level) will be updated.
 */
public class JsonPrependReplacer
    extends JsonReplacer
{

  private final String prepend;

  public JsonPrependReplacer(String jsonFieldName, String prepend) {
    super(jsonFieldName);
    this.prepend = checkNotNull(prepend);
  }

  @Override
  protected String getUpdatedContent(JsonNode field) {
    if (field.isNull()) {
      return null;
    }
    return prepend + field.asText();
  }

  @Override
  public String toString() {
    return "prepend=" + prepend + ", " + super.toString();
  }

}
