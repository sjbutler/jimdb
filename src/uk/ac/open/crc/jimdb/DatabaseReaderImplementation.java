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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import uk.ac.open.crc.idtk.Species;


/**
 * Provides a variety of methods for accessing information in the database. Be 
 * cautious as this API will be subject to change, both through the addition 
 * of new functionality and the consolidation of old.
 *
 */
public class DatabaseReaderImplementation implements DatabaseReader {
    EntityDatabaseReader entityDatabaseReader;
    
    /**
     * Creates an instance of the class.
     */
    DatabaseReaderImplementation() {
        this.entityDatabaseReader = new EntityDatabaseReader();
    }
    
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<String> getProjectList() {
        return this.entityDatabaseReader.getProjectsList();
    }
    
    /**
     * {@inheritDoc}
     * @param identifierName {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<String> getTokensFor( String identifierName ) {
        return this.entityDatabaseReader.tokensFor( identifierName );
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<String> getPackageNamesForProject(String projectName) {
        return this.entityDatabaseReader.getPackageNamesForProject(projectName);
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @param packageName {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<String> getClassNamesForPackage(String projectName, String packageName) {
        return this.entityDatabaseReader.getClassNamesForPackage(projectName, packageName);
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @param species {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<String> getIdentifierNamesFor( String projectName, Species species ) {
        return this.entityDatabaseReader.getIdentifierNamesFor( projectName, species );
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<String> getIdentifierNamesFor( String projectName ) {
        return this.entityDatabaseReader.getIdentifierNamesFor( projectName );
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @param species {@inheritDoc}
     * @param count {@inheritDoc}
     * @param minimumLength v
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<String> getNameSetFor(
            String projectName, 
            Species species, 
            Integer count,
            Integer minimumLength) {
        return this.entityDatabaseReader.getNameSetFor(projectName, species, count, minimumLength);
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @param species {@inheritDoc}
     * @param count {@inheritDoc}
     * @param minimumLength {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<String> getTokenisedNameSetFor(
            String projectName, 
            Species species, 
            Integer count, 
            Integer minimumLength) {
        return this.entityDatabaseReader.getNameSetFor(projectName, species, count, minimumLength);
    }
    
    /**
     * {@inheritDoc}
     * @param species {@inheritDoc}
     * @param count {@inheritDoc}
     * @param minimumLength {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<String> getNameSetFor(
            Species species, 
            Integer count, 
            Integer minimumLength) {
        return this.entityDatabaseReader.getNameSetFor(species, count, minimumLength);
    }
    
    /**
     * {@inheritDoc}
     * @param species {@inheritDoc}
     * @param count {@inheritDoc}
     * @param minimumLength {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<String> getTokenisedNameSetFor(
            Species species, 
            Integer count, 
            Integer minimumLength) {
        return this.entityDatabaseReader.getNameSetFor(species, count, minimumLength);
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<InheritableProgramEntity> getAllClassNamesFor( String projectName ) {
        return this.entityDatabaseReader.getAllClassNamesFor( projectName );
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<ProgramEntity> getAllClassesAndInterfacesFor( String projectName ) {
        return this.entityDatabaseReader.getAllClassesAndInterfacesFor( projectName );
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<ProgramEntity> getAllFieldNamesFor( String projectName ) {
        return this.entityDatabaseReader.getAllFieldNamesFor( projectName );
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<ProgramEntity> getAllFormalArgumentNamesFor( String projectName ) {
        return this.entityDatabaseReader.getAllFormalArgumentNamesFor( projectName );
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<ProgramEntity> getAllLocalVariableNamesFor( String projectName ) {
        return this.entityDatabaseReader.getAllLocalVariableNamesFor( projectName );
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @param fqn {@inheritDoc}
     * @return {@inheritDoc}
     * @deprecated {@inheritDoc}
     */
    @Override
    @Deprecated
    public InheritableProgramEntity getClassOrInterfaceFor( 
            String projectName, 
            String fqn ) {
        return this.entityDatabaseReader.getClassOrInterfaceFor( projectName, fqn );
    }
    
    /**
     * {@inheritDoc}
     * @param className {@inheritDoc}
     * @param targetSpecies {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<InheritableProgramEntity> getEntityCandidatesFor( 
            String className, 
            Species targetSpecies ) {
        return this.entityDatabaseReader.getEntityCandidatesFor( 
                className, 
                targetSpecies );
    }
    
    /**
     * {@inheritDoc}
     * @param className {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<InheritableProgramEntity> getSubClassesFor( String className ) {
        return this.entityDatabaseReader.getSubclassesFor( className );
    }
    
    /**
     * {@inheritDoc}
     * @param className {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public ArrayList<InheritableProgramEntity> getSubTypesFor( String className ) {
        return this.entityDatabaseReader.getSubTypesFor( className );
    }
    
    /**
     * {@inheritDoc}
     * @param species {@inheritDoc}
     * @param maxCount {@inheritDoc}
     * @param typeGroup {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public HashSet<ProgramEntity> getEntitySetWhere( 
            Species species, 
            int maxCount, 
            TypeGroup typeGroup ) {
        return this.entityDatabaseReader.getEntitySetWhere( 
                species, 
                maxCount, 
                typeGroup );
    }
    
    /**
     * {@inheritDoc}
     * @param projectName {@inheritDoc}
     * @param projectVersion {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public List<ProgramEntity> getEntitiesFor( 
            String projectName, 
            String projectVersion ) {
        return this.entityDatabaseReader.getEntitiesFor( 
                projectName, 
                projectVersion );
    }
    
    /**
     * {@inheritDoc}
     * @param projectNameAndVersion {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public List<ProgramEntity> getEntitiesFor( String projectNameAndVersion ) {
        return this.entityDatabaseReader.getEntitiesFor( projectNameAndVersion );
    }
}
