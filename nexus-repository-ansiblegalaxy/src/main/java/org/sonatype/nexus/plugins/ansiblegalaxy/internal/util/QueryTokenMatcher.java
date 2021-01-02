package org.sonatype.nexus.plugins.ansiblegalaxy.internal.util;

import java.util.Map;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Matcher;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenParser;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link Matcher} that examines the {@link Request#getPath() request path} and {@link Request#getParameters() request
 * parameters} and attempts to parse it using the
 * {@link TokenParser}.
 *
 * If there is a match, the tokens are stored in the context under key {@link TokenMatcher.State}.
 *
 * @see {@link TokenMatcher}: examines only request path and not parameters.
 */
public class QueryTokenMatcher
    extends TokenMatcher
{

  private final TokenParser parser;

  private final String pattern;

  public QueryTokenMatcher(String pattern) {
    super(pattern);
    this.pattern = checkNotNull(pattern);
    this.parser = new TokenParser(pattern);
  }

  @Override
  public boolean matches(final Context context) {
    checkNotNull(context);

    String uri = AnsibleGalaxyPathUtils.getUri(context.getRequest());
    log.debug("Matching: {}~={}", uri, parser);
    final Map<String, String> tokens = parser.parse(uri);
    if (tokens == null) {
      // There was no match.
      return false;
    }

    // matched expose tokens in context
    context.getAttributes().set(State.class, new State()
    {
      @Override
      public String pattern() {
        return pattern;
      }

      @Override
      public Map<String, String> getTokens() {
        return tokens;
      }
    });
    return true;
  }

}
