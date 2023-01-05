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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.sonatype.goodies.common.Loggers;
import org.sonatype.nexus.plugins.ansiblegalaxy.AssetKind;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.metadata.AnsibleGalaxyAttributes;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer.*;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyDataAccess;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyPathUtils;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.JsonSearcher;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.StreamUtils;
import org.sonatype.nexus.repository.cache.CacheInfo;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.proxy.ProxyFacet;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.*;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.payloads.TempBlob;
import org.sonatype.nexus.transaction.UnitOfWork;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyDataAccess.HASH_ALGORITHMS;
import static org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyPathUtils.*;

/**
 * AnsibleGalaxy {@link ProxyFacet} implementation.
 *
 * @since 0.0.1
 */
@Named
public class AnsibleGalaxyProxyFacetImpl
        extends ProxyFacetSupport
        implements AnsibleGalaxyProxyFacet {
    private final Logger log = Loggers.getLogger(getClass());

    private final AnsibleGalaxyPathUtils ansiblegalaxyPathUtils;

    private final AnsibleGalaxyDataAccess ansiblegalaxyDataAccess;

    @Inject
    public AnsibleGalaxyProxyFacetImpl(
            final AnsibleGalaxyPathUtils ansiblegalaxyPathUtils,
            final AnsibleGalaxyDataAccess ansiblegalaxyDataAccess) {
        this.ansiblegalaxyPathUtils = checkNotNull(ansiblegalaxyPathUtils);
        this.ansiblegalaxyDataAccess = checkNotNull(ansiblegalaxyDataAccess);
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

    // HACK: Workaround for known CGLIB issue, forces an Import-Package for org.sonatype.nexus.repository.config
    @Override
    protected void doValidate(final Configuration configuration) throws Exception {
        super.doValidate(configuration);
    }

    @Nullable
    @Override
    protected Content getCachedContent(final Context context) {
        AssetKind assetKind = context.getAttributes().require(AssetKind.class);

        TokenMatcher.State matcherState = ansiblegalaxyPathUtils.matcherState(context);
        switch (assetKind) {
            case API_METADATA:
                return getAsset(ansiblegalaxyPathUtils.apiMetadataPath(matcherState));
            case COLLECTION_DETAIL:
                return getAsset(ansiblegalaxyPathUtils.collectionDetailPath(matcherState));
            case COLLECTION_VERSION_LIST:
                return getAsset(ansiblegalaxyPathUtils.collectionVersionPagedPath(matcherState));
            case COLLECTION_VERSION_DETAIL:
                return getAsset(ansiblegalaxyPathUtils.collectionVersionDetailPath(matcherState));
            case COLLECTION_ARTIFACT:
                return getAsset(ansiblegalaxyPathUtils.collectionArtifactPath(matcherState));
            case ROLE_SEARCH:
                return getAsset(ansiblegalaxyPathUtils.roleSearchPath(matcherState));
            case ROLE_DETAIL:
                return getAsset(ansiblegalaxyPathUtils.roleDetailPath(matcherState));
            case ROLE_VERSION_LIST:
                return getAsset(ansiblegalaxyPathUtils.roleVersionPagedPath(matcherState));
            case ROLE_ARTIFACT:
                return getAsset(ansiblegalaxyPathUtils.roleArtifactPath(matcherState));
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

        TokenMatcher.State matcherState = ansiblegalaxyPathUtils.matcherState(context);
        switch (assetKind) {
            case API_METADATA:
                return putAsset(context, content, ansiblegalaxyPathUtils.apiMetadataPath(matcherState), assetKind);
            case COLLECTION_DETAIL:
                return putAsset(context, content, ansiblegalaxyPathUtils.collectionDetailPath(matcherState), assetKind);
            case COLLECTION_VERSION_LIST:
                return putAsset(context, content, ansiblegalaxyPathUtils.collectionVersionPagedPath(matcherState), assetKind);
            case COLLECTION_VERSION_DETAIL:
                return putComponent(context, ansiblegalaxyPathUtils.getCollectionAttributes(matcherState), content,
                        ansiblegalaxyPathUtils.collectionVersionDetailPath(matcherState), assetKind);
            case COLLECTION_ARTIFACT:
                return putComponent(context, ansiblegalaxyPathUtils.getCollectionAttributes(matcherState), content,
                        ansiblegalaxyPathUtils.collectionArtifactPath(matcherState), assetKind);
            case ROLE_SEARCH:
                return putAsset(context, content, ansiblegalaxyPathUtils.roleSearchPath(matcherState), assetKind);
            case ROLE_DETAIL:
                return putAsset(context, content, ansiblegalaxyPathUtils.roleDetailPath(matcherState), assetKind);
            case ROLE_VERSION_LIST:
                return putAsset(context, content, ansiblegalaxyPathUtils.roleVersionPagedPath(matcherState), assetKind);
            case ROLE_ARTIFACT:
                return putComponent(context, ansiblegalaxyPathUtils.getRoleAttributes(matcherState), content,
                        ansiblegalaxyPathUtils.roleArtifactPath(matcherState), assetKind);
            default:
                throw new IllegalStateException("Received an invalid AssetKind of type: " + assetKind.name());
        }
    }

    private Content putAsset(
            final Context context,
            final Content content,
            final String assetPath,
            final AssetKind assetKind) throws IOException {
        StorageFacet storageFacet = facet(StorageFacet.class);
        try (InputStream updatedStream = getUpdatedContent(context, assetKind, content.openInputStream())) {
            try (TempBlob tempBlob = storageFacet.createTempBlob(updatedStream, HASH_ALGORITHMS)) {
                return ansiblegalaxyDataAccess.maybeCreateAndSaveAsset(getRepository(), assetPath, assetKind, tempBlob,
                        content);
            }
        }
    }

    private Content putComponent(
            final Context context,
            final AnsibleGalaxyAttributes ansibleGalaxyAttributes,
            final Content content,
            final String assetPath,
            final AssetKind assetKind) throws IOException {
        StorageFacet storageFacet = facet(StorageFacet.class);
        try (InputStream updatedStream = getUpdatedContent(context, assetKind, content.openInputStream())) {
            try (TempBlob tempBlob = storageFacet.createTempBlob(updatedStream, HASH_ALGORITHMS)) {
                return ansiblegalaxyDataAccess.maybeCreateAndSaveComponent(getRepository(), ansibleGalaxyAttributes, assetPath,
                        tempBlob, content, assetKind);
            }
        }
    }

    private InputStream getUpdatedContent(Context context, AssetKind assetKind, InputStream in) throws IOException {
        if (assetKind == AssetKind.COLLECTION_ARTIFACT || assetKind == AssetKind.ROLE_ARTIFACT) {
            return in; // do not modify
        } else if (assetKind == AssetKind.ROLE_VERSION_LIST) {
            List<Replacer> replacers = new ArrayList<>();
            // add nxrm context path to paging URI:
            final String pageFieldName = "next_link";
            replacers.add(new JsonPrependReplacer(pageFieldName, "/repository/" + getRepository().getName()));

            // replace artifact download links so they are handled by this proxy repo:
            final String downloadUrlFieldName = "download_url";
            replacers.add(new JsonSearchReplacer(downloadUrlFieldName, getRoleArtifactUpstreamUrl(),
                    getRepository().getUrl() + ROLE_ARTIFACT_URI_PREFIX));

            // update download links to include data used by nxrm for storage:
            String moduleName = getModuleName(context);
            if (StringUtils.isNotBlank(moduleName)) {
                String queryString = String.format("?%s=%s", ROLE_ARTIFACT_MODULE_PARAM_NAME, moduleName);
                replacers.add(new JsonPostpendReplacer(downloadUrlFieldName, queryString));
            }

            return new ReplacerStream(replacers).getReplacedContent(in);
        }

        // default: replace all upstream URLs with repo URLs
        SearchReplacer urlReplacer = new SearchReplacer(getRemoteUrl().toString(), getRepository().getUrl() + "/");
        return new ReplacerStream(urlReplacer).getReplacedContent(in);
    }

    private String getModuleName(Context context) {
        String path = context.getRequest().getPath().replaceFirst(ROLE_VERSION_URI_SUFFIX, "");
        Request roleDetailRequest = new Request.Builder().copy(context.getRequest()).action(HttpMethods.GET).path(path)
                .parameters(new Parameters()).build();
        Context roleDetailContext = new Context(context.getRepository(), roleDetailRequest);
        roleDetailContext.getAttributes().set(AssetKind.class, AssetKind.ROLE_DETAIL);
        try {
            final String moduleFieldName = "name";
            log.info("searching for '{}' via: '{}'", moduleFieldName, roleDetailRequest);
            final Response response = context.getRepository().facet(ViewFacet.class).dispatch(roleDetailRequest, context);
            try (InputStream stream = response.getPayload().openInputStream()) {
                return new JsonSearcher(moduleFieldName).getValue(StreamUtils.toString(stream));
            }
        } catch (Exception e) {
            log.error("can't load {}", roleDetailRequest, e);
        }
        return null;
    }

    @Override
    protected void indicateVerified(
            final Context context,
            final Content content,
            final CacheInfo cacheInfo) throws IOException {
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
