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
import java.io.InputStream;
import java.net.URI;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.goodies.common.Loggers;
import org.sonatype.nexus.plugins.ansiblegalaxy.AssetKind;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.metadata.AnsibleGalaxyAttributes;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer.JsonPrependReplacer;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer.JsonSearchReplacer;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer.ReplacerStream;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer.SearchReplacer;
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
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.payloads.TempBlob;
import org.sonatype.nexus.transaction.UnitOfWork;

import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyDataAccess.HASH_ALGORITHMS;
import static org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyPathUtils.ROLE_ARTIFACT_URI_PREFIX;

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

    if (assetKind == AssetKind.API_INTERNALS) {
      return null; // results not stored
    }

    TokenMatcher.State matcherState = ansiblegalaxyPathUtils.matcherState(context);
    switch (assetKind) {
      case ROLE:
      case COLLECTION_VERSION_LIST:
        return getAsset(ansiblegalaxyPathUtils.modulePagedPath(matcherState));
      case COLLECTION_VERSION:
        return getAsset(ansiblegalaxyPathUtils.versionPath(matcherState));
      case ROLE_VERSION_LIST:
        return getAsset(ansiblegalaxyPathUtils.roleMetadataPagedPath(matcherState));
      case ARTIFACT:
        return getAsset(ansiblegalaxyPathUtils.artifactPath(matcherState));
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

    if (assetKind == AssetKind.API_INTERNALS) {
      return content; // results not stored
    }

    TokenMatcher.State matcherState = ansiblegalaxyPathUtils.matcherState(context);
    switch (assetKind) {
      case ROLE:
      case COLLECTION_VERSION_LIST:
        return putAsset(content, ansiblegalaxyPathUtils.modulePagedPath(matcherState), assetKind);
      case COLLECTION_VERSION:
        return putComponent(ansiblegalaxyPathUtils.getAttributesFromMatcherState(matcherState), content,
            ansiblegalaxyPathUtils.versionPath(matcherState), assetKind);
      case ROLE_VERSION_LIST:
        return putAsset(content, ansiblegalaxyPathUtils.roleMetadataPagedPath(matcherState), assetKind);
      case ARTIFACT:
        return putComponent(ansiblegalaxyPathUtils.getAttributesFromMatcherState(matcherState), content,
            ansiblegalaxyPathUtils.artifactPath(matcherState), assetKind);
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
    try (InputStream updatedStream = getUpdatedContent(assetKind, content.openInputStream())) {
      try (TempBlob tempBlob = storageFacet.createTempBlob(updatedStream, HASH_ALGORITHMS)) {
        return ansiblegalaxyDataAccess.maybeCreateAndSaveAsset(getRepository(), assetPath, assetKind, tempBlob,
            content);
      }
    }
  }

  private Content putComponent(
      final AnsibleGalaxyAttributes ansibleGalaxyAttributes,
      final Content content,
      final String assetPath,
      final AssetKind assetKind) throws IOException
  {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (InputStream updatedStream = getUpdatedContent(assetKind, content.openInputStream())) {
      try (TempBlob tempBlob = storageFacet.createTempBlob(updatedStream, HASH_ALGORITHMS)) {
        return ansiblegalaxyDataAccess.maybeCreateAndSaveComponent(getRepository(), ansibleGalaxyAttributes, assetPath,
            tempBlob, content, assetKind);
      }
    }
  }

  private InputStream getUpdatedContent(AssetKind assetKind, InputStream in) throws IOException {
    if (assetKind == AssetKind.ARTIFACT) {
      return in; // do not modify
    }
    else if (assetKind == AssetKind.ROLE_VERSION_LIST) {
      // add nxrm context path to paging URI:
      JsonPrependReplacer pageReplacer =
          new JsonPrependReplacer("next_link", "/repository/" + getRepository().getName());

      // replace artifact download links so they are handled by this proxy repo:
      JsonSearchReplacer downloadReplacer = new JsonSearchReplacer("download_url", getRoleArtifactUpstreamUrl(),
          getRepository().getUrl() + ROLE_ARTIFACT_URI_PREFIX);

      return new ReplacerStream(pageReplacer, downloadReplacer).getReplacedContent(in);
    }

    // default: replace all upstream URLs with repo URLs
    SearchReplacer urlReplacer = new SearchReplacer(getRemoteUrl().toString(), getRepository().getUrl() + "/");
    return new ReplacerStream(urlReplacer).getReplacedContent(in);
  }

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
    String uri = getUri(context.getRequest()).substring(1);
    log.debug("uri for upstream request: {}", uri);
    return uri;
  }

  /**
   * Returns relative URI, including query parameters.
   */
  private static String getUri(final Request request) {
    StringBuilder sb = new StringBuilder(request.getPath());
    Parameters params = request.getParameters();
    if (null != params && !params.isEmpty()) {
      sb.append("?");

      String queryString = params.entries().stream().map(param -> param.getKey() + "=" + param.getValue())
          .collect(Collectors.joining("&"));
      sb.append(queryString);
    }

    return sb.toString();
  }

  @Override
  protected HttpRequestBase buildFetchHttpRequest(URI uri, Context context) {
    if (context.getRequest().getPath().startsWith(ROLE_ARTIFACT_URI_PREFIX)) {
      log.debug("rebuilding role artifact request for {}; orig = {}", context.getRequest(), uri);
      URI newUri = ansiblegalaxyPathUtils.rebuildRoleDownloadUri(uri, getRoleArtifactUpstreamUrl());
      log.debug("updated fetch request: {} -> {}", uri, newUri);
      return super.buildFetchHttpRequest(newUri, context);
    }
    return super.buildFetchHttpRequest(uri, context);
  }

  private String getRoleArtifactUpstreamUrl() {
    // TODO: this should be a configurable repository item entered by user on UI
    return "https://github.com";
  }

}
