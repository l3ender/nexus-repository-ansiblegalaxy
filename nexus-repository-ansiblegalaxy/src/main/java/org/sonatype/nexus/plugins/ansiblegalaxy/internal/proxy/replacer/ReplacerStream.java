package org.sonatype.nexus.plugins.ansiblegalaxy.internal.proxy.replacer;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.sonatype.goodies.common.Loggers;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles replacing multiple types of content, in order.
 */
public class ReplacerStream {

    private final Logger log = Loggers.getLogger(getClass());

    private final List<Replacer> replacers;

    public ReplacerStream(Replacer replacer) {
        this.replacers = new ArrayList<>();
        this.replacers.add(replacer);
    }

    public ReplacerStream(List<Replacer> replacers) {
        this.replacers = checkNotNull(replacers);
    }

    public InputStream getReplacedContent(InputStream input) throws IOException {
        if (CollectionUtils.isNotEmpty(replacers)) {
            String content = StreamUtils.toString(input);

            String updatedContent = content;

            for (Replacer replacer : replacers) {
                log.debug("replacing content using {}: {}", replacer.getClass().getSimpleName(), replacer);

                updatedContent = replacer.getReplacedContent(content);
                if (log.isTraceEnabled()) {
                    log.trace("content replace:\n\t---> old: {}\n\t---> new: {}", content, updatedContent);
                }

                content = updatedContent;
            }

            return StreamUtils.toStream(updatedContent);
        }

        return input;
    }

}
