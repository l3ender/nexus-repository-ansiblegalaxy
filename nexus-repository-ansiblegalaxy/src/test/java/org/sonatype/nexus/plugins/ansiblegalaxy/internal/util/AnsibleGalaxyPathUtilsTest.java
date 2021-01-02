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

import java.util.HashMap;
import java.util.Map;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class AnsibleGalaxyPathUtilsTest
    extends TestSupport
{
  private AnsibleGalaxyPathUtils underTest;

  @Mock
  TokenMatcher.State state;

  @Before
  public void setUp() {
    underTest = new AnsibleGalaxyPathUtils();
    when(state.getTokens()).thenReturn(defaultTokens());
  }

  private Map<String, String> defaultTokens() {
    Map<String, String> tokens = new HashMap<>();
    tokens.put("author", "azure");
    tokens.put("module", "azcollection");
    tokens.put("version", "1.2.0");
    return tokens;
  }

  @Test
  public void versionListPath() {
    String result = underTest.versionListPath(state);

    assertThat(result, is(equalTo("azure/azcollection/versions1")));
  }

  @Test
  public void versionListPathPaged() {
    Map<String, String> tokens = defaultTokens();
    tokens.put("pagenum", "3");
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.versionListPath(state);

    assertThat(result, is(equalTo("azure/azcollection/versions3")));
  }

  @Test
  public void versionPath() {
    String result = underTest.versionPath(state);

    assertThat(result, is(equalTo("azure/azcollection/1.2.0/version")));
  }

  @Test
  public void artifactPath() {
    String result = underTest.artifactPath(state);

    assertThat(result, is(equalTo("azure/azcollection/1.2.0/artifact")));
  }

}
