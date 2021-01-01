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
package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.goodies.common.Loggers;
import org.sonatype.nexus.plugins.ansiblegalaxy.AssetKind;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyDataAccess;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyPathUtils;
import org.sonatype.nexus.repository.cache.CacheInfo;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.proxy.ProxyFacet;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.payloads.TempBlob;
import org.sonatype.nexus.transaction.UnitOfWork;

import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyDataAccess.HASH_ALGORITHMS;

/**
 * AnsibleGalaxy {@link ProxyFacet} implementation.
 *
 * @since 0.0.1
 */
@Named
public class AnsibleGalaxyProxyFacetImpl
    extends ProxyFacetSupport
    implements AnsibleGalaxyProxyFacet
{
  private final Logger log = Loggers.getLogger(getClass());

  private final AnsibleGalaxyPathUtils ansiblegalaxyPathUtils;

  private final AnsibleGalaxyDataAccess ansiblegalaxyDataAccess;

  @Inject
  public AnsibleGalaxyProxyFacetImpl(
      final AnsibleGalaxyPathUtils ansiblegalaxyPathUtils,
      final AnsibleGalaxyDataAccess ansiblegalaxyDataAccess)
  {
    this.ansiblegalaxyPathUtils = checkNotNull(ansiblegalaxyPathUtils);
    this.ansiblegalaxyDataAccess = checkNotNull(ansiblegalaxyDataAccess);
  }

  // HACK: Workaround for known CGLIB issue, forces an Import-Package for org.sonatype.nexus.repository.config
  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    super.doValidate(configuration);
  }

  @Nullable
  @Override
  protected Content getCachedContent(final Context context) {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    if (assetKind.equals(AssetKind.API_INTERNALS)) {
      return null; // results not stored
    }

    TokenMatcher.State matcherState = ansiblegalaxyPathUtils.matcherState(context);
    switch (assetKind) {
      case VERSION_LIST:
        return getAsset(ansiblegalaxyPathUtils.versionListPath(matcherState));
      case VERSION:
        return getAsset(ansiblegalaxyPathUtils.versionPath(matcherState));
      default:
        throw new IllegalStateException("Received an invalid AssetKind of type: " + assetKind.name());
    }
  }

  @TransactionalTouchBlob
  public Content getAsset(final String assetPath) {
    StorageTx tx = UnitOfWork.currentTx();

    Asset asset = ansiblegalaxyDataAccess.findAsset(tx, tx.findBucket(getRepository()), assetPath);
    if (asset == null) {
      return null;
    }
    return ansiblegalaxyDataAccess.toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  protected Content store(final Context context, final Content content) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    log.info("----- store() assetKind: {}", assetKind.name());
    log.info("----- store() content: {}", content.toString());
    if (assetKind.equals(AssetKind.API_INTERNALS)) {
      return content; // results not stored
    }

    TokenMatcher.State matcherState = ansiblegalaxyPathUtils.matcherState(context);
    switch (assetKind) {
      case VERSION_LIST:
        return putAsset(content, ansiblegalaxyPathUtils.versionListPath(matcherState), assetKind);
      case VERSION:
        return putAsset(content, ansiblegalaxyPathUtils.versionPath(matcherState), assetKind);
      default:
        throw new IllegalStateException("Received an invalid AssetKind of type: " + assetKind.name());
    }
  }

  private Content putAsset(
      final Content content,
      final String assetPath,
      final AssetKind assetKind) throws IOException
  {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), HASH_ALGORITHMS)) {
      return ansiblegalaxyDataAccess.maybeCreateAndSaveAsset(getRepository(), assetPath, assetKind, tempBlob, content);
    }
  }

  // private Content putComponent(
  // final GolangAttributes golangAttributes,
  // final Content content,
  // final String assetPath,
  // final AssetKind assetKind) throws IOException
  // {
  // StorageFacet storageFacet = facet(StorageFacet.class);
  // try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), HASH_ALGORITHMS)) {
  // return ansiblegalaxyDataAccess.maybeCreateAndSaveComponent(getRepository(), golangAttributes, assetPath, tempBlob,
  // content, assetKind);
  // }
  // }

  @Override
  protected void indicateVerified(
      final Context context,
      final Content content,
      final CacheInfo cacheInfo) throws IOException
  {
    setCacheInfo(content, cacheInfo);
  }

  @TransactionalTouchMetadata
  public void setCacheInfo(final Content content, final CacheInfo cacheInfo) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = Content.findAsset(tx, tx.findBucket(getRepository()), content);
    if (asset == null) {
      log.debug("Attempting to set cache info for non-existent AnsibleGalaxy asset {}",
          content.getAttributes().require(Asset.class));
      return;
    }
    log.debug("Updating cacheInfo of {} to {}", asset, cacheInfo);
    CacheInfo.applyToAsset(asset, cacheInfo);
    tx.saveAsset(asset);
  }

  @Override
  protected String getUrl(@Nonnull final Context context) {
    return context.getRequest().getPath().substring(1);
  }
}
