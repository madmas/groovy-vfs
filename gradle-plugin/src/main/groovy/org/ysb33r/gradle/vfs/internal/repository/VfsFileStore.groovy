package org.ysb33r.gradle.vfs.internal.repository

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.internal.hash.HashUtil
import org.gradle.internal.hash.HashValue
import org.gradle.internal.resource.local.FileStore
import org.gradle.internal.resource.local.LocallyAvailableResource

/**
 * @author Schalk W. Cronjé.
 */
class VfsFileStore implements FileStore {

    VfsFileStore( File root, Logger log ) {
        this.root=root
        this.logger=log
    }

    @Override
    LocallyAvailableResource move(Object key, File source) {
        File dest=keyToLocation(key)
        File target=new File(dest,source.name)
        FileUtils.moveFileToDirectory(source,dest,true)
        LocallyAvailableResource lar= createLocallyAvailableResource(target)
        logger.debug "VfsFileStore: Moved ${source.absolutePath} -> ${dest.absolutePath}: SHA1=${lar.sha1}"
        lar
    }

    @Override
    LocallyAvailableResource copy(Object key, File source) {
        File dest=keyToLocation(key)
        File target=new File(dest,source.name)
        FileUtils.copyFileToDirectory(source,dest,true)
        LocallyAvailableResource lar= createLocallyAvailableResource(target)
        logger.debug "VfsFileStore: Copied ${source.absolutePath} -> ${dest.absolutePath}: SHA1=${lar.sha1}"
        lar
    }

    @Override
    void moveFilestore(File destination) {
        FileUtils.moveDirectory(root,destination)
        logger.debug "VfsFileStore: Move filestore to ${destination.absolutePath}"
        root=destination
    }

    @Override
    LocallyAvailableResource add(Object key, Action addAction) {
        throw GradleException("NOT IMPLEMENTED")
    }

    File keyToLocation(Object key) {
        new File(root,org.gradle.internal.FileUtils.toSafeFileName(key.toString()))
    }

    private File root
    private Logger logger

    static LocallyAvailableResource createLocallyAvailableResource(File target) {
        HashValue sha1 = HashUtil.sha1(target)
        [
                getFile : { -> target },
                getSha1 : { -> sha1 },
                getLastModified : { -> target.lastModified },
                getContentLength : { -> target.size() }
        ] as LocallyAvailableResource
    }
}
