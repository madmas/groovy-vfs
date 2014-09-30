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
package org.ysb33r.gradle.vfs.internal

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