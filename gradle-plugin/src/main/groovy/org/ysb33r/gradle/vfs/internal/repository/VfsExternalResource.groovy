// ============================================================================
// (C) Copyright Schalk W. Cronje 2014
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================
package org.ysb33r.gradle.vfs.internal.repository

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.commons.vfs2.FileObject
import org.gradle.api.Action
import org.gradle.api.Transformer
import org.gradle.internal.hash.HashUtil
import org.gradle.internal.hash.HashValue
import org.gradle.internal.resource.ExternalResource
import org.gradle.internal.resource.LocallyAvailableExternalResource
import org.gradle.internal.resource.local.LocallyAvailableResource
import org.gradle.internal.resource.metadata.ExternalResourceMetaData
import org.ysb33r.groovy.dsl.vfs.VFS

/**
 * @author Schalk W. Cronjé.
 */
@CompileStatic
class VfsExternalResource implements LocallyAvailableExternalResource {

    @CompileDynamic
    VfsExternalResource( properties=[:], VFS vfs, FileObject fo ) {
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
        vfsFileObject.content.size
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
            vfs.cat (vfsFileObject) { InputStream is ->
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
    @CompileDynamic
    ExternalResourceMetaData getMetaData() {
        new VfsExternalResourceMetaData(
            location : getURI(),
            lastModified: new Date(vfsFileObject.content.lastModifiedTime),
            contentLength: getContentLength()
        )
    }

    @Override
    @CompileDynamic
    LocallyAvailableResource getLocalResource() {
        if(downloaded == null) {
//            File tmp = new File('BASEDIR') ????
//            downloaded =
        }
        def md = this.metaData
        File downloaded
        HashValue sha1 = HashUtil.sha1(downloaded)
        [
            getFile : { -> downloaded },
            getSha1 : { -> sha1 },
            getLastModified : { -> md.lastModified },
            getContentLength : { -> downloaded.size }

        ] as LocallyAvailableResource
    }

    private VFS vfs
    private FileObject vfsFileObject
    private File downloaded = null

}

