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
 * An object used to pass configuration information into the 
 * database layer. The configuration options available are kept
 * simple, but may be subject to change if there is sufficient
 * need. 
 *
 */
class InttConfiguration {
    
    private static InttConfiguration instance = null;
    
    /**
     * Recovers the instance of the configuration class.
     * @return the instance of this class
     */
    static InttConfiguration getInstance() {
        if ( instance == null ) {
            instance = new InttConfiguration();
        }
        
        return instance;
    }
    
        
    // ---------------------------
    private boolean recursiveSplit;
    
    private boolean modalExpansion;
    
    private InttConfiguration() {
        this.recursiveSplit = false;
        this.modalExpansion = false;
    }
    
    /**
     * Sets intt so that it will process single tokens 
     * more aggressively if it doesn't recognise them. The default setting is
     * off.
     */
    void setResursiveSplitOn() {
        this.recursiveSplit = true;
    }
    
    /**
     * Sets intt so that it will not try to split unrecognised tokens.
     */
    void setRecursiveSplitOff() {
        this.recursiveSplit = false;
    }
    
    /**
     * Sets intt to expand modal contractions. For example, 'cant' is 
     * expanded to 'can not'. Default setting is off.
     */
    void setModalExpansionOn() {
        this.modalExpansion = true;
    }
    
    /**
     * Switches modal expansion off.
     */
    void setModalExpansionOff() {
        this.modalExpansion = false;
    }
    
    /**
     * Retrieves the current state of the modal expansion switch.
     * @return {@code true} if modal expansion is enabled
     */
    boolean getModalExpansion() {
        return this.modalExpansion;
    }
    
    /**
     * Retrieves the state of the recursive split switch.
     * @return {@code true} if recursive splits are enabled
     */
    boolean getRecursiveSplit() {
        return this.recursiveSplit;
    }
    
    void setLoggingLevel( String level ) {
        
    }
    
    
}
