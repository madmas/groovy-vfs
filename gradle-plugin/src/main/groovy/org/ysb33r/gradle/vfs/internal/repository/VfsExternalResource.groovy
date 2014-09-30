package org.ysb33r.gradle.vfs.internal.repository

import org.apache.commons.vfs2.FileObject
import org.gradle.api.Action
import org.gradle.api.Transformer
import org.gradle.internal.resource.ExternalResource
import org.gradle.internal.resource.metadata.ExternalResourceMetaData
import org.ysb33r.gradle.vfs.VfsExternalResourceMetaData
import org.ysb33r.groovy.dsl.vfs.VFS

/**
 * @author Schalk W. Cronjé.
 */
class VfsExternalResource implements ExternalResource {

    VfsExternalResource( VFS vfs, FileObject fo ) {
        this.vfs = vfs
        vfsFileObject = fo
    }

    /**
     * Get the URI of the resource.
     */
    @Override
    URI getURI() {
        vfsFileObject.getURL().toURI()
    }

    /**
     * Get the name of the resource. Use {@link #getURI()} instead.
     */
    @Override
    String getName() {
        vfsFileObject.name.baseName
    }

    /**
     * Get the resource size
     *
     * @return a <code>long</code> value representing the size of the resource in bytes.
     */
    @Override
    long getContentLength() {
        vfsFileObject.content.size()
    }

    /**
     * Is this resource local to this host, i.e. is it on the file system?
     *
     * @return <code>boolean</code> value indicating if the resource is local.
     */
    @Override
    boolean isLocal() {
        vfsFileObject.name.scheme == 'file'
    }

    /**
     * Copies the contents of this resource to the given file.
     */
    @Override
    void writeTo(File destination) throws IOException {
        try {
            vfs.cp vfsFileObject, destination, overwrite:true, recursive:false
        } catch (final Exception e) {
            throw new IOException("Could not write to ${destination.absolutePath}",e)
        }
    }

    /**
     * Copies the contents of this resource to the given stream. Does not close the stream.
     */
    @Override
    void writeTo(OutputStream destination) throws IOException {
        try {
            vfs.cat (vfsFileObject) { is ->
                destination << is
            }
        } catch (final Exception e) {
            throw new IOException("Could not write content to stream",e)
        }
    }

    /**
     * Executes the given action against the contents of this resource.
     */
    @Override
    void withContent(Action<? super InputStream> readAction) throws IOException {
        throw new IOException('Not implemented yet')
    }

    /**
     * Executes the given action against the contents of this resource.
     */
    @Override
    def <T> T withContent(Transformer<? extends T, ? super InputStream> readAction) throws IOException {
        throw new IOException('Not implemented yet')
        return null
    }

    @Override
    void close() throws IOException {
        try {
            vfsFileObject.close()
        } catch( final Exception e ) {
            throw new IOException("Failed to close",e)
        }
    }

    /**
     * Returns the meta-data for this resource.
     */
    @Override
    ExternalResourceMetaData getMetaData() {
        new VfsExternalResourceMetaData(
            location : getURI(),
            lastModified: vfsFileObject.content.lastModifiedTime,
            contentLength: getContentLength()
        )
    }

    private VFS vfs
    private FileObject vfsFileObject
}
