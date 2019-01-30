package org.dice_research.squirrel.adapter;

import java.io.IOException;

import org.hobbit.core.components.AbstractSystemAdapter;

/**
 * 
 * @author gsjunior86
 *
 */
public class SquirrelAdapter extends AbstractSystemAdapter {
	
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		super.init();
	}

	@Override
	public void receiveGeneratedData(byte[] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveGeneratedTask(String taskId, byte[] data) {
		
		byte[] result = null;
		
		try {
			sendResultToEvalStorage(taskId, result);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
