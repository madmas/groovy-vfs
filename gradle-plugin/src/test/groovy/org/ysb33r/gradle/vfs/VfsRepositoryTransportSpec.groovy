package org.ysb33r.gradle.vfs

import org.gradle.api.Project
import org.gradle.internal.resource.transfer.CacheAwareExternalResourceAccessor
import org.gradle.internal.resource.transport.ExternalResourceRepository
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.gradle.vfs.internal.repository.VfsRepositoryTransport
import spock.lang.Specification


/**
 * @author Schalk W. Cronjé.
 */
class VfsRepositoryTransportSpec extends Specification {

    Project project = ProjectBuilder.builder().build()
    VfsRepositoryTransport transport = new VfsRepositoryTransport(project,'test')

    def "Must implement RepositoryTransport"() {

        expect: "Repository to never be local"
            transport.isLocal() == false

        and: "Repository to be instantiated"
            transport.repository instanceof ExternalResourceRepository
            transport.repository != null

        and: "Resource access to be available"
            transport.resourceAccessor instanceof CacheAwareExternalResourceAccessor
            transport.resourceAccessor != null
    }
}