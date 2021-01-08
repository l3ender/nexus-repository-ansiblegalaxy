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

import java.io.IOException;

import org.sonatype.goodies.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonSearcherTest
    extends TestSupport
{

  private static final String JSON =
      "{\"key1\":\"value1\",\"key2\":1,\"key3\":{\"a\":true,\"b\":\"aaa\",\"id\":\"xyz\"},\"key4\":\"abc\",\"key5\":false,\"id\":\"CCC\",\"nullprop\":null}";

  @Test(expected = IOException.class)
  public void testInvalidJson() throws IOException {
    String input = "abc 123";

    new JsonSearcher("key1").getValue(input);
  }

  @Test
  public void testEmptyJson() throws IOException {
    String input = "{}";

    String result = new JsonSearcher("prop").getValue(input);

    assertThat(result, nullValue());
  }

  @Test
  public void testPropNotFound() throws IOException {
    String result = new JsonSearcher("prop").getValue(JSON);

    assertThat(result, nullValue());
  }

  @Test
  public void testTopLevelProp() throws IOException {
    String result = new JsonSearcher("key1").getValue(JSON);

    assertThat(result, is(equalTo("value1")));
  }

  @Test
  public void testNullProp() throws IOException {
    String result = new JsonSearcher("nullprop").getValue(JSON);

    assertThat(result, nullValue());
  }

  @Test
  public void testNestedLevelPropReplace() throws IOException {
    String result = new JsonSearcher("b").getValue(JSON);

    assertThat(result, is(equalTo("aaa")));
  }

  @Test
  public void testFirstResult() throws IOException {
    String result = new JsonSearcher("id").getValue(JSON);

    assertThat(result, is(equalTo("CCC")));
  }

}
