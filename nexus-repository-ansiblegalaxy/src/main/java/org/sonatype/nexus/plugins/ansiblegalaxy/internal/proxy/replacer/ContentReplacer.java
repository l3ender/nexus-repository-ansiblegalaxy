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

public abstract class ContentReplacer
{

  protected final Logger log = Loggers.getLogger(getClass());

  protected abstract String getReplacedContent(String input) throws IOException;

  /**
   * Converts an input stream to string, using UTF8.
   */
  protected String toString(InputStream in) throws IOException {
    return Strings.nullToEmpty(CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8))).trim();
  }

  /**
   * Converts a string to an input stream, using UTF8.
   */
  protected InputStream toStream(String str) throws IOException {
    return new ReaderInputStream(CharSource.wrap(str).openStream(), StandardCharsets.UTF_8);
  }

  public InputStream getReplacedContent(InputStream input) throws IOException {
    String content = toString(input);

    String replacedContent = getReplacedContent(content);

    log.debug("content replace:\n\t---> old: {}\n\t---> new: {}", content, replacedContent);
    return toStream(replacedContent);
  }

}
