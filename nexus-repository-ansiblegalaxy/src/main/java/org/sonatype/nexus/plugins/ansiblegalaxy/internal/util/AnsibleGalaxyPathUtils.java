/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2020-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.ansiblegalaxy.internal.util;

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.sonatype.goodies.common.Loggers;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.metadata.AnsibleGalaxyAttributes;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;

import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility methods for working with AnsibleGalaxy routes and paths.
 */
@Named
@Singleton
public class AnsibleGalaxyPathUtils {

    public static final String ROLE_VERSION_URI_SUFFIX = "versions/";
    public static final String ROLE_ARTIFACT_MODULE_PARAM_NAME = "module";
    public static final String ROLE_ARTIFACT_URI_PREFIX = "/download/role";
    private static final String METADATA_PATH = "metadata";
    private static final String API_PATH = "api";
    private static final String COLLECTION_PATH = "collection";
    private static final String ROLE_PATH = "role";
    private final Logger log = Loggers.getLogger(getClass());

    /**
     * Returns the author from a {@link TokenMatcher.State}.
     */
    public String author(final TokenMatcher.State state) {
        return match(state, "author");
    }

    public String module(final TokenMatcher.State state) {
        return match(state, "module");
    }

    public String version(final TokenMatcher.State state) {
        return match(state, "version");
    }

    public String version(final Context context) {
        return version(matcherState(context));
    }

    public String id(final TokenMatcher.State state) {
        return match(state, "id");
    }

    public String page(final TokenMatcher.State state) {
        return StringUtils.defaultIfBlank(state.getTokens().get("pagenum"), "1");
    }

    public String page_size(final TokenMatcher.State state) {
        return StringUtils.defaultIfBlank(state.getTokens().get("page_size"), "20");
    }

    public String offset(final TokenMatcher.State state) {
        return StringUtils.defaultIfBlank(state.getTokens().get("offset"), "0");
    }
    public String limit(final TokenMatcher.State state) {
        return StringUtils.defaultIfBlank(state.getTokens().get("limit"), "100");
    }

    public TokenMatcher.State matcherState(final Context context) {
        State state = context.getAttributes().require(TokenMatcher.State.class);
        log.debug("matched state tokens: {}", state.getTokens());
        return state;
    }

    /**
     * Utility method encapsulating getting a particular token by name from a matcher, including preconditions.
     */
    private String match(final TokenMatcher.State state, final String name) {
        checkNotNull(state);
        String result = state.getTokens().get(name);
        checkNotNull(result);
        return result;
    }

    public String apiMetadataPath(final State matcherState) {
        return String.format("%s/%s/detail.json", METADATA_PATH, API_PATH);
    }

    public String collectionDetailPath(final State matcherState) {
        String author = author(matcherState);
        String module = module(matcherState);

        return String.format("%s/%s/%s/detail.json", COLLECTION_PATH, author, module);
    }

    public String collectionVersionPagedPath(final State matcherState) {
        String author = author(matcherState);
        String module = module(matcherState);
        String page = page(matcherState);

        return String.format("%s/%s/%s/versions%s.json", COLLECTION_PATH, author, module, page);
    }

    public String collectionVersionLimitPath(final State matcherState) {
        String author = author(matcherState);
        String module = module(matcherState);
        String offset = offset(matcherState);
        String limit = limit(matcherState);

        return String.format("%s/%s/%s/versions-offset-%s-%s.json", COLLECTION_PATH, author, module, offset, limit);
    }

    public String roleSearchPath(final State matcherState) {
        String author = author(matcherState);
        String module = module(matcherState);
        String page = page(matcherState);

        return String.format("%s/%s/%s/roles%s.json", ROLE_PATH, author, module, page);
    }

    public String roleDetailPath(final State matcherState) {
        String id = id(matcherState);

        return String.format("%s/%s/%s/detail.json", METADATA_PATH, ROLE_PATH, id);
    }

    public String roleVersionPagedPath(final State matcherState) {
        String id = id(matcherState);
        String page = page(matcherState);

        return String.format("%s/%s/%s/versions%s.json", METADATA_PATH, ROLE_PATH, id, page);
    }

    public String collectionVersionDetailPath(final State matcherState) {
        String author = author(matcherState);
        String module = module(matcherState);
        String version = version(matcherState);

        return String.format("%s/%s/%s/%s/detail.json", COLLECTION_PATH, author, module, version);
    }

    private String artifactPath(final State matcherState) {
        String author = author(matcherState);
        String module = module(matcherState);
        String version = version(matcherState);

        return String.format("%s/%s/%s/%s.tar.gz", author, module, version, author + "-" + module + "-" + version);
    }

