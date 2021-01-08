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
package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer;

import java.io.IOException;

import org.sonatype.goodies.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SearchReplacerTest
    extends TestSupport
{
  private SearchReplacer underTest;

  @Before
  public void setUp() {
    underTest = new SearchReplacer("abc", "xyz");
  }

  @Test
  public void testNoChange() throws IOException {
    String input = "xyz 123 xyz !@#$%^&*()_-<>?\"[]{};:,.+=\\|`";

    String result = underTest.getReplacedContent(input);

    assertThat(result, is(equalTo(input)));
  }

  @Test
  public void testSingleReplace() throws IOException {
    String input = "abc 123 xyz";

    String result = underTest.getReplacedContent(input);

    assertThat(result, is(equalTo("xyz 123 xyz")));
  }

  @Test
  public void testMultiReplace() throws IOException {
    String input = "abc 123 abc";

    String result = underTest.getReplacedContent(input);

    assertThat(result, is(equalTo("xyz 123 xyz")));
  }

}
