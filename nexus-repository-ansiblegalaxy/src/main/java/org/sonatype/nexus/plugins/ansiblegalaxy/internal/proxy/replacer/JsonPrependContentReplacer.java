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
 * String updated = new JsonPrependContentReplacer("foo", "123").getReplacedContent(json);
 * // output: {"foo": "123abc"}
 * </pre>
 * 
 * All values in JSON (regardless of where field nested level) will be updated.
 */
public class JsonPrependContentReplacer
    extends JsonContentReplacer
{

  private final String prepend;

  public JsonPrependContentReplacer(String jsonFieldName, String prepend) {
    super(jsonFieldName);
    this.prepend = checkNotNull(prepend);
  }

  @Override
  protected String getUpdatedContent(JsonNode item) {
    String text = item.get(jsonFieldName).asText();
    if (null != text) { // TODO check not working
      return prepend + text;
    }
    return text;
  }

}
