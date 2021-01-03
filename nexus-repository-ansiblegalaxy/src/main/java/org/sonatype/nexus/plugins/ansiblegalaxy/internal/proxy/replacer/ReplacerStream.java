package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.sonatype.goodies.common.Loggers;

import com.google.common.base.Strings;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles replacing multiple types of content, in order.
 */
public class ReplacerStream
{

  private final Logger log = Loggers.getLogger(getClass());

  private final Replacer[] replacers;

  public ReplacerStream(Replacer... replacers) {
    this.replacers = checkNotNull(replacers);
  }

  public InputStream getReplacedContent(InputStream input) throws IOException {
    String content = toString(input);

    String updatedContent = content;

    for (Replacer replacer : replacers) {
      log.debug("replacing content using {}: {}", replacer.getClass().getSimpleName(), replacer);

      updatedContent = replacer.getReplacedContent(content);
      log.trace("content replace:\n\t---> old: {}\n\t---> new: {}", content, updatedContent);

      content = updatedContent;
    }

    return toStream(updatedContent);
  }

  /**
   * Converts an input stream to string, using UTF8.
   */
  private String toString(InputStream in) throws IOException {
    return Strings.nullToEmpty(CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8))).trim();
  }

  /**
   * Converts a string to an input stream, using UTF8.
   */
  private InputStream toStream(String str) throws IOException {
    return new ReaderInputStream(CharSource.wrap(str).openStream(), StandardCharsets.UTF_8);
  }

}
