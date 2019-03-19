package org.dice_research.ldcbench.nodes.ckan.simple;

import org.dice_research.ldcbench.nodes.ckan.dao.CkanDAO;
import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.hobbit.core.components.Component;

import eu.trentorise.opendata.jackan.CheckedCkanClient;
import eu.trentorise.opendata.jackan.CkanClient;

public class SimpleCkanComponent extends AbstractCommandReceivingComponent implements Component {
	
	private static final String TOKEN = "8b40fc3b-2889-4341-a3bd-c8ff16661544";
	private static CkanDAO ckanDAO;
	
	public static void main(String[] args) {
//		CkanClient cc = new CkanClient("http://localhost:80",TOKEN);
		CkanClient cc = new CheckedCkanClient("http://localhost:80", TOKEN);
		
		
//		ckanDAO.insertDataSource(dataset);
		
	}
	
	
//	private void createDataset() {
//		
//	}

	@Override
	public void init() throws Exception {
		super.init();
		
//		createContainer("ckanContainer", null);
		
		ckanDAO = new CkanDAO(new CheckedCkanClient("http://localhost:80", TOKEN));

	}

	@Override
	public void receiveCommand(byte command, byte[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() throws Exception {
		synchronized (this) {
            this.wait();
        }
	}

}
