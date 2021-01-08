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

import java.util.Map;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class QueryTokenMatcherTest
    extends TestSupport
{

  private static final String PATH = "/api/v2/collections/azure/azcollection/versions/";

  private QueryTokenMatcher underTest;

  private Parameters parameters;

  private AttributesMap attributes;

  @Mock
  Request request;

  @Mock
  Context context;

  @Before
  public void setUp() {
    underTest = null;
    parameters = new Parameters();
    attributes = new AttributesMap();
    when(request.getParameters()).thenReturn(parameters);
    when(request.getPath()).thenReturn(PATH);
    when(context.getRequest()).thenReturn(request);
    when(context.getAttributes()).thenReturn(attributes);
  }

  @Test
  public void testNonQueryUriWithoutQueryParams() {
    underTest = new QueryTokenMatcher("/api/{apiversion}/collections/{author}/{module}/versions/");
    boolean matches = underTest.matches(context);
    assertTrue(matches);

    State state = context.getAttributes().get(State.class);
    assertThat(state, notNullValue());
    Map<String, String> tokens = state.getTokens();
    assertTrue(MapUtils.isNotEmpty(tokens));
    assertThat(tokens.get("apiversion"), is(equalTo("v2")));
    assertThat(tokens.get("author"), is(equalTo("azure")));
    assertThat(tokens.get("module"), is(equalTo("azcollection")));
    assertThat(tokens.get("pagenum"), nullValue());
  }

  @Test
  public void testNonQueryUriWithQueryParam() {
    underTest = new QueryTokenMatcher("/api/{apiversion}/collections/{author}/{module}/versions/");
    parameters.set("test", "abc");
    boolean matches = underTest.matches(context);
    assertTrue(matches);

    State state = context.getAttributes().get(State.class);
    assertThat(state, notNullValue());
    Map<String, String> tokens = state.getTokens();
    assertTrue(MapUtils.isNotEmpty(tokens));
    assertThat(tokens.get("apiversion"), is(equalTo("v2")));
    assertThat(tokens.get("author"), is(equalTo("azure")));
    assertThat(tokens.get("module"), is(equalTo("azcollection")));
    assertThat(tokens.get("test"), nullValue());
  }

  @Test
  public void testQueryUriWithoutQueryParam() {
    underTest = new QueryTokenMatcher("/api/{apiversion}/collections/{author}/{module}/versions/",
        ImmutableMap.of("page", "pagenum"));
    boolean matches = underTest.matches(context);
    assertTrue(matches);

    State state = context.getAttributes().get(State.class);
    assertThat(state, notNullValue());
    Map<String, String> tokens = state.getTokens();
    assertTrue(MapUtils.isNotEmpty(tokens));
    assertThat(tokens.get("apiversion"), is(equalTo("v2")));
    assertThat(tokens.get("author"), is(equalTo("azure")));
    assertThat(tokens.get("module"), is(equalTo("azcollection")));
    assertThat(tokens.get("pagenum"), nullValue());
  }

  @Test
  public void testQueryUriWithUnmatchingQueryParam() {
    underTest = new QueryTokenMatcher("/api/{apiversion}/collections/{author}/{module}/versions/",
        ImmutableMap.of("page", "pagenum"));
    parameters.set("test", "abc");
    boolean matches = underTest.matches(context);
    assertTrue(matches);

    State state = context.getAttributes().get(State.class);
    assertThat(state, notNullValue());
    Map<String, String> tokens = state.getTokens();
    assertTrue(MapUtils.isNotEmpty(tokens));
    assertThat(tokens.get("apiversion"), is(equalTo("v2")));
    assertThat(tokens.get("author"), is(equalTo("azure")));
    assertThat(tokens.get("module"), is(equalTo("azcollection")));
    assertThat(tokens.get("pagenum"), nullValue());
  }

  @Test
  public void testQueryUriWithMatchingQueryParam() {
    underTest = new QueryTokenMatcher("/api/{apiversion}/collections/{author}/{module}/versions/",
        ImmutableMap.of("page", "pagenum"));
    parameters.set("page", "5");
    boolean matches = underTest.matches(context);
    assertTrue(matches);

    State state = context.getAttributes().get(State.class);
    assertThat(state, notNullValue());
    Map<String, String> tokens = state.getTokens();
    assertTrue(MapUtils.isNotEmpty(tokens));
    assertThat(tokens.get("apiversion"), is(equalTo("v2")));
    assertThat(tokens.get("author"), is(equalTo("azure")));
    assertThat(tokens.get("module"), is(equalTo("azcollection")));
    assertThat(tokens.get("pagenum"), is(equalTo("5")));
  }

  @Test
  public void testWithMultipleQueryParam() {
    underTest = new QueryTokenMatcher("/api/{apiversion}/collections/{author}/{module}/versions/",
        ImmutableMap.of("page", "pagenum"));
    parameters.set("abc", "test");
    parameters.set("page", "5");
    boolean matches = underTest.matches(context);
    assertTrue(matches);

    State state = context.getAttributes().get(State.class);
    assertThat(state, notNullValue());
    Map<String, String> tokens = state.getTokens();
    assertTrue(MapUtils.isNotEmpty(tokens));
    assertThat(tokens.get("apiversion"), is(equalTo("v2")));
    assertThat(tokens.get("author"), is(equalTo("azure")));
    assertThat(tokens.get("module"), is(equalTo("azcollection")));
    assertThat(tokens.get("pagenum"), is(equalTo("5")));
  }

}
