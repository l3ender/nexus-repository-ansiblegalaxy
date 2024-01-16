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
import java.util.HashMap;
import java.util.Map;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.metadata.AnsibleGalaxyAttributes;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
  public void collectionDetailPath() {
    String result = underTest.collectionDetailPath(state);

    assertThat(result, is(equalTo("collection/azure/azcollection/detail.json")));
  }

  @Test
  public void collectionVersionListPath() {
    String result = underTest.collectionVersionPagedPath(state);

    assertThat(result, is(equalTo("collection/azure/azcollection/versions1.json")));
  }

  @Test
  public void roleSearchPath() {
    String result = underTest.roleSearchPath(state);

    assertThat(result, is(equalTo("role/azure/azcollection/roles1.json")));
  }

  @Test
  public void roleDetailPath() {
    Map<String, String> tokens = new HashMap<>();
    tokens.put("id", "444");
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.roleDetailPath(state);

    assertThat(result, is(equalTo("metadata/role/444/detail.json")));
  }

  @Test
  public void collectionVersionListPathPaged() {
    Map<String, String> tokens = defaultTokens();
    tokens.put("pagenum", "3");
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.collectionVersionPagedPath(state);

    assertThat(result, is(equalTo("collection/azure/azcollection/versions3.json")));
  }

  @Test
  public void roleSearchPathPaged() {
    Map<String, String> tokens = defaultTokens();
    tokens.put("pagenum", "2");
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.roleSearchPath(state);

    assertThat(result, is(equalTo("role/azure/azcollection/roles2.json")));
  }

  @Test
  public void collectionVersionPath() {
    String result = underTest.collectionVersionDetailPath(state);

    assertThat(result, is(equalTo("collection/azure/azcollection/1.2.0/detail.json")));
  }

  @Test
  public void roleMetadataPagedPath() {
    Map<String, String> tokens = new HashMap<>();
    tokens.put("id", "555");
    tokens.put("pagenum", "3");
    when(state.getTokens()).thenReturn(tokens);
    String result = underTest.roleVersionPagedPath(state);

    assertThat(result, is(equalTo("metadata/role/555/versions3.json")));
  }

  @Test
  public void collectionArtifactPath() {
    String result = underTest.collectionArtifactPath(state);

    assertThat(result, is(equalTo("api/v3/plugin/ansible/content/published/collections/artifacts/azure-azcollection-1.2.0.tar.gz")));
  }

  @Test
  public void collectionComponentAttributes() {
    AnsibleGalaxyAttributes result = underTest.getCollectionAttributes(state);

    assertThat(result, notNullValue());
    assertThat(result.getGroup(), is(equalTo("collection/azure")));
    assertThat(result.getName(), is(equalTo("azcollection")));
    assertThat(result.getVersion(), is(equalTo("1.2.0")));
  }

  @Test
  public void roleArtifactPath() {
    String result = underTest.roleArtifactPath(state);

    assertThat(result, is(equalTo("role/azure/azcollection/1.2.0/azure-azcollection-1.2.0.tar.gz")));
  }

  private static final String ORIG_UPSTREAM_URL =
      "https://galaxy.ansible.com/download/role/geerlingguy/ansible-role-jenkins/archive/3.0.0.tar.gz";

  private static final String REAL_UPSTREAM_PATH = "/geerlingguy/ansible-role-jenkins/archive/3.0.0.tar.gz";

  @Test
  public void roleDownloadUrlDefault() throws URISyntaxException {
    String desiredDownloadUrl = "https://github.com";
    URI originalDownloadUri = new URI(ORIG_UPSTREAM_URL);
    URI updatedDownloadUri = underTest.rebuildRoleDownloadUri(originalDownloadUri, desiredDownloadUrl);

    assertThat(updatedDownloadUri.toString(), is(equalTo("https://github.com" + REAL_UPSTREAM_PATH)));
  }

  @Test
  public void roleDownloadUrlTrailingSlash() throws URISyntaxException {
    String desiredDownloadUrl = "https://github.com/";
    URI originalDownloadUri = new URI(ORIG_UPSTREAM_URL);
    URI updatedDownloadUri = underTest.rebuildRoleDownloadUri(originalDownloadUri, desiredDownloadUrl);

    assertThat(updatedDownloadUri.toString(), is(equalTo("https://github.com" + REAL_UPSTREAM_PATH)));
  }

  @Test
  public void roleDownloadUrlCustomHost() throws URISyntaxException {
    String desiredDownloadUrl = "http://custom.example.local";
    URI originalDownloadUri = new URI(ORIG_UPSTREAM_URL);
    URI updatedDownloadUri = underTest.rebuildRoleDownloadUri(originalDownloadUri, desiredDownloadUrl);

    assertThat(updatedDownloadUri.toString(), is(equalTo("http://custom.example.local" + REAL_UPSTREAM_PATH)));
  }

  @Test
  public void roleDownloadUrlCustomHostSinglePath() throws URISyntaxException {
    String desiredDownloadUrl = "http://custom.example.local/artifacts";
    URI originalDownloadUri = new URI(ORIG_UPSTREAM_URL);
    URI updatedDownloadUri = underTest.rebuildRoleDownloadUri(originalDownloadUri, desiredDownloadUrl);

    assertThat(updatedDownloadUri.toString(),
        is(equalTo("http://custom.example.local/artifacts" + REAL_UPSTREAM_PATH)));
  }

  @Test
  public void roleDownloadUrlCustomHostSinglePathTraillingSlash() throws URISyntaxException {
    String desiredDownloadUrl = "http://custom.example.local/artifacts/";
    URI originalDownloadUri = new URI(ORIG_UPSTREAM_URL);
    URI updatedDownloadUri = underTest.rebuildRoleDownloadUri(originalDownloadUri, desiredDownloadUrl);

    assertThat(updatedDownloadUri.toString(),
        is(equalTo("http://custom.example.local/artifacts" + REAL_UPSTREAM_PATH)));
  }

  @Test
  public void roleDownloadUrlCustomHostMultiPath() throws URISyntaxException {
    String desiredDownloadUrl = "http://custom.example.local/artifacts/other";
    URI originalDownloadUri = new URI(ORIG_UPSTREAM_URL);
    URI updatedDownloadUri = underTest.rebuildRoleDownloadUri(originalDownloadUri, desiredDownloadUrl);

    assertThat(updatedDownloadUri.toString(),
        is(equalTo("http://custom.example.local/artifacts/other" + REAL_UPSTREAM_PATH)));
  }

  @Test
  public void roleDownloadUrlCustomHostMultiPathTrailingSlash() throws URISyntaxException {
    String desiredDownloadUrl = "http://custom.example.local/artifacts/other/";
    URI originalDownloadUri = new URI(ORIG_UPSTREAM_URL);
    URI updatedDownloadUri = underTest.rebuildRoleDownloadUri(originalDownloadUri, desiredDownloadUrl);

    assertThat(updatedDownloadUri.toString(),
        is(equalTo("http://custom.example.local/artifacts/other" + REAL_UPSTREAM_PATH)));
  }

  @Test
  public void roleComponentAttributes() {
    AnsibleGalaxyAttributes result = underTest.getRoleAttributes(state);

    assertThat(result, notNullValue());
    assertThat(result.getGroup(), is(equalTo("role/azure")));
    assertThat(result.getName(), is(equalTo("azcollection")));
    assertThat(result.getVersion(), is(equalTo("1.2.0")));
  }

}
