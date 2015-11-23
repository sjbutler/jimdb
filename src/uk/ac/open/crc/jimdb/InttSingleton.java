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

import java.util.List;
import uk.ac.open.crc.intt.IdentifierNameTokeniser;
import uk.ac.open.crc.intt.IdentifierNameTokeniserFactory;

/**
 * Provides a class within the package for managing instances of intt.
 */
class InttSingleton {

    private static InttSingleton instance = null;
    
    /**
     * Retrieves the instance of the intt tokeniser.
     * @return a tokeniser
     */
    synchronized static InttSingleton getInstance() {
        if ( instance == null ) {
            instance = new InttSingleton();
        }
        
        return instance;
    }
    
    // -------------------------
    
    
    private final IdentifierNameTokeniser tokeniser;
    private final IdentifierNameTokeniserFactory factory;
    
    private InttSingleton() {
        InttConfiguration config = InttConfiguration.getInstance();
        this.factory = new IdentifierNameTokeniserFactory();
        if ( config.getRecursiveSplit() ) {
            this.factory.setRecursiveSplitOn();
        }
        if ( config.getModalExpansion() ) {
            this.factory.seModalExpansionOn();
        }
  
        this.tokeniser = this.factory.create();
    }
    
    /**
     * Invokes the tokeniser.
     * @param identifierName a name to tokenise
     * @return an list of the aplphanumeric tokens found in the name
     */
    synchronized List<String> tokenise(String identifierName) {
        return this.tokeniser.tokenise(identifierName);
    }
}
