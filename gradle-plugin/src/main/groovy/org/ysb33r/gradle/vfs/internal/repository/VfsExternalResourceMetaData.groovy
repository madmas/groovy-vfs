package org.ysb33r.gradle.vfs.internal.repository

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import org.gradle.internal.hash.HashValue
import org.gradle.internal.resource.metadata.ExternalResourceMetaData

/**
 * @author Schalk W. Cronjé.
 */
@CompileStatic
@TupleConstructor
class VfsExternalResourceMetaData implements ExternalResourceMetaData, Serializable {

    URI location
    Date lastModified
    long contentLength = -1

    /**
     * Some kind of opaque checksum that was advertised by the remote “server”.
     *
     * For HTTP this is likely the value of the ETag header but it may be any kind of opaque checksum.
     *
     * @return The entity tag, or null if there was no advertised or suitable etag.
     */
    @Override
    String getEtag() { null }

    /**
     * The advertised sha-1 of the external resource.
     *
     * This should only be collected if it is very cheap to do so. For example, some HTTP servers send an
     * “X-Checksum-Sha1” that makes the sha1 available cheaply. In this case it makes sense to advertise this as metadata here.
     *
     * @return The sha1, or null if it's unknown.
     */
    @Override
    HashValue getSha1() { null }
}
