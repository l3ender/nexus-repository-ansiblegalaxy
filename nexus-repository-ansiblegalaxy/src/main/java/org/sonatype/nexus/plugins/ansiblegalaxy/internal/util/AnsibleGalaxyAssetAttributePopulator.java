/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.ansiblegalaxy.internal.util;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.metadata.AnsibleGalaxyAttributes;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.metadata.AnsibleGalaxyDependencyAttributes;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class AnsibleGalaxyAssetAttributePopulator
        extends ComponentSupport {
    public void populate(final NestedAttributesMap attributes, final AnsibleGalaxyAttributes ansibleGalaxyAttributes) {
        attributes.set("group", ansibleGalaxyAttributes.getGroup());
        attributes.set("name", ansibleGalaxyAttributes.getGroup() + "-" + ansibleGalaxyAttributes.getName());
        attributes.set("version", ansibleGalaxyAttributes.getVersion());
        attributes.set("description", ansibleGalaxyAttributes.getDescription());

        if (ansibleGalaxyAttributes.getDependencies() != null) {
            NestedAttributesMap dependencies = attributes.child("dependencies");
            for (AnsibleGalaxyDependencyAttributes ansibleGalaxyDependency : ansibleGalaxyAttributes.getDependencies()) {
                dependencies.set(ansibleGalaxyDependency.getName(), ansibleGalaxyDependency.getVersion_requirement());
            }
        }

    }
}