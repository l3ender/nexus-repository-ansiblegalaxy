/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2020-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype
 * .com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License
 * Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are
 * trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.ansiblegalaxy.internal

import javax.inject.Inject
import javax.inject.Provider

import org.sonatype.goodies.common.Loggers
import org.sonatype.nexus.plugins.ansiblegalaxy.AssetKind
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.security.AnsibleGalaxySecurityFacet
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.QueryTokenMatcher
import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.RecipeSupport
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.attributes.AttributesFacet
import org.sonatype.nexus.repository.cache.NegativeCacheFacet
import org.sonatype.nexus.repository.cache.NegativeCacheHandler
import org.sonatype.nexus.repository.http.PartialFetchHandler
import org.sonatype.nexus.repository.httpclient.HttpClientFacet
import org.sonatype.nexus.repository.purge.PurgeUnusedFacet
import org.sonatype.nexus.repository.routing.RoutingRuleHandler
import org.sonatype.nexus.repository.search.SearchFacet
import org.sonatype.nexus.repository.security.SecurityHandler
import org.sonatype.nexus.repository.storage.DefaultComponentMaintenanceImpl
import org.sonatype.nexus.repository.storage.StorageFacet
import org.sonatype.nexus.repository.storage.UnitOfWorkHandler
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Context
import org.sonatype.nexus.repository.view.Matcher
import org.sonatype.nexus.repository.view.handlers.ConditionalRequestHandler
import org.sonatype.nexus.repository.view.handlers.ContentHeadersHandler
import org.sonatype.nexus.repository.view.handlers.ExceptionHandler
import org.sonatype.nexus.repository.view.handlers.HandlerContributor
import org.sonatype.nexus.repository.view.handlers.LastDownloadedHandler
import org.sonatype.nexus.repository.view.handlers.TimingHandler
import org.sonatype.nexus.repository.view.matchers.ActionMatcher
import org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher

import org.slf4j.Logger

import static org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyPathUtils.ROLE_ARTIFACT_MODULE_PARAM_NAME
import static org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyPathUtils.ROLE_ARTIFACT_URI_PREFIX
import static org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyPathUtils.ROLE_VERSION_URI_SUFFIX
import static org.sonatype.nexus.repository.http.HttpMethods.GET
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD

/**
 * Support for AnsibleGalaxy recipes.
 */
abstract class AnsibleGalaxyRecipeSupport
extends RecipeSupport {
  private static final Logger LOG = Loggers.getLogger(AnsibleGalaxyRecipeSupport.class)

  @Inject
  Provider<AnsibleGalaxySecurityFacet> securityFacet

  @Inject
  Provider<ConfigurableViewFacet> viewFacet

  @Inject
  Provider<StorageFacet> storageFacet

  @Inject
  Provider<SearchFacet> searchFacet

  @Inject
  Provider<AttributesFacet> attributesFacet

  @Inject
  ExceptionHandler exceptionHandler

  @Inject
  TimingHandler timingHandler

  @Inject
  SecurityHandler securityHandler

  @Inject
  PartialFetchHandler partialFetchHandler

  @Inject
  ConditionalRequestHandler conditionalRequestHandler

  @Inject
  ContentHeadersHandler contentHeadersHandler

  @Inject
  UnitOfWorkHandler unitOfWorkHandler

  @Inject
  HandlerContributor handlerContributor

  @Inject
  Provider<DefaultComponentMaintenanceImpl> componentMaintenanceFacet

  @Inject
  Provider<HttpClientFacet> httpClientFacet

  @Inject
  Provider<PurgeUnusedFacet> purgeUnusedFacet

  @Inject
  Provider<NegativeCacheFacet> negativeCacheFacet

  @Inject
  NegativeCacheHandler negativeCacheHandler

  @Inject
  RoutingRuleHandler routingRuleHandler

  @Inject
  LastDownloadedHandler lastDownloadedHandler

  protected AnsibleGalaxyRecipeSupport(final Type type, final Format format) {
    super(type, format)
  }

  static Matcher apiInternalsMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new TokenMatcher("/api"),
        setAssetKind(AssetKind.API_INTERNALS)
        )
  }

  static Matcher collectionDetailMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new TokenMatcher("/api/{apiversion}/collections/{author}/{module}/"),
        setAssetKind(AssetKind.COLLECTION_DETAIL)
        )
  }

  static Matcher collectionVersionListMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new QueryTokenMatcher("/api/{apiversion}/collections/{author}/{module}/versions/", [page: "pagenum"]),
        setAssetKind(AssetKind.COLLECTION_VERSION_LIST)
        )
  }

  static Matcher collectionVersionDetailMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new TokenMatcher("/api/{apiversion}/collections/{author}/{module}/versions/{version}/"),
        setAssetKind(AssetKind.COLLECTION_VERSION_DETAIL)
        )
  }

  static Matcher collectionArtifactMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new TokenMatcher("/download/{author}-{module}-{version}.tar.gz"),
        setAssetKind(AssetKind.COLLECTION_ARTIFACT)
        )
  }

  static Matcher roleSearchMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new QueryTokenMatcher("/api/{apiversion}/roles/", [owner__username: "author", name: "module"]),
        setAssetKind(AssetKind.ROLE_SEARCH)
        )
  }

  static Matcher roleDetailMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new TokenMatcher("/api/{apiversion}/roles/{id}/"),
        setAssetKind(AssetKind.ROLE_DETAIL)
        )
  }

  static Matcher roleVersionListMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new QueryTokenMatcher("/api/{apiversion}/roles/{id}/${ROLE_VERSION_URI_SUFFIX}", [page: "pagenum"]),
        setAssetKind(AssetKind.ROLE_VERSION_LIST)
        )
  }

  static Matcher roleArtifactMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new QueryTokenMatcher(ROLE_ARTIFACT_URI_PREFIX + "/{author}/{reponame}/archive/{version}.tar.gz", [(ROLE_ARTIFACT_MODULE_PARAM_NAME): "module"]),
        setAssetKind(AssetKind.ROLE_ARTIFACT)
        )
  }

  private static Matcher setAssetKind(AssetKind assetKind) {
    return new Matcher() {
          @Override
          boolean matches(final Context context) {
            LOG.debug("{} matches {}", assetKind, context.getRequest())
            context.attributes.set(AssetKind.class, assetKind)
            return true;
          }
        }
  }
}
