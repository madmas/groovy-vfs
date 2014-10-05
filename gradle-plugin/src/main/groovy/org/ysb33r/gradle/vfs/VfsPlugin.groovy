// ============================================================================
// (C) Copyright Schalk W. Cronje 2013
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

import org.gradle.api.*
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.internal.ClosureBackedAction
import org.gradle.api.internal.artifacts.BaseRepositoryFactory
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler
import org.gradle.api.internal.artifacts.repositories.DefaultBaseRepositoryFactory
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.project.ProjectInternal
import org.ysb33r.groovy.dsl.vfs.VFS
import org.apache.commons.logging.LogFactory

class VfsPlugin implements Plugin<Project> {
        
    void apply(Project project) {
        addProjectExtension(project)
    }
    
    void addProjectExtension(Project project) {
        addVfsProjectExtension(project)
        hookIntoArtifactRepositoryHandlers(project)
    }

    void addVfsProjectExtension(Project project) {
        if(!project.metaClass.respondsTo(project,'vfs',Closure)) {

            project.ext.__vfs = new VFS (
                    logger : LogFactory.getLog('vfs'),
                    temporaryFileStore : "${project.gradle.gradleUserHomeDir}/vfs".toString()
            )
            project.ext.vfs = { Closure c -> project.__vfs.script(c) }
            project.logger.debug 'Added project.vfs'
        }
    }

    void hookIntoArtifactRepositoryHandlers(Project project) {
        RepositoryHandler repositories = project.repositories

        Project hookedProject = project
        DefaultBaseRepositoryFactory.metaClass.createVfsRepository = { ->
            new VfsArtifactRepository(
                    hookedProject,
                    fileResolver,
                    createPasswordCredentials(),
                    locallyAvailableResourceFinder,
                    resolverStrategy,
                    artifactFileStore
            )
        }

        if(!repositories.metaClass.respondsTo(repositories,'vfsRoot',Object)) {
            assert repositories instanceof DefaultRepositoryHandler

            def factory= ((ProjectInternal) project).getServices().get(BaseRepositoryFactory.class)

            repositories.metaClass.vfsRoot = { Object action ->
                final String defaultName = VfsArtifactRepository.class.simpleName
                VfsArtifactRepository repo
                if(action instanceof Closure) {
                    repo = addRepository(
                        factory.createVfsRepository(),
                        defaultName,
                        new ClosureBackedAction<VfsArtifactRepository>(action)
                    ) as VfsArtifactRepository

                } else if (action instanceof Action<? super VfsArtifactRepository>) {
                    repo = addRepository(factory.createVfsRepository(),defaultName,action) as VfsArtifactRepository
                } else {
                    throw UnsupportedOperationException("Cannot create a vfs repository with ${action.class.name} as a type. "+
                            "Use a Closure or an Action instead")
                }

                if(repo.url == null) {
                    throw new InvalidUserDataException ("Cannot create a VFS repository without a URL")
                }
            }
        }

    }
}

