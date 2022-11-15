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


public class AnsibleGalaxyModuleName {
    private String name;

    private String versions_url;

    public String getVersions_url() {
        return versions_url;
    }

    public AnsibleGalaxyModuleName setVersions_url(String versions_url) {
        this.versions_url = versions_url;
        return this;
    }

    public String getName() {
        return name;
    }

    public AnsibleGalaxyModuleName setName(String name) {
        this.name = name;
        return this;
    }
}