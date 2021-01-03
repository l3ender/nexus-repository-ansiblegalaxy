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

import static org.sonatype.nexus.repository.http.HttpMethods.GET
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD

/**
 * Support for AnsibleGalaxy recipes.
 */
abstract class AnsibleGalaxyRecipeSupport
extends RecipeSupport {
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
        new Matcher() {
          @Override
          boolean matches(final Context context) {
            context.attributes.set(AssetKind.class, AssetKind.API_INTERNALS)
            return true
          }
        }
        )
  }

  static Matcher collectionVersionListMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new QueryTokenMatcher("/api/{apiversion}/collections/{author}/{module}/versions/", [page: "pagenum"]),
        new Matcher() {
          @Override
          boolean matches(final Context context) {
            context.attributes.set(AssetKind.class, AssetKind.COLLECTION_VERSION_LIST)
            return true
          }
        }
        )
  }

  static Matcher collectionVersionMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new TokenMatcher("/api/{apiversion}/collections/{author}/{module}/versions/{version}/"),
        new Matcher() {
          @Override
          boolean matches(final Context context) {
            context.attributes.set(AssetKind.class, AssetKind.COLLECTION_VERSION)
            return true
          }
        }
        )
  }

  static Matcher collectionArtifactMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new TokenMatcher("/download/{author}-{module}-{version}.tar.gz"),
        new Matcher() {
          @Override
          boolean matches(final Context context) {
            context.attributes.set(AssetKind.class, AssetKind.ARTIFACT)
            return true
          }
        }
        )
  }

  static Matcher roleMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new QueryTokenMatcher("/api/{apiversion}/roles/", [owner__username: "author", name: "module"]),
        new Matcher() {
          @Override
          boolean matches(final Context context) {
            context.attributes.set(AssetKind.class, AssetKind.ROLE)
            return true
          }
        }
        )
  }

  static Matcher roleVersionListMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new QueryTokenMatcher("/api/{apiversion}/roles/{id}/versions/", [page: "pagenum"]),
        new Matcher() {
          @Override
          boolean matches(final Context context) {
            context.attributes.set(AssetKind.class, AssetKind.ROLE_VERSION_LIST)
            return true
          }
        }
        )
  }

  static Matcher roleArtifactMatcher() {
    LogicMatchers.and(
        new ActionMatcher(GET, HEAD),
        new TokenMatcher("/download/{author}/{module}/archive/{version}.tar.gz"),
        new Matcher() {
          @Override
          boolean matches(final Context context) {
            context.attributes.set(AssetKind.class, AssetKind.ARTIFACT)
            return true
          }
        }
        )
  }
}
