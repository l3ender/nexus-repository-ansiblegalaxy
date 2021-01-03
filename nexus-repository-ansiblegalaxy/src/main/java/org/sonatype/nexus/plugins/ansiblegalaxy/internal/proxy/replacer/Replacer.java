package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer;

import java.io.IOException;

import org.sonatype.goodies.common.Loggers;

import org.slf4j.Logger;

public abstract class Replacer
{

  protected final Logger log = Loggers.getLogger(getClass());

  public abstract String getReplacedContent(String input) throws IOException;

}
