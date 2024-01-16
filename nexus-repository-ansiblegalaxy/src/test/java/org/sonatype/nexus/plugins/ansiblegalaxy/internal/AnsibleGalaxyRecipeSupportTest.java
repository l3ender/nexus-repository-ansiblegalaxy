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
package org.sonatype.nexus.plugins.ansiblegalaxy.internal;

import java.util.Map;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Matcher;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;

import com.google.common.collect.ImmutableListMultimap;
import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.ansiblegalaxy.internal.AnsibleGalaxyRecipeSupport.collectionArtifactMatcher;
import static org.sonatype.nexus.plugins.ansiblegalaxy.internal.AnsibleGalaxyRecipeSupport.roleArtifactMatcher;

public class AnsibleGalaxyRecipeSupportTest
    extends TestSupport
{

  @Mock
  AttributesMap attributes;

  @Mock
  Request request;

  @Mock
  Context context;

  @Before
  public void setUp() {
    attributes = new AttributesMap();
    when(request.getAction()).thenReturn("GET");
    when(context.getRequest()).thenReturn(request);
    when(context.getAttributes()).thenReturn(attributes);
  }

  @Test
  public void testCollectionArtifactTar() {
    when(request.getPath()).thenReturn("/api/v3/plugin/ansible/content/published/collections/artifacts/azure-azcollection-1.3.1.tar.gz");
    Matcher matcher = collectionArtifactMatcher();
    boolean matches = matcher.matches(context);
    assertTrue(matches);

    State state = context.getAttributes().get(State.class);
    assertThat(state, notNullValue());
    Map<String, String> tokens = state.getTokens();
    assertTrue(MapUtils.isNotEmpty(tokens));
    assertThat(tokens.get("author"), is(equalTo("azure")));
    assertThat(tokens.get("module"), is(equalTo("azcollection")));
    assertThat(tokens.get("version"), is(equalTo("1.3.1")));
  }

  @Test
  public void testCollectionBranchArtifactTar() {
    when(request.getPath()).thenReturn("/api/v3/plugin/ansible/content/published/collections/artifacts/azure-azcollection-master.tar.gz");
    Matcher matcher = collectionArtifactMatcher();
    boolean matches = matcher.matches(context);
    assertTrue(matches);

    State state = context.getAttributes().get(State.class);
    assertThat(state, notNullValue());
    Map<String, String> tokens = state.getTokens();
    assertTrue(MapUtils.isNotEmpty(tokens));
    assertThat(tokens.get("author"), is(equalTo("azure")));
    assertThat(tokens.get("module"), is(equalTo("azcollection")));
    assertThat(tokens.get("version"), is(equalTo("master")));
  }

  @Test
  public void testRoleArtifact() {
    when(request.getPath()).thenReturn("/download/role/geerlingguy/ansible-role-jenkins/archive/2.6.0.tar.gz");
    when(request.getParameters()).thenReturn(new Parameters(ImmutableListMultimap.of("module", "jenkins")));
    Matcher matcher = roleArtifactMatcher();
    boolean matches = matcher.matches(context);
    assertTrue(matches);

    State state = context.getAttributes().get(State.class);
    assertThat(state, notNullValue());
    Map<String, String> tokens = state.getTokens();
    assertTrue(MapUtils.isNotEmpty(tokens));
    assertThat(tokens.get("author"), is(equalTo("geerlingguy")));
    assertThat(tokens.get("module"), is(equalTo("jenkins")));
    assertThat(tokens.get("version"), is(equalTo("2.6.0")));
  }

}
