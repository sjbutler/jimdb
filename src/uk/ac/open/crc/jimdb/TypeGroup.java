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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Provides a means of categorising types. Categories are simplistic
 * and designed to allow caller to extract groups of names that 
 * may have some semantic or structural differences because of the
 * types they are used for. NB this is experimental and thus volatile.
 * 
 * <p>
 * Types are classified as boolean, numeric, object reference, string
 * or void. The latter is only relevant for method return types.
 * </p>
 */
enum TypeGroup {
    BOOLEAN,
    NUMERIC,
    REFERENCE,
    STRING,
    VOID;
    
    /**
     * Classifies a type as boolean, numeric, object reference, or string.
     * @param o an object
     * @return the classification of the object
     */
    static TypeGroup classify( Object o ) {
        if ( o instanceof Boolean ) {
            return BOOLEAN;
        }
        else if ( o instanceof String ) {
            return STRING;
        }
        else if ( o instanceof Integer 
                || o instanceof Double 
                || o instanceof Float 
                || o instanceof Long 
                || o instanceof Short
                || o instanceof BigDecimal
                || o instanceof BigInteger ) {
            return NUMERIC;
        } 
        else {
            return REFERENCE;
        }
    }
    
    /**
     * Classifies a {@code boolean} primitive.
     * @param b a {@code boolean} primitive
     * @return {@code BOOLEAN}
     */
    static TypeGroup classify( boolean b ) {
        return BOOLEAN;
    }
    
    /**
     * Classifies a byte primitive.
     * @param b a {@code byte} primitive
     * @return {@code REFERENCE}
     */
    static TypeGroup classify( byte b ) {
        return REFERENCE;
    }
    
    /**
     * Classifies a {@code char} primitive.
     * @param c a {@code char} primitive
     * @return {@code REFERENCE}
     */
    static TypeGroup classify( char c ) {
        return REFERENCE;
    }
    
    /**
     * Classifies a {@code double} primitive.
     * @param d a {@code double} primitive
     * @return {@code NUMERIC}
     */
    static TypeGroup classify( double d ) {
        return NUMERIC;
    }
    
    /**
     * Classifies a {@code float} primitive.
     * @param f a {@code float} primitive
     * @return {@code numeric}
     */
    static TypeGroup classify( float f ) {
        return NUMERIC;
    }
    
    /**
     * Classifies an {@code int} primitive.
     * @param i an {@code int} primitive
     * @return {@code NUMERIC}
     */
    static TypeGroup classify( int i ) {
        return NUMERIC;
    }
    
    /**
     * Classifies a {@code long} primitive.
     * @param l a {@code long} primitive
     * @return {@code NUMERIC}
     */
    static TypeGroup classify( long l ) {
        return NUMERIC;
    }
    
    /**
     * Classifies a {@code short} primitive.
     * @param s a {@code short} primitive
     * @return {@code NUMERIC}
     */
    static TypeGroup classify( short s ) {
        return NUMERIC;
    }
    
    /**
     * Classifies a type name expressed in a string.
     * @param typeName a string containing a type name
     * @return a classification
     */
    static TypeGroup classifyFromString( String typeName ) {
        TypeGroup classification;
        
        switch ( typeName ) {
            case "boolean": 
            case "Boolean":
                classification = BOOLEAN;
                break;
            case "BigDecimal":
            case "BigInteger":
            case "double":
            case "Double":
            case "float":
            case "Float":
            case "int":
            case "Integer":
            case "long":
            case "Long":
            case "short":
            case "Short":
                classification = NUMERIC;
                break;
            case "String": 
                classification = STRING;
                break;
            case "void":
                classification = VOID;
                break;
            default: classification = REFERENCE;    
        }
        
        return classification;
    }
    
}
