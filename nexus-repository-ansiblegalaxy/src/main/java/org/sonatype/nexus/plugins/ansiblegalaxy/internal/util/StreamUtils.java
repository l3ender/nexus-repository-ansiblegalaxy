package org.sonatype.nexus.plugins.ansiblegalaxy.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.common.base.Strings;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import org.apache.commons.io.input.ReaderInputStream;

public final class StreamUtils
{

  private StreamUtils() {
  }

  /**
   * Converts an input stream to string, using UTF8.
   */
  public static String toString(InputStream in) throws IOException {
    return Strings.nullToEmpty(CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8))).trim();
  }

  /**
   * Converts a string to an input stream, using UTF8.
   */
  public static InputStream toStream(String str) throws IOException {
    return new ReaderInputStream(CharSource.wrap(str).openStream(), StandardCharsets.UTF_8);
  }

}
