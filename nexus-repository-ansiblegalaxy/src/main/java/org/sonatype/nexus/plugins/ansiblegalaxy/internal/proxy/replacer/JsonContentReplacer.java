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
 * String json; // {"foo": "123 abc"}
 * 
 * String updated = new JsonContentReplacer("foo", "abc", "xyz").getReplacedContent(json);
 * // output: {"foo": "123 xyz"}
 * </pre>
 * 
 * All values in JSON (regardless of where field nested level) will be updated.
 */
public class JsonContentReplacer
    extends JsonReplacer
{

  private final String search;

  private final String replace;

  public JsonContentReplacer(String jsonFieldName, String search, String replace) {
    super(jsonFieldName);
    this.search = checkNotNull(search);
    this.replace = checkNotNull(replace);
  }

  @Override
  protected String getUpdatedContent(JsonNode field) {
    if (field.isNull()) {
      return null;
    }
    return field.asText().replaceAll(search, replace);
  }

  @Override
  public String toString() {
    return "search=" + search + ", replace=" + replace + ", " + super.toString();
  }

}
