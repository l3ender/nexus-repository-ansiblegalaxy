package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Replaces all "search" string occurrences with "replace" string value for the provided input stream.
 *
 */
public class StringContentReplacer
    extends ContentReplacer
{

  private final String search;

  private final String replace;

  public StringContentReplacer(String search, String replace) {
    this.search = checkNotNull(search);
    this.replace = checkNotNull(replace);
  }

  @Override
  protected String getReplacedContent(String input) throws IOException {
    return input.replaceAll(search, replace);
  }

}
