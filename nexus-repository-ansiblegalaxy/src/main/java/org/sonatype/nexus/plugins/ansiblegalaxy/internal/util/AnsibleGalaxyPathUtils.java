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

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility methods for working with AnsibleGalaxy routes and paths.
 */
@Named
@Singleton
public class AnsibleGalaxyPathUtils
{
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

  public TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class);
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

  public String versionListPath(final State matcherState) {
    String author = author(matcherState);
    String module = module(matcherState);

    return String.format("%s/%s/versions", author, module);
  }

  public String versionPath(final State matcherState) {
    String author = author(matcherState);
    String module = module(matcherState);
    String version = version(matcherState);

    return String.format("%s/%s/%s", author, module, version);
  }
}
