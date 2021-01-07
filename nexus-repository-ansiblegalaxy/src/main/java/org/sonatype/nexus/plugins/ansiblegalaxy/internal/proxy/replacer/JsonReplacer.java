package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class JsonReplacer
    extends Replacer
{

  private static final ObjectMapper MAPPER = new ObjectMapper(); // thread safe object

  protected final String jsonFieldName;

  public JsonReplacer(String jsonFieldName) {
    this.jsonFieldName = checkNotNull(jsonFieldName);
  }

  protected abstract String getUpdatedContent(JsonNode item);

  @Override
  public String getReplacedContent(String input) throws IOException {
    JsonNode tree = MAPPER.readTree(input);

    updateFields(tree);

    return MAPPER.writeValueAsString(tree);
  }

  private void updateFields(JsonNode item) {
    if (item.has(jsonFieldName)) {
      JsonNode old = item.get(jsonFieldName);
      if (!old.isNull()) {
        String updated = getUpdatedContent(old);
        log.trace("updating {} from {} -> {}", jsonFieldName, old, updated);
        ((ObjectNode) item).put(jsonFieldName, updated);
      }
    }

    // recurse
    for (JsonNode child : item) {
      updateFields(child);
    }
  }

  @Override
  public String toString() {
    return "jsonFieldName=" + jsonFieldName;
  }

}
