package org.ysb33r.gradle.vfs.internal.repository

import org.apache.commons.logging.LogFactory
import org.apache.commons.vfs2.FileObject
import org.gradle.api.Nullable
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransport
import org.gradle.internal.FileUtils
import org.gradle.internal.resource.ExternalResource
import org.gradle.internal.resource.metadata.ExternalResourceMetaData
import org.gradle.internal.resource.transfer.CacheAwareExternalResourceAccessor
import org.gradle.internal.resource.transport.ExternalResourceRepository
import org.ysb33r.groovy.dsl.vfs.VFS

/**
 * @author Schalk W. Cronjé.
 */
class VfsRepositoryTransport implements RepositoryTransport, ExternalResourceRepository {

    VfsRepositoryTransport(Project project,final String name) {
        vfs = new VFS (
                logger : LogFactory.getLog('vfs'),
                temporaryFileStore : "${project.gradle.gradleUserHomeDir}/vfs/${FileUtils.toSafeFileName(name)}"
        )
    }

    @Override
    ExternalResourceRepository getRepository() {
        return this
    }

    @Override
    CacheAwareExternalResourceAccessor getResourceAccessor() {
        return null
    }

    @Override
    boolean isLocal() { false }

    /**
     * Attempts to fetch the given resource.
     *
     * @return null if the resource is not found.
     */
    @Override
    ExternalResource getResource(URI source) throws IOException {
        try {
            return null

        } catch (final Exception e) {
            throw new IOException("Could not push ${source.absolutePath} to ${destination}",e)
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
     * Fetches only the metadata for the result.
     *
     * @param source The location of the resource to obtain the metadata for
     * @return The resource metadata, or null if the resource does not exist
     */
    @Override
    @Nullable
    ExternalResourceMetaData getResourceMetaData(URI source) throws IOException { null }

    /**
     * Return a listing of child resources names.
     *
     * @param parent The parent directory from which to generate the listing.
     * @return A listing of the direct children of the given parent. Returns null when the parent resource does not exist.
     * @throws IOException On listing failure.
     */
    @Override
    @Nullable
    List<String> list(URI parent) throws IOException {
        try {
            // TODO: interrogate for Capability.LIST_CHILDREN and if found return null
            List<String> contents = []
            vfs.ls(parent) { FileObject fo ->
                contents << fo.name.baseName
            }
            return contents

        } catch (final Exception e) {
            throw new IOException("Could not list content of  ${destination}",e)
        }
    }

    private VFS vfs


}
