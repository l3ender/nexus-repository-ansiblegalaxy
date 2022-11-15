package org.sonatype.nexus.plugins.ansiblegalaxy.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sonatype.nexus.plugins.ansiblegalaxy.AssetKind;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.metadata.AnsibleGalaxyAttributes;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.metadata.AnsibleGalaxyDependencyAttributes;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 0.0.2
 */
@Named
@Singleton
public class AnsibleGalaxyAttributeParser {
    private final TgzParser tgzParser;

    private final ObjectMapper objectMapper;

    @Inject
    public AnsibleGalaxyAttributeParser(final TgzParser tgzParser,
                                        final ObjectMapper objectMapper) {
        this.tgzParser = checkNotNull(tgzParser);
        this.objectMapper = checkNotNull(objectMapper);
    }

    public AnsibleGalaxyAttributes getAttributes(final AssetKind assetKind, final InputStream inputStream) throws IOException {
        return getAttributesFromInputStream(inputStream);
    }

    public AnsibleGalaxyAttributes getAttributesFromInputStream(final InputStream inputStream) throws IOException {
        try (InputStream is = tgzParser.getMetadataFromInputStream(inputStream)) {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonMap = mapper.readTree(is);
            JsonNode collection_info = jsonMap.get("collection_info");

            AnsibleGalaxyAttributes attrs = new AnsibleGalaxyAttributes(collection_info.get("namespace").toString().replaceAll("\"", ""), collection_info.get("name").toString().replaceAll("\"", ""), collection_info.get("version").toString().replaceAll("\"", ""));
            attrs.setDescription(collection_info.get("description").toString().replaceAll("\"", ""));

            JsonNode dependencies = collection_info.get("dependencies");
            Iterator<Map.Entry<String, JsonNode>> it = dependencies.fields();
            List<AnsibleGalaxyDependencyAttributes> list = new ArrayList<AnsibleGalaxyDependencyAttributes>();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> field = it.next();
                list.add(new AnsibleGalaxyDependencyAttributes(field.getKey(), field.getValue().toString().replaceAll("\"", "")));
            }
            attrs.setDependencies(list);

            return attrs;
        }
    }
}