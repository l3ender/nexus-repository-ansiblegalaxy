package org.sonatype.nexus.plugins.ansiblegalaxy.internal.hosted;

import org.sonatype.nexus.plugins.ansiblegalaxy.AssetKind;
import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.Facet.Exposed;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;

import java.io.IOException;

@Exposed
public interface AnsibleGalaxyHostedFacet
        extends Facet {
    Content get(String path) throws IOException;

    void put(String path, Payload content, final AssetKind assetKind) throws IOException;

    boolean delete(String path) throws IOException;

    Content searchByName(Context context, String user, String module);

    Content searchVersionsByName(Context context, String user, String module);

    Content moduleByNameAndVersion(Context context, String user, String module, String version);
}