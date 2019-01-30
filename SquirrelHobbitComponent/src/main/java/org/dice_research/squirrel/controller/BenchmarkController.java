package org.dice_research.squirrel.controller;

import org.hobbit.core.components.AbstractBenchmarkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkController extends AbstractBenchmarkController{
	private static final Logger logger = LoggerFactory.getLogger(BenchmarkController.class);
	
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		super.init();
		logger.debug("Init()");	
	}

	@Override
	protected void executeBenchmark() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
