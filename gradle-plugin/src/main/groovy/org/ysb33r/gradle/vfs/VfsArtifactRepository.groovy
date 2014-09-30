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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.gradle.api.*
import org.gradle.api.artifacts.repositories.*
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.ConfiguredModuleComponentRepository
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.ResolverStrategy
import org.gradle.api.internal.artifacts.metadata.ModuleVersionArtifactMetaData
import org.gradle.api.internal.artifacts.repositories.ResolutionAwareRepository
import org.gradle.api.internal.artifacts.repositories.resolver.IvyResolver
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.resource.local.FileStore
import org.gradle.internal.resource.local.LocallyAvailableResourceFinder
import org.gradle.util.CollectionUtils
import org.ysb33r.gradle.vfs.internal.repository.VfsRepositoryTransport

@CompileStatic
class VfsArtifactRepository implements ArtifactRepository,ResolutionAwareRepository {

    VfsArtifactRepository(
        Project project,
        FileResolver fileResolver,
        PasswordCredentials passwordCredentials,
        LocallyAvailableResourceFinder<ModuleVersionArtifactMetaData> locallyAvailableResourceFinder,
        ResolverStrategy resolverStrategy,
        FileStore artifactFileStore
    ) {
        File root=new File(project.gradle.startParameter.projectCacheDir,'vfs')
        this.project = project
        this.fileResolver = fileResolver
        this.locallyAvailableResourceFinder = locallyAvailableResourceFinder
        this.resolverStrategy = resolverStrategy
        this.artifactFileStore = artifactFileStore //new VfsFileStore( root, project.logger )
        this.passwordCredentials = passwordCredentials
    }

    void setPatterns(final Object... pat) {
        patterns.clear()
        patterns.addAll(CollectionUtils.stringize(pat as List))
    }

    void pattern(final Object pat) {
        patterns.add(pat.toString())
    }

    void url(final String u) { this.url=u }

    String getUrl() { this.url }

    void name(final String n) {this.name=n}

    ConfiguredModuleComponentRepository/*IvyResolver*/ createResolver() {
        validate()

        def resolver = new IvyResolver(
                name,
                new VfsRepositoryTransport(project,name),
                locallyAvailableResourceFinder,
                false,
                resolverStrategy,
                artifactFileStore
        )

        URI uri = url.toURI()
        if(patterns.empty) {
            // TODO: Not sure whether we should have empty. Maybe we should always force at least one pattern
            resolver.addArtifactLocation(uri, '/[artifact]-[revision](-[classifier]).[ext]')
        } else {
            patterns.each { String it ->
                resolver.addArtifactLocation(uri, '/'+it)
            }
        }

        resolver
    }

    @PackageScope
    VfsArtifactRepository validate() {

        if( url != null ) {
            return this
        }

        throw new InvalidUserDataException ('vfs repository requires a base url')
    }

    @PackageScope
    Project project

    @PackageScope
    List<String> patterns = []

    String name
    private String url
    private Project project
    private FileResolver fileResolver
    private LocallyAvailableResourceFinder locallyAvailableResourceFinder
    private ResolverStrategy resolverStrategy
    private FileStore artifactFileStore
    private PasswordCredentials passwordCredentials

}

