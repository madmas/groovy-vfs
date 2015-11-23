/*
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Ported to Groovy from the original Java code at
 * https://github.com/miltonio/milton2/blob/master/examples/milton-embedded
 */

package org.ysb33r.groovy.vfs.test.services

import groovy.transform.CompileStatic
import io.milton.config.HttpManagerBuilder
import io.milton.http.HttpManager
import io.milton.http.fs.FileSystemResourceFactory
import io.milton.http.fs.NullSecurityManager
import io.milton.simpleton.SimpletonServer

import java.io.*;
import java.util.*;

/**
 * @author Schalk W. Cronjé
 */
class WebdavServer {
    int port = 50081
    String bindInterface = 'localhost'
    File logDir = new File( './build/tmp/webdavserver/logs' )
    File webappDir = new File( './src/main/webapp')
    File homeFolder = new File( './build/tmp/webdavserver/files')

    WebdavServer( Map properties = [:] ) {
        properties.each { k,v ->
            this."${k}" = v
        }

        logFile = new File(logDir,'access/yyyy_mm_dd.request.log')
        FileSystemResourceFactory resourceFactory = new FileSystemResourceFactory(
            homeFolder,
            new NullSecurityManager(),
            "/"
        )

        resourceFactory.setAllowDirectoryBrowsing(true)
        HttpManagerBuilder b = new HttpManagerBuilder()
        b.setEnableFormAuth(false)
        b.setResourceFactory(resourceFactory)
        HttpManager httpManager = b.buildHttpManager()
        server = new SimpletonServer(httpManager, b.getOuterWebdavResponseHandler(), 100, 10)
        server.httpPort = port
    }

    @CompileStatic
    void start() {
        server.start()
    }

    @CompileStatic
    void stop() {
        server.stop()
    }

    private File logFile
    private SimpletonServer server
}
