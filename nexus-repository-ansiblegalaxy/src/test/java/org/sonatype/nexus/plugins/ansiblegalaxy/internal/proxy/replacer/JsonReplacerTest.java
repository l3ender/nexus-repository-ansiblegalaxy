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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonReplacerTest
    extends TestSupport
{

  private static final String SEARCH = "abc";

  private static final String REPLACE = "xyz";

  private static final String PREPEND = "123";

  private static final String JSON =
      "{\"key1\":\"value1\",\"key2\":1,\"key3\":{\"a\":true,\"b\":\"abc\",\"id\":\"abc\"},\"key4\":\"abc\",\"key5\":false,\"id\":\"abc\",\"nullprop\":null}";

  @Test(expected = IOException.class)
  public void testInvalidJson() throws IOException {
    String input = "abc 123";

    String result = new JsonSearchReplacer("prop", SEARCH, REPLACE).getReplacedContent(input);

    assertThat(result, is(equalTo(input)));
  }

  @Test
  public void testEmptyJson() throws IOException {
    String input = "{}";

    String result = new JsonSearchReplacer("prop", SEARCH, REPLACE).getReplacedContent(input);

    assertThat(result, is(equalTo("{}")));
  }

  @Test
  public void testPropNotFound() throws IOException {
    String result = new JsonSearchReplacer("prop", SEARCH, REPLACE).getReplacedContent(JSON);

    assertThat(result, is(equalTo(JSON)));
  }

  @Test
  public void testTopLevelPropNoReplace() throws IOException {
    String result = new JsonSearchReplacer("key1", SEARCH, REPLACE).getReplacedContent(JSON);

    assertThat(result, is(equalTo(JSON)));
  }

  @Test
  public void testNullProp() throws IOException {
    String result = new JsonPrependReplacer("nullprop", PREPEND).getReplacedContent(JSON);

    assertThat(result, is(equalTo(JSON)));
  }

  @Test
  public void testTopLevelPropReplace() throws IOException {
    String result = new JsonSearchReplacer("key4", SEARCH, REPLACE).getReplacedContent(JSON);

    String updated =
        "{\"key1\":\"value1\",\"key2\":1,\"key3\":{\"a\":true,\"b\":\"abc\",\"id\":\"abc\"},\"key4\":\"xyz\",\"key5\":false,\"id\":\"abc\",\"nullprop\":null}";
    assertThat(result, is(equalTo(updated)));
  }

  @Test
  public void testNestedLevelPropReplace() throws IOException {
    String result = new JsonSearchReplacer("b", SEARCH, REPLACE).getReplacedContent(JSON);

    String updated =
        "{\"key1\":\"value1\",\"key2\":1,\"key3\":{\"a\":true,\"b\":\"xyz\",\"id\":\"abc\"},\"key4\":\"abc\",\"key5\":false,\"id\":\"abc\",\"nullprop\":null}";
    assertThat(result, is(equalTo(updated)));
  }

  @Test
  public void testMultiplePropReplace() throws IOException {
    String result = new JsonSearchReplacer("id", SEARCH, REPLACE).getReplacedContent(JSON);

    String updated =
        "{\"key1\":\"value1\",\"key2\":1,\"key3\":{\"a\":true,\"b\":\"abc\",\"id\":\"xyz\"},\"key4\":\"abc\",\"key5\":false,\"id\":\"xyz\",\"nullprop\":null}";
    assertThat(result, is(equalTo(updated)));
  }

  @Test
  public void testMultiplePropPrepend() throws IOException {
    String result = new JsonPrependReplacer("id", PREPEND).getReplacedContent(JSON);

    String updated =
        "{\"key1\":\"value1\",\"key2\":1,\"key3\":{\"a\":true,\"b\":\"abc\",\"id\":\"123abc\"},\"key4\":\"abc\",\"key5\":false,\"id\":\"123abc\",\"nullprop\":null}";
    assertThat(result, is(equalTo(updated)));
  }

}
