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
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;

import javax.inject.Inject;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;

public class AnsibleGalaxyModulesBuilder {
    private final AnsibleGalaxyPathUtils ansibleGalaxyPathUtils;

    @Inject
    public AnsibleGalaxyModulesBuilder(final AnsibleGalaxyPathUtils ansibleGalaxyPathUtils) {
        this.ansibleGalaxyPathUtils = checkNotNull(ansibleGalaxyPathUtils);
    }

    public AnsibleGalaxyModules parse(final long total, final long limit, final long offset, final Context context) {
        AnsibleGalaxyModules releases = new AnsibleGalaxyModules();
        releases.setPagination(parsePagination(total, limit, offset, context));
        return releases;
    }

    private AnsibleGalaxyPagination parsePagination(final long total, final long limit, final long offset, final Context context) {
        AnsibleGalaxyPagination modulePagination = new AnsibleGalaxyPagination();
        Parameters parameters = context.getRequest().getParameters();

        modulePagination.setCount(total);

        Parameters newParameters = new Parameters();
        for (Entry<String, String> param : parameters) {
            newParameters.set(param.getKey(), param.getValue());
        }

        if (offset - limit > 0) {
            newParameters.replace("offset", Long.toString(offset - limit));
            modulePagination.setPrevious(ansibleGalaxyPathUtils.buildModuleReleaseByNamePath(newParameters));
        } else {
            modulePagination.setPrevious(null);
        }

        if (offset + limit < total) {
            newParameters.replace("offset", Long.toString(offset + limit));
            modulePagination.setNext(ansibleGalaxyPathUtils.buildModuleReleaseByNamePath(newParameters));
        } else {
            modulePagination.setNext(null);
        }

        return modulePagination;
    }
}