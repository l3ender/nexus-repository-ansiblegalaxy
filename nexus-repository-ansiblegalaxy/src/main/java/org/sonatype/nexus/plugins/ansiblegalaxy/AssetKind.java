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
package org.sonatype.nexus.plugins.ansiblegalaxy;

import org.sonatype.nexus.repository.cache.CacheControllerHolder.CacheType;

import javax.annotation.Nonnull;

import static org.sonatype.nexus.repository.cache.CacheControllerHolder.CONTENT;
import static org.sonatype.nexus.repository.cache.CacheControllerHolder.METADATA;

/**
 * Asset kinds for AnsibleGalaxy.
 */
public enum AssetKind {
    API_METADATA(METADATA),
    COLLECTION_DETAIL(METADATA),
    COLLECTION_VERSION_LIST(METADATA),
    COLLECTION_VERSION_LIST_LIMIT(METADATA),
    COLLECTION_VERSION_DETAIL(METADATA),
    COLLECTION_ARTIFACT(CONTENT),
    ROLE_SEARCH(METADATA),
    ROLE_DETAIL(METADATA),
    ROLE_VERSION_LIST(METADATA),
    ROLE_ARTIFACT(CONTENT);

    private final CacheType cacheType;

    AssetKind(final CacheType cacheType) {
        this.cacheType = cacheType;
    }

    @Nonnull
    public CacheType getCacheType() {
        return cacheType;
    }
}
