/**
 * VmImageCoupling - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.main;

import il.ac.technion.beans.Host;
import il.ac.technion.beans.Image;
import il.ac.technion.beans.VM;
import il.ac.technion.configuration.Configuration;
import il.ac.technion.configuration.ConfigurationException;
import il.ac.technion.heuristics.Greedy;
import il.ac.technion.heuristics.LS;
import il.ac.technion.heuristics.SimulationResults;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Main {
	
	/**
	 * 
	 */
	private static Logger logger = Logger.getLogger(Main.class);
	private static final int NUM_ARGS = 1;
	private static final String LINE_DELIM = System.getProperty("line.separator");
	
	public static void main(String[] args) {
		if (args.length != NUM_ARGS) {
			logger.fatal("Invalid arguments");
			return;
		}
		
		if (null == Main.class.getResource(args[0])) {
			logger.fatal("Invalid file");
			return;
		} 
		
		String confFilePath = Main.class.getResource(args[0]).getPath();
		
		Configuration conf = null;
		try {
			conf = new Configuration(confFilePath);
		} catch (IOException e) {
			logger.fatal(e.getMessage());
			return;
		} catch (ConfigurationException e) {
			logger.fatal(e.getMessage());
			return;
		}
		
		SimulationResults sr = runLsAndAssign(conf);
		
		for (Host host : sr.placement) {
			logger.info(host.description());
		}
		logger.info(sr);
		System.out.println(sr.toCsv());
	}
	
	private static SimulationResults runLsAndAssign(Configuration conf) {
		List<Host> hosts = conf.getHosts();
		Map<Image, List<VM>> im2vms = conf.getImageMap();
		LS ls = new LS();
		SimulationResults sr = ls.solve(hosts, new ArrayList<Image>(im2vms.keySet()),im2vms, conf.getId2VmMap(), conf.getImageRedundancy());
		return sr;
	}

	private static SimulationResults runGreedyAndAssign(Configuration conf) {
		List<Host> hosts = conf.getHosts();
		Map<Image, List<VM>> im2vms = conf.getImageMap();
		Greedy greedy = new Greedy();
		SimulationResults sr = greedy.solve(hosts, new ArrayList<Image>(im2vms.keySet()),im2vms, conf.getId2VmMap(), conf.getImageRedundancy());
		return sr;
	}
}
