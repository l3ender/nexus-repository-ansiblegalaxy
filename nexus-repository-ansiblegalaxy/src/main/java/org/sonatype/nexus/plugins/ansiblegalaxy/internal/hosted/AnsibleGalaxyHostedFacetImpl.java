package org.sonatype.nexus.plugins.ansiblegalaxy.internal.hosted;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.plugins.ansiblegalaxy.AssetKind;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.metadata.*;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyAssetAttributePopulator;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyAttributeParser;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyDataAccess;
import org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyPathUtils;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.search.ElasticSearchService;
import org.sonatype.nexus.repository.search.SearchService;
import org.sonatype.nexus.repository.storage.*;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.payloads.BytesPayload;
import org.sonatype.nexus.repository.view.payloads.TempBlob;
import org.sonatype.nexus.transaction.UnitOfWork;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA256;
import static org.sonatype.nexus.plugins.ansiblegalaxy.AssetKind.COLLECTION_ARTIFACT;
import static org.sonatype.nexus.plugins.ansiblegalaxy.internal.util.AnsibleGalaxyDataAccess.HASH_ALGORITHMS;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;
import static org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter.P_NAME;


@Named
public class AnsibleGalaxyHostedFacetImpl
        extends FacetSupport
        implements AnsibleGalaxyHostedFacet {

    private static final Iterable<HashAlgorithm> HASHING = ImmutableList.of(SHA256);

    private final AnsibleGalaxyDataAccess ansibleGalaxyDataAccess;
    private final AnsibleGalaxyAttributeParser ansibleGalaxyAttributeParser;
    private final AnsibleGalaxyAssetAttributePopulator ansibleGalaxyAssetAttributePopulator;
    private final ElasticSearchService searchService;
    private final SearchService searchNexusService;
    private final AnsibleGalaxyModulesResultBuilder builder;
    private final AnsibleGalaxyModulesBuilder moduleBuilder;
    private final AnsibleGalaxyPathUtils ansibleGalaxyPathUtils;

    private final List<SortBuilder> sorting;

    @Inject
    public AnsibleGalaxyHostedFacetImpl(AnsibleGalaxyDataAccess ansibleGalaxyDataAccess,
                                        AnsibleGalaxyAttributeParser ansibleGalaxyAttributeParser,
                                        AnsibleGalaxyAssetAttributePopulator ansibleGalaxyAssetAttributePopulator,
                                        ElasticSearchService searchService,
                                        SearchService searchNexusService,
                                        AnsibleGalaxyModulesResultBuilder builder,
                                        AnsibleGalaxyModulesBuilder moduleBuilder,
                                        AnsibleGalaxyPathUtils ansibleGalaxyPathUtils) {

        this.ansibleGalaxyDataAccess = ansibleGalaxyDataAccess;
        this.ansibleGalaxyAttributeParser = ansibleGalaxyAttributeParser;
        this.ansibleGalaxyAssetAttributePopulator = ansibleGalaxyAssetAttributePopulator;
        this.searchService = searchService;
        this.searchNexusService = searchNexusService;
        this.builder = builder;
        this.moduleBuilder = moduleBuilder;
        this.ansibleGalaxyPathUtils = ansibleGalaxyPathUtils;


        this.sorting = new ArrayList<>();
        this.sorting.add(SortBuilders.fieldSort(P_NAME).order(SortOrder.ASC));
        this.sorting.add(SortBuilders.fieldSort(ComponentEntityAdapter.P_VERSION).order(SortOrder.ASC));
    }

    @TransactionalTouchBlob
    @Override
    public Content get(final String path) {


        checkNotNull(path);
        StorageTx tx = UnitOfWork.currentTx();

        Asset asset = this.ansibleGalaxyDataAccess.findAsset(tx, tx.findBucket(getRepository()), path);
        if (asset == null) {
            return null;
        }
        if (asset.markAsDownloaded()) {
            tx.saveAsset(asset);
        }

        return this.ansibleGalaxyDataAccess.toContent(asset, tx.requireBlob(asset.requireBlobRef()));
    }

    @Override
    public void put(final String path, final Payload content, final AssetKind assetKind) throws IOException {
        checkNotNull(path);
        checkNotNull(content);

        if (assetKind != COLLECTION_ARTIFACT) {
            throw new IllegalArgumentException("Unsupported AssetKind");
        }
        try (TempBlob tempBlob = facet(StorageFacet.class).createTempBlob(content, HASH_ALGORITHMS)) {
            storeModule(path, tempBlob, content);
        }
    }

    @Override
    public boolean delete(final String path) throws IOException {
        // return assets().path(path).find().map(FluentAsset::delete).orElse(false);
        return true;
    }

    @Override
    public Content searchByName(Context context, String user, String module) {
        AnsibleGalaxyModuleName result = new AnsibleGalaxyModuleName();
        result.setVersions_url(ansibleGalaxyPathUtils.collectionNamePath(user, module))
                .setName(module);
        ObjectMapper objectMapper = new ObjectMapper();
        String results = null;
        try {
            results = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new Content(new BytesPayload(results.getBytes(), ContentTypes.APPLICATION_JSON));
    }

    @Override
    @TransactionalTouchMetadata
    public Content searchVersionsByName(final Context context, String user, String module) {
        StorageTx tx = UnitOfWork.currentTx();

        AnsibleGalaxyModules releases = getModuleReleasesFromSearchResponse(context, tx, user, module);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String results = objectMapper.writeValueAsString(releases);

            return new Content(new BytesPayload(results.getBytes(), ContentTypes.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


    private AnsibleGalaxyModules getModuleReleasesFromSearchResponse(
            final Context context,
            final StorageTx tx,
            String user, String module) {
        TokenMatcher.State state = context.getAttributes().require(TokenMatcher.State.class);

        List<Repository> list = new ArrayList<Repository>();
        list.add(getRepository());
        String baseUrlRepo = getRepository().getUrl();
        Iterable<Asset> assets;
        assets = tx.findAssets(Query.builder().where(P_NAME).like(String.format("%%%s/%s%%", user, module)).build(), list);
        long count = tx.countAssets(Query.builder().where(P_NAME).like(String.format("%%%s/%s%%", user, module)).build(), list);

        AnsibleGalaxyModules releases = moduleBuilder.parse(count, count, 0, context);
        for (Asset asset : assets) {
            AnsibleGalaxyModulesResult result = builder.parse(asset, baseUrlRepo);
            releases.addResult(result);
        }


        return releases;
    }

    @Override
    @TransactionalTouchMetadata
    public Content moduleByNameAndVersion(Context context, String user, String module, String version) {

        StorageTx tx = UnitOfWork.currentTx();

        List<Repository> list = new ArrayList<Repository>();
        list.add(getRepository());
        String baseUrlRepo = getRepository().getUrl();
        Asset asset = null;
        Iterable<Asset> assets;
        assets = tx.findAssets(Query.builder().where(P_NAME).like(String.format("%%%s/%s%%", user, module)).build(), list);
        for (Asset assetX : assets) {
            asset = assetX;
            break;
        }
        AnsibleGalaxyModule result = new AnsibleGalaxyModule();
        result.setVersion(version)
                .setHref(ansibleGalaxyPathUtils.parseHref(baseUrlRepo, asset.attributes().child("ansiblegalaxy").get("name").toString(), asset.attributes().child("ansiblegalaxy").get("version").toString()))
                .setDownload_url(ansibleGalaxyPathUtils.download(baseUrlRepo, user, module, version))
                .setNamespace(new AnsibleGalaxyModule.AnsibleGalaxyModuleNamespace().setName(user))
                .setCollection(new AnsibleGalaxyModule.AnsibleGalaxyModuleCollection().setName(module))
                .setMetadata(new AnsibleGalaxyModule.AnsibleGalaxyModuleMetadata().setDependencies(asset.attributes().child("ansiblegalaxy").get("dependencies")))
                .setArtifact(new AnsibleGalaxyModule.AnsibleGalaxyModuleArtifact().setSha256(asset.attributes().child("checksum").get("sha256").toString()));
        ObjectMapper objectMapper = new ObjectMapper();
        String results = null;
        try {
            results = objectMapper.writeValueAsString(result);
            return new Content(new BytesPayload(results.getBytes(), ContentTypes.APPLICATION_JSON));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


    @TransactionalStoreBlob
    protected Content storeModule(final String path,
                                  final TempBlob moduleContent,
                                  final Payload payload) throws IOException {
        StorageTx tx = UnitOfWork.currentTx();
        Bucket bucket = tx.findBucket(getRepository());

        Asset asset = createModuleAsset(path, tx, bucket, moduleContent.get());

        return this.ansibleGalaxyDataAccess.saveAsset(tx, asset, moduleContent, payload);
    }

    private Asset createModuleAsset(final String path,
                                    final StorageTx tx,
                                    final Bucket bucket,
                                    final InputStream inputStream) throws IOException {
        AnsibleGalaxyAttributes module;
        module = this.ansibleGalaxyAttributeParser.getAttributesFromInputStream(inputStream);

        return findOrCreateAssetAndComponent(path, tx, bucket, module);
    }

    private Asset findOrCreateAssetAndComponent(final String path,
                                                final StorageTx tx,
                                                final Bucket bucket,
                                                final AnsibleGalaxyAttributes module) {
        Asset asset = this.ansibleGalaxyDataAccess.findAsset(tx, bucket, path);
        if (asset == null) {
            Component component = findOrCreateComponent(tx, bucket, module);
            asset = tx.createAsset(bucket, component);
            asset.name(path);

            asset.formatAttributes().set(P_ASSET_KIND, COLLECTION_ARTIFACT.name());
        }

        this.ansibleGalaxyAssetAttributePopulator.populate(asset.formatAttributes(), module);

        return asset;
    }

    private Component findOrCreateComponent(final StorageTx tx,
                                            final Bucket bucket,
                                            final AnsibleGalaxyAttributes module) {
        Component component = this.ansibleGalaxyDataAccess.findComponent(tx, getRepository(), module.getGroup(), module.getGroup() + "-" + module.getName(), module.getVersion());
        if (component == null) {
            component = tx.createComponent(bucket, getRepository().getFormat())
                    .group(module.getGroup())
                    .name(module.getGroup() + "-" + module.getName())
                    .version(module.getVersion());
            tx.saveComponent(component);
        }
        return component;
    }
}

