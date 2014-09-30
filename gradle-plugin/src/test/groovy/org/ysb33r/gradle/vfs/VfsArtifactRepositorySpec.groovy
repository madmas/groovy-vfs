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

package org.ysb33r.gradle.vfs

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.artifacts.ivyservice.CacheLockingManager
import org.gradle.api.internal.artifacts.ivyservice.DefaultCacheLockingManager
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.ResolverStrategy
import org.gradle.api.internal.artifacts.metadata.ModuleVersionArtifactMetaData
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.resource.local.FileStore
import org.gradle.internal.resource.local.LocallyAvailableResourceFinder
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.*

/**
 * @author Schalk W. Cronjé.
 */
class VfsArtifactRepositorySpec extends spock.lang.Specification {

    Project project
    FileResolver fileResolver
    PasswordCredentials passwordCredentials
    LocallyAvailableResourceFinder<ModuleVersionArtifactMetaData> locallyAvailableResourceFinder
    ResolverStrategy resolverStrategy
    FileStore artifactFileStore
    VfsArtifactRepository repository
    CacheLockingManager cacheLockingManager

    void setup() {
        project = ProjectBuilder.builder().build()
        fileResolver = Mock(FileResolver)
        passwordCredentials= Mock(PasswordCredentials)
        locallyAvailableResourceFinder= Mock(LocallyAvailableResourceFinder)
        resolverStrategy= new ResolverStrategy()
        artifactFileStore= Mock(FileStore)
        cacheLockingManager= Mock(CacheLockingManager)
        repository = new VfsArtifactRepository( project,fileResolver,passwordCredentials,locallyAvailableResourceFinder,resolverStrategy,artifactFileStore )
    }

    def "Instantiate an Artifact Repository"() {

        expect:
            repository.name == null
            repository.url == null
    }

    def "Instantiate an Artifact Repository and call validate"() {
        when:
            repository.validate()

        then:
            thrown(InvalidUserDataException)
    }

    def "An Artifact Repository must be able to create a IvyResolver"() {
        when:
            repository.url = 'http://foo.example'
            repository.name = 'foo'
//            (project.gradle as GradleInternal).services.newInstance(cacheLockingManager.class)
            def ivy = repository.createResolver()

        then:
            ivy != null
    }
}
