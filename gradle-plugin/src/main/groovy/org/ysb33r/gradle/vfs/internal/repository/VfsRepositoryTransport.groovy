package org.ysb33r.gradle.vfs.internal.repository

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.commons.logging.LogFactory
import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileObject
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.artifacts.ivyservice.CacheLockingManager
import org.gradle.api.internal.artifacts.ivyservice.DefaultCacheLockingManager
import org.gradle.api.internal.file.DefaultTemporaryFileProvider
import org.gradle.api.internal.file.TemporaryFileProvider
import org.gradle.api.internal.file.TmpDirTemporaryFileProvider
import org.gradle.cache.internal.CacheFactory
import org.gradle.cache.internal.DefaultCacheFactory
import org.gradle.cache.internal.DefaultCacheRepository
import org.gradle.cache.internal.DefaultCacheScopeMapping
import org.gradle.cache.internal.DefaultFileLockManager
import org.gradle.internal.hash.HashValue
import org.gradle.internal.resource.cached.ByUrlCachedExternalResourceIndex
import org.gradle.internal.resource.cached.CachedExternalResourceIndex
import org.gradle.internal.resource.transfer.DefaultCacheAwareExternalResourceAccessor
import org.gradle.internal.resource.transport.http.DefaultHttpSettings
import org.gradle.internal.resource.transport.http.HttpClientHelper
import org.gradle.internal.resource.transport.http.HttpResourceAccessor
import org.gradle.internal.resource.transport.http.HttpResourceUploader
import org.gradle.logging.ProgressLoggerFactory
import org.gradle.util.BuildCommencedTimeProvider
import org.gradle.util.GradleVersion
import org.ysb33r.groovy.dsl.vfs.VFS
import org.gradle.api.Nullable
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransport
import org.gradle.internal.FileUtils
import org.gradle.internal.resource.ExternalResource
import org.gradle.internal.resource.metadata.ExternalResourceMetaData
import org.gradle.internal.resource.transfer.CacheAwareExternalResourceAccessor
import org.gradle.internal.resource.transfer.ExternalResourceAccessor
import org.gradle.internal.resource.transport.ExternalResourceRepository

/**
 * @author Schalk W. Cronjé.
 */
@CompileStatic
class VfsRepositoryTransport implements RepositoryTransport, ExternalResourceRepository, ExternalResourceAccessor {

    VfsRepositoryTransport(Project project,final String name) {
        vfs = new VFS (
            logger : LogFactory.getLog('vfs'),
            temporaryFileStore : "${project.gradle.startParameter.projectCacheDir}/vfs/${FileUtils.toSafeFileName(name)}"
        )
//        GradleInternal gradle = project.gradle as GradleInternal
        def gradleServices = (project.gradle as GradleInternal).services

//        gradle.services.get(DefaultCacheLockingManager.class)
        //project.gradle.startParameter.projectCacheDir
        BuildCommencedTimeProvider bctp = new BuildCommencedTimeProvider()

        def mapping= new DefaultCacheScopeMapping(
                project.gradle.startParameter.gradleUserHomeDir,
                project.gradle.startParameter.projectCacheDir,
                GradleVersion.version(project.gradle.gradleVersion)
        )

        def cache = new DefaultCacheRepository(mapping,gradleServices.get(CacheFactory.class))

        def lockMgr = new DefaultCacheLockingManager(cache)

        def indexer = new ByUrlCachedExternalResourceIndex('persistentCacheFile',bctp,lockMgr)

        def tempFiles = new TmpDirTemporaryFileProvider()

        resourceAccessor = new DefaultCacheAwareExternalResourceAccessor(
                this,
                indexer,
                bctp,
                tempFiles,
                lockMgr
        )
//        new DefaultCacheLockingManager(
//                new DefaultCacheRepository(
//                        new DefaultCacheScopeMapping(
//                                project.gradle.startParameter.gradleUserHomeDir,
//                                project.gradle.startParameter.projectCacheDir,
//                                project.gradle.gradleVersion
//                        ),
//                        new DefaultCacheFactory(
//                                new DefaultFileLockManager()
//                        )
//                )
//        )
//        resourceAccessor= gradle.services.get(CacheAwareExternalResourceAccessor)
//        resourceAccessor = new DefaultCacheAwareExternalResourceAccessor(this,
//                new ByUrlCachedExternalResourceIndex(
//                        'persistentCacheFile',bctp,gradle.services.get(DefaultCacheLockingManager.class)
//                        ),
//                bctp,
//                gradle.services.get(DefaultTemporaryFileProvider.class),
//                gradle.services.get(DefaultCacheLockingManager.class)
//        )

//         resourceAccessor = new DefaultCacheAwareExternalResourceAccessor(
// this,
// cachedExternalResourceIndex,
// bctp,
// gradle.services.get(DefaultTemporaryFileProvider.class),
// cacheLockingManager);

//        ProgressLoggerFactory progressLoggerFactory,
//                              TemporaryFileProvider temporaryFileProvider,
//        CachedExternalResourceIndex<String> cachedExternalResourceIndex,
//                                            BuildCommencedTimeProvider timeProvider,
//        CacheLockingManager cacheLockingManager)
// {
//            super(name);
//            HttpClientHelper http = new HttpClientHelper(new DefaultHttpSettings(credentials));
//            HttpResourceAccessor accessor = new HttpResourceAccessor(http);
//            HttpResourceUploader uploader = new HttpResourceUploader(http);
//         ProgressLoggingExternalResourceAccessor loggingAccessor = new ProgressLoggingExternalResourceAccessor(
// accessor, progressLoggerFactory);


    }

