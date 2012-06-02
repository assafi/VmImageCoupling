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
import il.ac.technion.heuristics.LS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Main {
	
	private static Logger logger = Logger.getLogger(Main.class);
	
	public static void main(String[] args) {
		if (args.length != 1) {
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
		List<Host> hosts = runAndAssign(conf);
		
		int numLocals = 0;
		for (Host host : hosts) {
			logger.info(host.description());
			numLocals += host.numVMs();
		}
		logger.info("Number of local VMs: " + numLocals);
	}
	
	private static List<Host> runAndAssign(Configuration conf) {
		List<Host> hosts = conf.getHosts();
		Map<Image, List<VM>> im2vms = conf.getImageMap();
		LS ls = new LS();
		ls.solve(hosts, new ArrayList<Image>(im2vms.keySet()),im2vms, conf.getId2VmMap());
		return hosts;
	}
	
}
