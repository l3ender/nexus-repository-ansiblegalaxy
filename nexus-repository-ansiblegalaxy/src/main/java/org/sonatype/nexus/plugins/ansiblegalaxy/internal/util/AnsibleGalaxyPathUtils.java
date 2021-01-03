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
package org.sonatype.nexus.plugins.ansiblegalaxy.internal.util;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.Loggers;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.metadata.AnsibleGalaxyAttributes;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility methods for working with AnsibleGalaxy routes and paths.
 */
@Named
@Singleton
public class AnsibleGalaxyPathUtils
{

  public static final String ROLE_ARTIFACT_URI_PREFIX = "/download/role";

  private final Logger log = Loggers.getLogger(getClass());

  /**
   * Returns the author from a {@link TokenMatcher.State}.
   */
  public String author(final TokenMatcher.State state) {
    return match(state, "author");
  }

  public String module(final TokenMatcher.State state) {
    return match(state, "module");
  }

  public String version(final TokenMatcher.State state) {
    return match(state, "version");
  }

  public String id(final TokenMatcher.State state) {
    return match(state, "id");
  }

  public TokenMatcher.State matcherState(final Context context) {
    State state = context.getAttributes().require(TokenMatcher.State.class);
    log.info("matched state tokens: {}", state.getTokens());
    return state;
  }

  /**
   * Utility method encapsulating getting a particular token by name from a matcher, including preconditions.
   */
  private String match(final TokenMatcher.State state, final String name) {
    checkNotNull(state);
    String result = state.getTokens().get(name);
    checkNotNull(result);
    return result;
  }

  public String modulePagedPath(final State matcherState) {
    String author = author(matcherState);
    String module = module(matcherState);
    String page = StringUtils.defaultIfBlank(matcherState.getTokens().get("pagenum"), "1");

    return String.format("%s/%s/info%s", author, module, page);
  }

  public String idPagedPath(final State matcherState) {
    String id = id(matcherState);
    String page = StringUtils.defaultIfBlank(matcherState.getTokens().get("pagenum"), "1");

    return String.format("%s/info%s", id, page);
  }

  public String versionPath(final State matcherState) {
    String author = author(matcherState);
    String module = module(matcherState);
    String version = version(matcherState);

    return String.format("%s/%s/%s/info", author, module, version);
  }

  public String artifactPath(final State matcherState) {
    String author = author(matcherState);
    String module = module(matcherState);
    String version = version(matcherState);

    return String.format("%s/%s/%s/artifact", author, module, version);
  }

  public AnsibleGalaxyAttributes getAttributesFromMatcherState(final TokenMatcher.State state) {
    return new AnsibleGalaxyAttributes(author(state), module(state), version(state));
  }

  /**
   * Used by proxy handler to send request to actual/real download location, which can be different than the proxy
   * repo's upstream endpoint.
   * 
   * @param originalUpstream example:
   *          https://galaxy.ansible.com/download/role/geerlingguy/ansible-role-jenkins/archive/3.0.0.tar.gz
   * @param desiredUpstreamUrl example: https://github.com
   * @return example: https://github.com/geerlingguy/ansible-role-jenkins/archive/3.0.0.tar.gz
   */
  public URI rebuildRoleDownloadUri(URI originalUpstream, String desiredUpstreamUrl) {
    final String schemeSeparator = "://";
    String desiredScheme = StringUtils.substringBefore(desiredUpstreamUrl, schemeSeparator);
    String desiredUrlWithoutScheme = StringUtils.substringAfter(desiredUpstreamUrl, schemeSeparator);

    final String pathSeparator = "/";
    String desiredHost = StringUtils.substringBefore(desiredUrlWithoutScheme, pathSeparator);
    String desiredPath = StringUtils.substringAfter(desiredUrlWithoutScheme, pathSeparator);

    if (desiredPath.endsWith(pathSeparator)) {
      desiredPath = StringUtils.chop(desiredPath);
    }

    String upstreamPath = originalUpstream.getPath().replaceFirst(ROLE_ARTIFACT_URI_PREFIX, "");

    String path = upstreamPath;
    if (StringUtils.isNotBlank(desiredPath)) {
      path = "/" + desiredPath + path;
    }

    try {
      return new URIBuilder(originalUpstream).setScheme(desiredScheme).setHost(desiredHost).setPath(path).build();
    }
    catch (URISyntaxException e) {
      log.error("cannot update http request from {} -> {}", originalUpstream, desiredUpstreamUrl, e);
      return originalUpstream;
    }
  }

}
