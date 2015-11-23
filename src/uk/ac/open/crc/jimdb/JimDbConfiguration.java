/*
 Copyright (C) 2010-2015 The Open University

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

package uk.ac.open.crc.jimdb;

/**
 * Provides a blackboard for jimdb classes to store
 * configuration values. The class is only used during 
 * data storage phase currently. Very much a work in progress.
 */
class JimDbConfiguration {
    private static JimDbConfiguration instance = null;

    /**
     * Makes the instance of this class available.
     * @return the instance of this class
     */
    static JimDbConfiguration getInstance() {
        if (instance == null) {
            instance = new JimDbConfiguration();
        }
        
        return instance;
    }
    
    private Integer projectKey;
    private String projectName;
    private String projectVersion;
    
    private JimDbConfiguration() {
        this.projectKey = null;
        
        this.projectName = null;
        this.projectVersion = null;
    }
    
    /**
     * Recovers the database key for the current project.
     * @return 
     */
    Integer getProjectKey() {
        return this.projectKey;
    }
    
    /**
     * Store a database key for the current project.
     * @param key a database key
     */
    void setProjectKey(Integer key) {
        this.projectKey = key;
    }
    
    /**
     * Recover the project version.
     * @return the project version
     */
    String getProjectVersion() {
        return this.projectVersion;
    }
    
    /**
     * Recover the project name.
     * @return the project name
     */
    String getProjectName() {
        return this.projectName;
    }
    
    /**
     * Store a name for the current project.
     * @param name a project name
     */
    void setProjectName(String name) {
        this.projectName = name;
    }
    
    /**
     * Store a version for the current project.
     * @param version a version string
     */
    void setProjectVersion(String version) {
        this.projectVersion = version;
    }
    
    void setLoggingLevel( String level ) {
        
    }
}
