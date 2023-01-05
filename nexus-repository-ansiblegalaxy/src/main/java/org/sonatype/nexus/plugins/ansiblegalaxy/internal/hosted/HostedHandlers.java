package org.sonatype.nexus.plugins.ansiblegalaxy.internal.hosted;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.plugins.ansiblegalaxy.AssetKind;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyPathUtils;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.view.*;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.payloads.StringPayload;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.http.HttpMethods.*;
import static org.sonatype.nexus.repository.http.HttpResponses.notFound;
import static org.sonatype.nexus.repository.http.HttpResponses.ok;

@Named
@Singleton
public class HostedHandlers
        extends ComponentSupport
        implements Handler {
    final Handler api = context -> {
        return ok(new StringPayload("{\"description\":\"GALAXY REST API\",\"current_version\":\"v1\",\"available_versions\":{\"v1\":\"v1/\",\"v2\":\"v2/\"},\"server_version\":\"3.4.15\",\"version_name\":\"Doin' it Right\",\"team_members\":[\"chouseknecht\",\"cutwater\",\"alikins\",\"newswangerd\",\"awcrosby\",\"tima\",\"gregdek\"]}", "application/json"));

    };
    private AnsibleGalaxyPathUtils ansibleGalaxyPathUtils;
    final Handler collectionDetail = context -> {

        TokenMatcher.State state = context.getAttributes().require(TokenMatcher.State.class);

        Parameters parameters = context.getRequest().getParameters();

        String user = ansibleGalaxyPathUtils.author(state);
        String module = ansibleGalaxyPathUtils.module(state);
        Content content = context.getRepository().facet(AnsibleGalaxyHostedFacet.class).searchByName(context, user, module);

        return (content != null) ? ok(content) : notFound();
    };
    final Handler collectionVersionList = context -> {
        TokenMatcher.State state = context.getAttributes().require(TokenMatcher.State.class);

        String user = ansibleGalaxyPathUtils.author(state);
        String module = ansibleGalaxyPathUtils.module(state);

        Content content = context.getRepository().facet(AnsibleGalaxyHostedFacet.class).searchVersionsByName(context, user, module);

        return (content != null) ? ok(content) : notFound();
    };
    final Handler collectionVersionDetail = context -> {
        TokenMatcher.State state = context.getAttributes().require(TokenMatcher.State.class);

        String user = ansibleGalaxyPathUtils.author(state);
        String module = ansibleGalaxyPathUtils.module(state);
        String version = ansibleGalaxyPathUtils.version(state);

        Content content = context.getRepository().facet(AnsibleGalaxyHostedFacet.class).moduleByNameAndVersion(context, user, module, version);

        return (content != null) ? ok(content) : notFound();
    };

    @Inject
    public HostedHandlers(AnsibleGalaxyPathUtils ansibleGalaxyPathUtils) {
        this.ansibleGalaxyPathUtils = ansibleGalaxyPathUtils;
    }

    /**
     * Pull the parsed content path out of the context.
     */
    private static String contentPath(final Context context) {
        TokenMatcher.State state = context.getAttributes().require(TokenMatcher.State.class);
        String path = state.getTokens().get("path");
        checkState(path != null, "Missing token: path");
        return path;
    }

    @Nonnull
    @Override
    public Response handle(@Nonnull Context context) throws Exception {
        String method = context.getRequest().getAction();
        Repository repository = context.getRepository();


        AnsibleGalaxyHostedFacet storage = repository.facet(AnsibleGalaxyHostedFacet.class);

        AssetKind assetKind = context.getAttributes().require(AssetKind.class);
        TokenMatcher.State state = context.getAttributes().require(TokenMatcher.State.class);

        switch (method) {
            case HEAD:
            case GET: {
                String path = this.ansibleGalaxyPathUtils.collectionArtifactPath(state);
                log.debug("{} repository '{}' content-path: {}", method, repository.getName(), path);
                Content content = storage.get(path);
                return (content != null) ? ok(content) : notFound();
            }

            case PUT: {
                Payload content = context.getRequest().getPayload();
                String path = this.ansibleGalaxyPathUtils.collectionArtifactPath(state);
                log.debug("{} repository '{}' content-path: {}", method, repository.getName(), path);

                storage.put(path, content, assetKind);
                return HttpResponses.created();
            }

            case DELETE: {
                String path = "";//contentPath(context);
                boolean deleted = storage.delete(path);
                if (deleted) {
                    return HttpResponses.noContent();
                }
                return HttpResponses.notFound(path);
            }

            default:
                return HttpResponses.methodNotAllowed(method, GET, HEAD, PUT, DELETE);
        }
    }
//
//    final Handler moduleByNameAndVersion = context -> {
//        State state = context.getAttributes().require(TokenMatcher.State.class);
//
//        String user = pathUtils.author(state);
//        String module = pathUtils.module(state);
//        String version = pathUtils.module(state);
//
//        Content content = context.getRepository().facet(AnsibleGalaxyHostedFacet.class).moduleByNameAndVersion(user, module, version);
//
//        return (content != null) ? ok(content) : notFound();
//    };
}
