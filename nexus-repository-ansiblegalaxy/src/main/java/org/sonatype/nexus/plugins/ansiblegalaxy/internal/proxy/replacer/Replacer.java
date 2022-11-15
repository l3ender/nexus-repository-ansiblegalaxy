package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer;

import org.slf4j.Logger;
import org.sonatype.goodies.common.Loggers;

import java.io.IOException;

public abstract class Replacer {

    protected final Logger log = Loggers.getLogger(getClass());

    public abstract String getReplacedContent(String input) throws IOException;

}
