package org.sonatype.nexus.plugins.ansiblegalaxy.internal.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Matcher;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link Matcher} that examines the {@link Request#getPath() request path} and {@link Request#getParameters() request
 * parameters} and attempts to parse them using the {@link QueryTokenParser}.
 *
 * If there is a match, the tokens are stored in the context under key {@link TokenMatcher.State}.
 * 
 * @see {@link TokenMatcher}.
 */
public class QueryTokenMatcher
    extends ComponentSupport
    implements Matcher
{

  private final QueryTokenParser parser;

  private final String pattern;

  public QueryTokenMatcher(String pattern) {
    this(pattern, new HashMap<>());
  }

  public QueryTokenMatcher(String pattern, Map<String, String> queryParamTokens) {
    this.pattern = checkNotNull(pattern);
    this.parser = new QueryTokenParser(pattern, queryParamTokens);
  }

  @Override
  public boolean matches(final Context context) {
    checkNotNull(context);

    String path = context.getRequest().getPath();
    Map<String, String> requestParameters = getRequestParameters(context.getRequest().getParameters());
    log.debug("Matching: {} ({})~={}", path, requestParameters, parser);
    final Map<String, String> tokens = parser.parse(path, requestParameters);
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

  private Map<String, String> getRequestParameters(Parameters parameters) {
    Map<String, String> params = new HashMap<>();
    for (Entry<String, String> param : parameters) {
      params.put(param.getKey(), param.getValue());
    }
    return params;
  }

}