    public String artifactPath(final String author, final String module, final String version) {

        return String.format("%s/%s/%s/%s.tar.gz", author, module, version, author + "-" + module + "-" + version);
    }

    public String download(String baseUrlRepo, final String author, final String module, final String version) {

        return String.format("%s/api/v3/plugin/ansible/content/published/collections/artifacts/%s.tar.gz", baseUrlRepo, author + "-" + module + "-" + version);
    }

    public String collectionArtifactPath(final State matcherState) {
        String author = author(matcherState);
        String module = module(matcherState);
        String version = version(matcherState);
        //return String.format("%s/%s", COLLECTION_PATH, artifactPath(matcherState));
        return String.format("api/v3/plugin/ansible/content/published/collections/artifacts/%s-%s-%s.tar.gz", author, module, version);
    }

    public String parseHref(String baseUrlRepo, final String name, final String version) {
        String[] animalsArray = name.split("-");
        return String.format("%s/api/v2/collections/%s/%s/versions/%s/", baseUrlRepo, animalsArray[0], animalsArray[1], version);
    }

    public String collectionArtifactPath(final String author, final String module, final String version) {
        // return String.format("%s/%s", COLLECTION_PATH, artifactPath(author, module, version));
        return String.format("api/v3/plugin/ansible/content/published/collections/artifacts/%s-%s-%s.tar.gz", author, module, version);
    }

    public String collectionNamePath(final String author, final String module) {
        return String.format("%s/%s/%s/%s", "collections", author, module, ROLE_VERSION_URI_SUFFIX);
    }

    public String roleArtifactPath(final State matcherState) {
        return String.format("%s/%s", ROLE_PATH, artifactPath(matcherState));
    }


    public String buildModuleReleaseByNamePath(final Parameters parameters) {
        if (parameters.isEmpty()) {
            return "/v3/releases";
        }
        return String.format("/v3/releases?%s", Joiner.on("&").withKeyValueSeparator("=").join(parameters));
    }

    private AnsibleGalaxyAttributes getAttributesFromMatcherState(final TokenMatcher.State state, String groupPath) {
        String group = String.format("%s/%s", groupPath, author(state));
        return new AnsibleGalaxyAttributes(group, module(state), version(state));
    }

    public AnsibleGalaxyAttributes getCollectionAttributes(final TokenMatcher.State state) {
        return getAttributesFromMatcherState(state, COLLECTION_PATH);
    }

    public AnsibleGalaxyAttributes getRoleAttributes(final TokenMatcher.State state) {
        return getAttributesFromMatcherState(state, ROLE_PATH);
    }

    /**
     * Used by proxy handler to send request to actual/real download location, which can be different than the proxy
     * repo's upstream endpoint.
     *
     * @param originalUpstream   example:
     *                           https://galaxy.ansible.com/download/role/geerlingguy/ansible-role-jenkins/archive/3.0.0.tar.gz
     * @param desiredUpstreamUrl example: https://github.com
     * @return example: https://github.com/geerlingguy/ansible-role-jenkins/archive/3.0.0.tar.gz
     */
    public URI rebuildRoleDownloadUri(URI originalUpstream, String desiredUpstreamUrl) {
        final String schemeSeparator = "://";
        String desiredScheme = StringUtils.substringBefore(desiredUpstreamUrl, schemeSeparator);
        String desiredUrlWithoutScheme = StringUtils.substringAfter(desiredUpstreamUrl, schemeSeparator);

        final String pathSeparator = "/";
        String desiredHost = StringUtils.substringBefore(desiredUrlWithoutScheme, pathSeparator);
        String desiredPath = StringUtils.substringAfter(desiredUrlWithoutScheme, pathSeparator);

        if (desiredPath.endsWith(pathSeparator)) {
            desiredPath = StringUtils.chop(desiredPath);
        }

        String upstreamPath = originalUpstream.getPath().replaceFirst(ROLE_ARTIFACT_URI_PREFIX, "");

        String path = upstreamPath;
        if (StringUtils.isNotBlank(desiredPath)) {
            path = "/" + desiredPath + path;
        }

        try {
            return new URIBuilder(originalUpstream).setScheme(desiredScheme).setHost(desiredHost).setPath(path).build();
        } catch (URISyntaxException e) {
            log.error("cannot update http request from {} -> {}", originalUpstream, desiredUpstreamUrl, e);
            return originalUpstream;
        }
    }

}
