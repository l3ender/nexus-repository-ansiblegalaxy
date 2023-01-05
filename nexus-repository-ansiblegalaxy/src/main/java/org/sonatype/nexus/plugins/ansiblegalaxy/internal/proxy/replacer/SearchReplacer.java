package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Replaces all "search" string occurrences with "replace" string value for the provided input stream.
 */
public class SearchReplacer
        extends Replacer {

    private final String search;

    private final String replace;

    public SearchReplacer(String search, String replace) {
        this.search = checkNotNull(search);
        this.replace = checkNotNull(replace);
    }

    @Override
    public String getReplacedContent(String input) throws IOException {
        return input.replaceAll(search, replace);
    }

    @Override
    public String toString() {
        return "search=" + search + ", replace=" + replace;
    }

}
