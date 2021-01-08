package org.sonatype.nexus.plugins.ansiblegalaxy.internal.util;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.view.matchers.token.TokenParser;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Extends the {@link TokenParser} to support URI query parameters.
 * 
 * Query parameters are optional. If they exist in the request, tokens from them will be loaded.
 * 
 * Query parameters are not order specific.
 */
public class QueryTokenParser
    extends TokenParser
{
  private final Map<String, String> queryParameterTokens;

  /**
   * @param templatePattern - see {@link TokenParser}.
   * @param queryParameterTokens - a key-value collection where key indicates the name of the query parameter used for
   *          searching the request, and value indicates
   *          the token name which will be used to save the request parameter value if it exists in the request.
   */
  public QueryTokenParser(final String templatePattern, Map<String, String> queryParameterTokens) {
    super(templatePattern);
    checkNotNull(queryParameterTokens);

    this.queryParameterTokens = queryParameterTokens;
  }

  /**
   * Attempts to parse the provided path against the template pattern, and the query parameters against the query
   * parameter tokens.
   * 
   * If the pattern matches, tokens are loaded from the URL path and query parameter map. For the parameter map:
   * key represent query parameter names, and values represent the token name in which the query value will be stored.
   * 
   * The resulting Map contains an entry for each token extracted from the URL path and query parameters.
   * 
   * Returns {@code null} if the pattern does not match.
   */
  @Nullable
  public Map<String, String> parse(final String path, Map<String, String> queryParameters) {
    Map<String, String> tokens = super.parse(path);
    if (null == tokens) {
      return null;
    }

    for (Entry<String, String> queryToken : queryParameterTokens.entrySet()) {
      String paramName = queryToken.getKey();
      String tokenName = queryToken.getValue();

      if (queryParameters.containsKey(paramName)) {
        String paramValue = queryParameters.get(paramName);
        log.trace("Query param '{}' exists, will be saved as token '{}', value '{}'", paramName, tokenName, paramValue);

        tokens.put(tokenName, paramValue);
      }
    }

    return tokens;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" + "queryParameterTokens=" + queryParameterTokens + "} " + super.toString();
  }
}
