/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2020-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.ansiblegalaxy.api;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.text.Strings2;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.rest.api.SimpleApiRepositoryAdapter;
import org.sonatype.nexus.repository.rest.api.model.AbstractApiRepository;
import org.sonatype.nexus.repository.routing.RoutingRuleStore;
import org.sonatype.nexus.repository.types.HostedType;
import org.sonatype.nexus.repository.types.ProxyType;

import org.sonatype.nexus.plugins.ansiblegalaxy.AnsibleGalaxyFormat;
import org.sonatype.nexus.plugins.ansiblegalaxy.AnsibleGalaxyAttributes;
import org.sonatype.nexus.plugins.ansiblegalaxy.AnsibleGalaxyHostedApiRepository;
import org.sonatype.nexus.plugins.ansiblegalaxy.AnsibleGalaxyProxyApiRepository;

@Named(AnsibleGalaxyFormat.NAME)
public class AnsibleGalaxyApiRepositoryAdapter
    extends SimpleApiRepositoryAdapter
{
    private static final String ANSIBLEGALAXY = "ansiblegalaxy";

    @Inject
    public AnsibleGalaxyApiRepositoryAdapter(final RoutingRuleStore routingRuleStore) {
        super(routingRuleStore);
    }

    @Override
    public AbstractApiRepository adapt(final Repository repository) {
        boolean online = repository.getConfiguration().isOnline();
        String name = repository.getName();
        String url = repository.getUrl();

        switch( repository.getType().toString()) {
            case HostedType.NAME:
                return new AnsibleGalaxyHostedApiRepository(
                    repository.getName(),
                    repository.getUrl(),
                    repository.getConfiguration().isOnline(),
                    getHostedStorageAttributes(repository),
                    getCleanupPolicyAttributes(repository),
                    getComponentAttributes(repository),
                    createAnsibleGalaxyAttributes(repository)
                );
            case ProxyType.NAME:
                return new AnsibleGalaxyProxyApiRepository(
                    repository.getName(),
                    repository.getUrl(),
                    repository.getConfiguration().isOnline(),
                    getHostedStorageAttributes(repository),
                    getCleanupPolicyAttributes(repository),
                    getProxyAttributes(repository),
                    getNegativeCacheAttributes(repository),
                    getHttpClientAttributes(repository),
                    getRoutingRuleName(repository),
                    getReplicationAttributes(repository),
                    createAnsibleGalaxyAttributes(repository)
                );
            default:
                return super.adapt(repository);
        }
    }

    private AnsibleGalaxyAttributes createAnsibleGalaxyAttributes(final Repository repository) {
        String disposition = repository.getConfiguration().attributes(ANSIBLEGALAXY).get("contentDisposition", String.class);
        return new AnsibleGalaxyAttributes(disposition);
    }
}
