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

import org.gradle.api.InvalidUserDataException

import java.io.File;

import spock.lang.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder
import org.apache.commons.vfs2.FileSystemOptions
import org.ysb33r.groovy.dsl.vfs.URI

class VfsPluginSpec extends spock.lang.Specification {
    File testFsReadOnlyRoot = new File("${System.getProperty('TESTFSREADROOT') ?: '.'}/src/test/resources").absoluteFile
    String testFsURI = new URI(testFsReadOnlyRoot).toString()
    File testFsWriteRoot= new File( "${System.getProperty('TESTFSWRITEROOT') ?: 'build/tmp/test-files'}/file")
    String testFsWriteURI= new URI(testFsWriteRoot).toString()
    Project project = ProjectBuilder.builder().build()
    
    void setup() {
        testFsWriteRoot.deleteDir()
        project.apply plugin:'org.ysb33r.vfs'
    }

    def "Can apply Vfs plugin to project"() {
        expect:
            project.__vfs instanceof org.ysb33r.groovy.dsl.vfs.VFS
            project.repositories.metaClass.vfs != null
    }
    
    def "Must be able to set vfs style properties via a configuration-style block"() {

        given:
            FtpFileSystemConfigBuilder fscb
            fscb = project.__vfs.fsMgr.getFileSystemConfigBuilder('ftp') as FtpFileSystemConfigBuilder
            fscb.setPassiveMode( project.__vfs.defaultFSOptions, false)
            project.vfs {
                    options {
                            ftp {
                                passiveMode true
                            }
                    }
            }

        expect:
            fscb.getPassiveMode( project.__vfs.defaultFSOptions )
    }
    
    def "Calling VFS closures should execute immediately"() {
        
        given:
            Integer count= 0
            project.vfs {
                cp testFsURI,"${testFsWriteURI}/one/two/three", recursive:true
            }
            testFsWriteRoot.eachFileRecurse {
                if (it.name =~ /file\d\.txt/ ) { 
                    ++count 
                }
            }
            
        expect:
          count == 4
        
    }

    def "Trying to add a vfs repository without a url will throw an exception"() {
        when:
            project.repositories {
                vfsRoot {
                }
            }
        then:
            thrown(InvalidUserDataException)
    }

    def "Must be able to add a vfs repository"() {
        when:
            project.repositories {
                vfsRoot {
                    name 'testvfs'
                    url "file://${project.buildDir.absolutePath}"
                    pattern '[artifact]-[revision](-[classifier]).[ext]'
                    pattern '[artifact]/[revision](-[classifier]).[ext]'
                }
            }
        then:
            project.repositories.withType(VfsArtifactRepository).size() != 0
            project.repositories.getByName('testvfs').url == "file://${project.buildDir.absolutePath}"
            project.repositories.getByName('testvfs').patterns.contains('[artifact]-[revision](-[classifier]).[ext]')
            project.repositories.getByName('testvfs').patterns.contains('[artifact]/[revision](-[classifier]).[ext]')
    }

    def "Find files in flat vfs repo"() {
        given: "A VFS repository"
            project.repositories {
                vfsRoot {
                    name 'testvfs'
                    url new File(testFsReadOnlyRoot,'vfsRepo').toURI().toString()
                    pattern '[artifact]-[revision](-[classifier]).[ext]'
                }
            }
        when: "Adding a file dependency"
            project.configurations {
                foo
                classified
            }
            project.dependencies {
                foo 'foobar:artifact:1.1.1@txt'
                classified 'foobar:artifact:1.1.1:jumbo@txt'
            }
            def fooFiles = project.configurations.foo.files
            def classifiedFiles = project.configurations.classified.files

        then: "Files with extensions should be resolved"
            fooFiles.size() == 1
            fooFiles.find { it.name.endsWith('artifact-1.1.1.txt') }

        and: "Files with classifiers should be resolved"
            classifiedFiles.size() == 1
            classifiedFiles.find { it.name.endsWith('artifact-1.1.1-jumbo.txt') }

    }
    def "Find files in nested vfs repo"() {
        given: "A VFS repository"
            project.repositories {
                vfsRoot {
                    name 'testvfs'
                    url new File(testFsReadOnlyRoot,'vfsRepo').toURI().toString()
                    pattern '[organisation]/[artifact]-[revision](-[classifier]).[ext]'
                }
            }
        when: "Adding a file dependency"
            project.configurations {
                foo
            }
            project.dependencies {
                foo 'groupFolder:artifact:1.2.1@txt'
            }
            def fooFiles = project.configurations.foo.files

        then: "Files should be resolved by folder"
            fooFiles.size() == 1
            fooFiles.find { it.name.endsWith('artifact-1.2.1.txt') }

    }
}

