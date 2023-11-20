package org.sonatype.nexus.plugins.ansiblegalaxy.internal.hosted

import org.sonatype.nexus.plugins.ansiblegalaxy.AnsibleGalaxyFormat
import org.sonatype.nexus.plugins.ansiblegalaxy.AssetKind
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.AnsibleGalaxyRecipeSupport
import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.http.HttpHandlers
import org.sonatype.nexus.repository.http.HttpMethods
import org.sonatype.nexus.repository.types.HostedType
import org.sonatype.nexus.repository.view.*
import org.sonatype.nexus.repository.view.matchers.ActionMatcher
import org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Puppet Hosted Recipe
 *
 * @since 0.0.2
 */
@Named(AnsibleGalaxyHostedRecipe.NAME)
@Singleton
class AnsibleGalaxyHostedRecipe
        extends AnsibleGalaxyRecipeSupport {
    public static final String NAME = "ansiblegalaxy-hosted"

    private final HostedHandlers hostedHandlers
    private final Provider<AnsibleGalaxyHostedFacet> hostedFacet

    @Inject
    AnsibleGalaxyHostedRecipe(
            final Provider<AnsibleGalaxyHostedFacet> hostedFacet,
            final HostedHandlers hostedHandlers,
            @Named(HostedType.NAME) final Type type,
            @Named(AnsibleGalaxyFormat.NAME) final Format format
    ) {
        super(type, format)
        this.hostedFacet = hostedFacet
        this.hostedHandlers = hostedHandlers
    }

    @Override
    void apply(@Nonnull final Repository repository) throws Exception {
        repository.attach(securityFacet.get())
        repository.attach(configure(viewFacet.get()))
        repository.attach(httpClientFacet.get())
        repository.attach(componentMaintenanceFacet.get())
        repository.attach(storageFacet.get())
        repository.attach(hostedFacet.get())
        repository.attach(searchFacet.get())
        repository.attach(attributesFacet.get())
    }

    /**
     * Configure {@link ViewFacet}.
     */
    private ViewFacet configure(final ConfigurableViewFacet facet) {
        Router.Builder builder = new Router.Builder()

        builder.route(new Route.Builder().matcher(apiInternalsMatcher())
                .handler(timingHandler)
                .handler(securityHandler)
                .handler(exceptionHandler)
                .handler(handlerContributor)
                .handler(conditionalRequestHandler)
                .handler(partialFetchHandler)
                .handler(contentHeadersHandler)
                .handler(unitOfWorkHandler)
                .handler(hostedHandlers.api)
                .create())

        builder.route(new Route.Builder().matcher(collectionDetailMatcher())
                .handler(timingHandler)
                .handler(securityHandler)
                .handler(exceptionHandler)
                .handler(handlerContributor)
                .handler(conditionalRequestHandler)
                .handler(partialFetchHandler)
                .handler(contentHeadersHandler)
                .handler(unitOfWorkHandler)
                .handler(hostedHandlers.collectionDetail)
                .create())

        builder.route(new Route.Builder().matcher(collectionVersionListMatcher())
                .handler(timingHandler)
                .handler(securityHandler)
                .handler(exceptionHandler)
                .handler(handlerContributor)
                .handler(conditionalRequestHandler)
                .handler(partialFetchHandler)
                .handler(contentHeadersHandler)
                .handler(unitOfWorkHandler)
                .handler(hostedHandlers.collectionVersionList)
                .create())

        builder.route(new Route.Builder().matcher(collectionVersionDetailMatcher())
                .handler(timingHandler)
                .handler(securityHandler)
                .handler(exceptionHandler)
                .handler(handlerContributor)
                .handler(conditionalRequestHandler)
                .handler(partialFetchHandler)
                .handler(contentHeadersHandler)
                .handler(unitOfWorkHandler)
                .handler(hostedHandlers.collectionVersionDetail)
                .create())

        builder.route(new Route.Builder().matcher(collectionArtifactMatcher())
                .handler(timingHandler)
                .handler(securityHandler)
                .handler(exceptionHandler)
                .handler(handlerContributor)
                .handler(conditionalRequestHandler)
                .handler(partialFetchHandler)
                .handler(contentHeadersHandler)
                .handler(unitOfWorkHandler)
                .handler(hostedHandlers)
                .create())

        builder.route(new Route.Builder().matcher(collectionArtifactIhmMatcher())
                .handler(timingHandler)
                .handler(securityHandler)
                .handler(exceptionHandler)
                .handler(handlerContributor)
                .handler(conditionalRequestHandler)
                .handler(partialFetchHandler)
                .handler(contentHeadersHandler)
                .handler(unitOfWorkHandler)
                .handler(hostedHandlers)
                .create())

        builder.route(new Route.Builder().matcher(moduleUploadMatcher())
                .handler(timingHandler)
                .handler(securityHandler)
                .handler(exceptionHandler)
                .handler(handlerContributor)
                .handler(conditionalRequestHandler)
                .handler(partialFetchHandler)
                .handler(contentHeadersHandler)
                .handler(unitOfWorkHandler)
                .handler(hostedHandlers)
                .create())

//
//        builder.route(new Route.Builder().matcher(moduleReleaseByNameAndVersionMatcher())
//                .handler(timingHandler)
//                .handler(securityHandler)
//                .handler(exceptionHandler)
//                .handler(handlerContributor)
//                .handler(conditionalRequestHandler)
//                .handler(partialFetchHandler)
//                .handler(contentHeadersHandler)
//                .handler(unitOfWorkHandler)
//                .handler(hostedHandlers.moduleByNameAndVersion)
//                .create());
//
//        builder.route(new Route.Builder()
//                .matcher(BrowseUnsupportedHandler.MATCHER)
//                .handler(browseUnsupportedHandler)
//                .create());

        builder.defaultHandlers(HttpHandlers.badRequest())

        facet.configure(builder.create())

        return facet
    }

    static Matcher moduleUploadMatcher() {
        return LogicMatchers.and(
                new ActionMatcher(HttpMethods.PUT, HttpMethods.POST),
                tokenMatcherForExtensionAndName(),
                setAssetKind(AssetKind.COLLECTION_ARTIFACT)
        )
    }

    static TokenMatcher tokenMatcherForExtensionAndName() {
        return new TokenMatcher("/{author}-{module}-{version}.tar.gz")
    }
}