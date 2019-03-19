package org.dice_research.ldcbench.nodes.ckan.dao;

import eu.trentorise.opendata.jackan.CheckedCkanClient;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanDatasetBase;
import eu.trentorise.opendata.jackan.model.CkanOrganization;


/**
 * 
 * DAO Object to insert and retrieve data from CKAN
 * 
 * @author gsjunior86
 *
 */
public class CkanDAO {
	
	private CheckedCkanClient ckanClient;
	
	
	/**
	 * Constructor requires a CKAN client
	 * 
	 * @param ckanClient
	 */
	public CkanDAO(CheckedCkanClient ckanClient) {
		this.ckanClient = ckanClient;
	}
	
	/**
	 * Insert a datasource in CKAN
	 * @param dataset
	 * @return returns the created dataset
	 */
	
	public CkanDataset insertDataSource(CkanDatasetBase dataset) {
		return ckanClient.createDataset(dataset);
	}
	
	/**
	 * 
	 * Delete datasource of a given name
	 * 
	 * @param datasetName
	 * @return true/false
	 */
	public boolean deleteDataSource(String datasetName) {
		try {
			CkanDataset dataSet = ckanClient.getDataset(datasetName);
			ckanClient.deleteDataset(dataSet.getId());
		}catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * Checks if an organization with a given name exists
	 * 
	 * @param organizationName Name of the organization
	 * @return true/false
	 */
	public boolean organizationExists(String organizationName) {
		for(CkanOrganization organization : ckanClient.getOrganizationList()) {
			if(organization.getName().equals(organizationName))
				return true;
		}
		return false;
	}
	
	
	

}
