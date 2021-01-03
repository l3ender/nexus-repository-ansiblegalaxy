package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class JsonContentReplacer
    extends ContentReplacer
{

  private static final ObjectMapper MAPPER = new ObjectMapper(); // thread safe object

  protected final String jsonFieldName;

  public JsonContentReplacer(String jsonFieldName) {
    this.jsonFieldName = checkNotNull(jsonFieldName);
  }

  protected abstract String getUpdatedContent(JsonNode item);

  @Override
  protected String getReplacedContent(String input) throws IOException {
    JsonNode tree = MAPPER.readTree(input);

    updateFields(tree);

    return MAPPER.writeValueAsString(tree);
  }

  private void updateFields(JsonNode item) {
    if (item.has(jsonFieldName)) {
      log.trace("updating {}", jsonFieldName);
      String updated = getUpdatedContent(item.get(jsonFieldName));
      ((ObjectNode) item).put(jsonFieldName, updated);
    }

    // recurse
    for (JsonNode child : item) {
      updateFields(child);
    }
  }

}
