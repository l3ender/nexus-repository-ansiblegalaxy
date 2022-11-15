package org.sonatype.nexus.plugins.ansiblegalaxy.internal.store;

import org.sonatype.nexus.plugins.ansiblegalaxy.AnsibleGalaxyFormat;
import org.sonatype.nexus.repository.content.store.FormatStoreModule;

import javax.inject.Named;

@Named(AnsibleGalaxyFormat.NAME)
public class AnsibleGalaxyStoreModule
        extends FormatStoreModule<AnsibleGalaxyContentRepositoryDAO,
        AnsibleGalaxyComponentDAO,
        AnsibleGalaxyAssetDAO,
        AnsibleGalaxyAssetBlobDAO> {
    // nothing to add...
}