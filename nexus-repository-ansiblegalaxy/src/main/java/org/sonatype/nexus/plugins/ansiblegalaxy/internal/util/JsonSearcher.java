package org.sonatype.nexus.plugins.ansiblegalaxy.internal.util;

import java.io.IOException;

import org.sonatype.goodies.common.Loggers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class JsonSearcher
{

  private static final ObjectMapper MAPPER = new ObjectMapper(); // thread safe object

  private final Logger log = Loggers.getLogger(getClass());

  private final String jsonFieldName;

  public JsonSearcher(String jsonFieldName) {
    this.jsonFieldName = checkNotNull(jsonFieldName);
  }

  public String getValue(String input) throws IOException {
    JsonNode tree = MAPPER.readTree(input);

    return getValue(tree);
  }

  private String getValue(JsonNode item) {
    if (item.has(jsonFieldName)) {
      JsonNode valueNode = item.get(jsonFieldName);
      if (valueNode.isNull()) {
        return null;
      }
      else {
        String value = valueNode.asText();
        log.debug("{}: {}", jsonFieldName, value);
        return value;
      }
    }

    // recurse
    for (JsonNode child : item) {
      String value = getValue(child);
      if (null != value) {
        return value;
      }
    }
    return null;
  }

}