    @Override
    ExternalResourceRepository getRepository() {
        this
    }

    @Override
    CacheAwareExternalResourceAccessor getResourceAccessor() {
        this.resourceAccessor
    }

    @Override
    boolean isLocal() { false }


    /**
     * Obtain the SHA-1 checksum for the resource at the given location.
     *
     * Implementation is optional. If it is not feasible to obtain this without reading the
     * entire resource, implementations should return null.
     *
     * @param location The address of the resource to obtain the sha-1 of
     * @return The sha-1 if it can be cheaply obtained, otherwise null.
     */
    @Nullable
    HashValue getResourceSha1(URI location) { null }


    /**
     * Obtain the resource at the given location.
     *
     * If the resource does not exist, this method will return null.
     *
     * If the resource may exist but can't be accessed due to some configuration issue, the implementation
     * may either return null or throw an {@link IOException} to indicate a fatal condition.
     *
     * @param location The address of the resource to obtain
     * @return The resource if it exists, otherwise null
     * @throws IOException If the resource may exist, but not could be obtained for some reason
     */
    @Override
    @Nullable
    @CompileDynamic
    ExternalResource getResource(URI source) throws IOException {
        try {
            getVfsExternalResource(source,true)
        } catch (final Exception e) {
            throw new IOException("Could not obtain resource ${source}",e)
        }
    }

    /**
     * Transfer a resource to the repository
     *
     * @param source The local file to be transferred.
     * @param destination Where to transfer the resource.
     * @throws IOException On publication failure.
     */
    @Override
    void put(File source, URI destination) throws IOException {
        try {
            vfs.cp source,destination, overwrite : true, recursive : false
        } catch (final Exception e) {
            throw new IOException("Could not push ${source.absolutePath} to ${destination}",e)
        }
    }

