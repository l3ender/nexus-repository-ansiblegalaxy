/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.ansiblegalaxy.internal.metadata;

import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyPathUtils;
import org.sonatype.nexus.repository.storage.Asset;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class AnsibleGalaxyModulesResultBuilder {
    private final AnsibleGalaxyPathUtils ansibleGalaxyPathUtils;

    @Inject
    public AnsibleGalaxyModulesResultBuilder(
            AnsibleGalaxyPathUtils ansibleGalaxyPathUtils) {

        this.ansibleGalaxyPathUtils = ansibleGalaxyPathUtils;
    }

    public AnsibleGalaxyModulesResult parse(final Asset asset, String baseUrlRepo) {
        AnsibleGalaxyModulesResult result = new AnsibleGalaxyModulesResult();
        result.setHref(this.ansibleGalaxyPathUtils.parseHref(baseUrlRepo, asset.attributes().child("ansiblegalaxy").get("name").toString(), asset.attributes().child("ansiblegalaxy").get("version").toString()));
        result.setVersion(asset.attributes().child("ansiblegalaxy").get("version").toString());

        return result;
    }


//
//  private AnsibleGalaxyAttributes parseMetadata(final Asset asset) {
//
//    NestedAttributesMap attributes = asset.formatAttributes();
//
//    AnsibleGalaxyAttributes metadata = new AnsibleGalaxyAttributes(attributes.get("group").toString(), attributes.get("name").toString(), attributes.get("version").toString());
//
//    if (attributes.get("description") != null) {
//      metadata.setDescription(attributes.get("description").toString());
//    }
//
//    if (attributes.get("dependencies") != null) {
//      Object dependenciesObj = attributes.get("dependencies");
//      checkState(dependenciesObj instanceof Map, "dependencies must be a 'child'");
//      Map dependencies = (Map) dependenciesObj;
//
//      List<AnsibleGalaxyDependencyAttributes> moduleDependencies = new ArrayList<>(dependencies.keySet().size());
//      for (Object dependencyName : dependencies.keySet()) {
//        if (dependencies.get(dependencyName) != null) {
//          moduleDependencies.add(new AnsibleGalaxyDependencyAttributes(dependencyName.toString(),
//                  dependencies.get(dependencyName).toString()));
//        }
//      }
//      metadata.setDependencies(moduleDependencies);
//    }
//    else {
//      metadata.setDependencies(Collections.emptyList());
//    }
//
//    return metadata;
//  }
}