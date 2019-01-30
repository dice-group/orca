package org.dice_research.squirrel.adapter;

import java.io.IOException;

import org.hobbit.core.components.AbstractSystemAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author gsjunior86
 *
 */
public class SquirrelAdapter extends AbstractSystemAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(SquirrelAdapter.class);

	
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		super.init();
	}

	@Override
	public void receiveGeneratedData(byte[] data) {
		String dataStr = new String(data);
	    logger.trace("receiveGeneratedData(" + new String(data) + "): " + dataStr);
		
	}

	@Override
	public void receiveGeneratedTask(String taskId, byte[] data) {
		
		String result = "result_" + taskId;
		
		try {
			sendResultToEvalStorage(taskId, result.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

}