    /**
     * Obtains only the metadata about the resource.
     *
     * If it is determined that the resource does not exist, this method will return null.
     *
     * If it is not possible to determine whether the resource exists or not, this method will
     * return a metadata instance with null/non value values (e.g. -1 for content length) to indicate
     * that the resource may indeed exist, but the metadata for it cannot be obtained.
     *
     * If the resource may exist but can't be accessed due to some configuration issue, the implementation
     * wo;; either return an empty metadata object or throw an {@link IOException} to indicate a fatal condition.
     *
     * @param location The location of the resource to obtain the metadata for
     * @return The available metadata if possible, an “empty” metadata object if the
     *         metadata can't be reliably be obtained, null if the resource doesn't exist
     * @throws IOException If the resource may exist, but not could be obtained for some reason
     */
    @Nullable
    @Override
    ExternalResourceMetaData getMetaData(URI location) throws IOException {
        getResourceMetaData(location)
    }
    /**
     * Fetches only the metadata for the result.
     *
     * @param source The location of the resource to obtain the metadata for
     * @return The resource metadata, or null if the resource does not exist
     */
    @Override
    @Nullable
    ExternalResourceMetaData getResourceMetaData(URI source) throws IOException {
        try {
            getVfsExternalResource(source,false)?.getMetaData()
        } catch (final IOException e) {
            throw e
        } catch (final Exception e) {
            throw new IOException("Could not obtain metadata for ${source}",e)
        }
    }

    /**
     * Return a listing of child resources names.
     *
     * @param parent The parent directory from which to generate the listing.
     * @return A listing of the direct children of the given parent. Returns null when the parent resource does not exist.
     * @throws IOException On listing failure.
     */
    @Override
    @Nullable
    @CompileDynamic
    List<String> list(URI parent) throws IOException {
        try {
            FileObject parentFile = vfs.resolveURI(parent)
            if(!parentFile.exists() || parentFile.fileSystem.hasCapability(Capability.LIST_CHILDREN)) {
                return null
            }

            List<String> contents = []
            vfs.ls(parent) { FileObject fo ->
                contents << fo.name.baseName
            }
            return contents

        } catch (final Exception e) {
            throw new IOException("Could not list content of ${parent}",e)
        }
    }

//    @Override
//    LocallyAvailableExternalResource getResource(
//            URI source,
//            CacheAwareExternalResourceAccessor.ResourceFileStore fileStore,
//            @Nullable LocallyAvailableResourceCandidates localCandidates
//    ) throws IOException {
//        try {
////            public interface LocallyAvailableResourceCandidates {
////
////                boolean isNone();
////
////                LocallyAvailableResource findByHashValue(HashValue hashValue);
////
////            }
//            FileObject fo = vfs.resolveURI(source)
//
//            if(!localCandidates.isNone()) {
//                LocallyAvailableResource lar = localCandidates.findByHashValue('foo')
//                if(null!=lar) {return lar}
//            }
//            (this.getResource(source) as VfsExternalResource).downloadNow()
//
//            throw new IOException("NOT IMPLEMENTED YET")
//        } catch (final Exception e) {
//            throw new IOException("Could not obtain resource ${source}",e)
//        }
//    }
//
//    class ResourceFileStore implements CacheAwareExternalResourceAccessor.ResourceFileStore {
//
//        /**
//         * Called when a resource is to be cached. Should *move* the given file into the
//         * appropriate location and return a handle to the file.
//         */
//        @Override
//        LocallyAvailableResource moveIntoCache(File downloadedResource) {
//            org.apache.commons.io.FileUtils.moveFileToDirectory(downloadedResource,,true)
//            throw new IOException("NOT IMPLEMENTED YET")
//            return null
//        }
//    }

    @CompileDynamic
    @groovy.transform.PackageScope
    VfsExternalResource getVfsExternalResource(URI source,boolean download) {
        FileObject fo = vfs.resolveURI(source)
        fo.exists() ?  new VfsExternalResource(vfs,fo,download:download) : null

    }
    private VFS vfs
    private CacheAwareExternalResourceAccessor resourceAccessor

}
/*
public DefaultCacheAwareExternalResourceAccessor(ExternalResourceAccessor delegate, CachedExternalResourceIndex<String> cachedExternalResourceIndex, BuildCommencedTimeProvider timeProvider, TemporaryFileProvider temporaryFileProvider, CacheLockingManager cacheLockingManager) {
    this.delegate = delegate;
    this.cachedExternalResourceIndex = cachedExternalResourceIndex;
    this.timeProvider = timeProvider;
    this.temporaryFileProvider = temporaryFileProvider;
    this.cacheLockingManager = cacheLockingManager;
}
*/