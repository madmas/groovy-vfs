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
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.strategy.ResolverStrategy
import org.gradle.api.internal.artifacts.metadata.ModuleVersionArtifactMetaData
import org.gradle.api.internal.artifacts.repositories.resolver.IvyResolver
import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.resource.local.FileStore
import org.gradle.internal.resource.local.LocallyAvailableResourceFinder
import org.gradle.util.CollectionUtils
import org.ysb33r.gradle.vfs.internal.repository.VfsRepositoryTransport

@CompileStatic
class VfsArtifactRepository implements ArtifactRepository {
    String name
    String url

    VfsArtifactRepository(
        Project project,
        FileResolver fileResolver,
        PasswordCredentials passwordCredentials,
        LocallyAvailableResourceFinder<ModuleVersionArtifactMetaData> locallyAvailableResourceFinder, //DefaultModuleVersionArtifactMetaData
        ResolverStrategy resolverStrategy,
        FileStore artifactFileStore
    ) {
        File root=new File(project.gradle.startParameter.projectCacheDir,'vfs')
        this.project = project
        this.fileResolver = fileResolver
        this.locallyAvailableResourceFinder = locallyAvailableResourceFinder //new PatternBasedLocallyAvailableResourceFinder(root,)
        this.resolverStrategy = resolverStrategy // ResolverStrategy()
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

    @PackageScope
    IvyResolver createResolver() {
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
                resolver.addArtifactLocation(uri, it)
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



    private Project project
    private FileResolver fileResolver
    private LocallyAvailableResourceFinder locallyAvailableResourceFinder
    private ResolverStrategy resolverStrategy
    private FileStore artifactFileStore
    private PasswordCredentials passwordCredentials

//    @CompileDynamic
//    static VfsArtifactRepository create( Project project,Closure cfg ) {
//        def repo = new VfsArtifactRepository(project)
//        def c = cfg.clone()
//        c.delegate = repo
//        c()
//
//        repo.validate()
//    }

//    static VfsArtifactRepository create(Action<? super VfsArtifactRepository> action) {
//
//    }

//    @CompileDynamic
//    static VfsArtifactRepository addTo( RepositoryHandler rh, def action ) {
//        VfsArtifactRepository repo = create(action)
//        rh.add( repo )
//        repo
//    }
}

/*

    public IvyResolver(
        String name,
        RepositoryTransport transport,
        LocallyAvailableResourceFinder<ModuleComponentArtifactMetaData> locallyAvailableResourceFinder,
                       boolean dynamicResolve,
                       ResolverStrategy resolverStrategy,
                       FileStore<ModuleComponentArtifactMetaData> artifactFileStore)


public class DefaultFlatDirArtifactRepository extends AbstractArtifactRepository implements FlatDirectoryArtifactRepository, ResolutionAwareRepository, PublicationAwareRepository {
    private final FileResolver fileResolver;
    private List<Object> dirs = new ArrayList<Object>();
    private final RepositoryTransportFactory transportFactory;
    private final LocallyAvailableResourceFinder<ModuleComponentArtifactMetaData> locallyAvailableResourceFinder;
    private final ResolverStrategy resolverStrategy;
    private final FileStore<ModuleComponentArtifactMetaData> artifactFileStore;

    public DefaultFlatDirArtifactRepository(FileResolver fileResolver,
                                            RepositoryTransportFactory transportFactory,
                                            LocallyAvailableResourceFinder<ModuleComponentArtifactMetaData> locallyAvailableResourceFinder,
                                            ResolverStrategy resolverStrategy,
                                            FileStore<ModuleComponentArtifactMetaData> artifactFileStore) {
        this.fileResolver = fileResolver;
        this.transportFactory = transportFactory;
        this.locallyAvailableResourceFinder = locallyAvailableResourceFinder;
        this.resolverStrategy = resolverStrategy;
        this.artifactFileStore = artifactFileStore;
    }

    public Set<File> getDirs() {
        return fileResolver.resolveFiles(dirs).getFiles();
    }

    public void setDirs(Iterable<?> dirs) {
        this.dirs = Lists.newArrayList(dirs);
    }

    public void dir(Object dir) {
        dirs(dir);
    }

    public void dirs(Object... dirs) {
        this.dirs.addAll(Arrays.asList(dirs));
    }

    public ModuleVersionPublisher createPublisher() {
        return createRealResolver();
    }

    public ConfiguredModuleComponentRepository createResolver() {
        return createRealResolver();
    }

    private IvyResolver createRealResolver() {
        Set<File> dirs = getDirs();
        if (dirs.isEmpty()) {
            throw new InvalidUserDataException("You must specify at least one directory for a flat directory repository.");
        }

        IvyResolver resolver = new IvyResolver(
            getName(),
            transportFactory.createTransport("file", getName(), null),
            locallyAvailableResourceFinder,
            false,
            resolverStrategy,
            artifactFileStore);

        for (File root : dirs) {
            resolver.addArtifactLocation(root.toURI(), "/[artifact]-[revision](-[classifier]).[ext]");
            resolver.addArtifactLocation(root.toURI(), "/[artifact](-[classifier]).[ext]");
        }
        return resolver;
    }

}

 */