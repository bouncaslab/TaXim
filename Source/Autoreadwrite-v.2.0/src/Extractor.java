/*  
  Taxim: A Toolchain for Automated and Configurable Simulation for Embedded Multiprocessor Design 
    Copyright (C) 2016  Deniz Candas (dnzcandas@gmail.com), Gorker Alp Malazgirt (gorkeralp@gmail.com), Arda Yurdakul (yurdakul@boun.edu.tr)

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

public class Extractor {
	public static void main(String[] args) throws IOException {

		File fparent = new File(Extractor.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String parent = fparent.getParentFile() + "/";
		System.out.println("Current directory is " + parent);

		File properties = new File(parent + "OutputMetrics");
		String[] memreport = lister(properties, "memreport");
		String[] x86report = lister(properties, "x86report");
		String[] powerResults = lister(properties, "powerresults");
		File[] files = new File(args[0]).listFiles();

		for (File file : files) {
			if (file.getName().equals("ExperimentLogs.txt")) {
				continue;
			}
			String[] topologies = listTopologies(file);
			String res = args[0] + "/" + file.getName();
			compiler(file.toString() + "/", topologies, memreport, x86report, powerResults, res, file.getName());

		}
		System.out.println("Extractor has been fully executed.");
	}

	private static void finder(String[] p, File f, HashMap<String, Double> c) {
		try {
			for (String string : p) {
				Scanner in = new Scanner(f);
				while (true) {
					String a = String.valueOf(in.findInLine(string + " = "));
					if (a != "null") {
						c.put(string, in.nextDouble());
						break;
					}
					try {
						in.nextLine();
					} catch (Exception e) {
						c.put(string, Double.NaN);
						break;
					}
				}
				in.close();
			}
		} catch (FileNotFoundException e) {
			for (String string : p) {
				c.put(string, Double.NaN);
			}
		}
	}

	private static void memFinder(String[] p, File f, String resultdir, String topology, FileWriter fwm,
			String experiments) throws IOException {
		try {
			Scanner in = new Scanner(f);
			fwm.write(topology + ", " + experiments + "\n");
			while (true) {
				for (String string : p) {
					if (String.valueOf(in.findInLine("NoRetry")) != "null") {
						break;
					}
					if (String.valueOf(in.findInLine("mod")) != "null") {
						fwm.write("\nmod" + in.next() + ": ");
					}
					if (String.valueOf(in.findInLine(string + " = ")) != "null") {
						fwm.write(string + " = " + String.valueOf(in.nextDouble()) + ", ");
					}
					if (String.valueOf(in.findInLine("Network.net")) != "null") {
						fwm.write("\n\nWarning: Could not locate all properties!");
						in.close();
						return;
					}
				}
				try {
					in.nextLine();
				} catch (Exception e) {
					fwm.write("\n Warning: Could not locate all properties!");
					in.close();
					break;
				}
			}
		} catch (FileNotFoundException e) {
			fwm.write("memreport not found");
			System.out.println("memreport of " + topology + " under " + experiments + " is missing!");
		}
	}

	private static void iterator(String[] array, FileWriter fw) throws IOException {
		for (String s : array) {
			fw.write(s + ",");
		}
	}

	private static void compiler(String parent, String[] topology, String[] mem, String[] x86, String[] power,
			String res, String experiments) throws IOException {
		File result = new File(res + "_result.csv");
		FileWriter fw = new FileWriter(result);
		fw.write("Topology,Experiment,");

		// iterator(mem, fw);
		iterator(x86, fw);
		iterator(power, fw);
		for (String t : topology) {
			File memresult = new File(res + "/" + t + "/memresult");
			FileWriter fwm = new FileWriter(memresult);
			memFinder(mem, new File(parent + t + "/memreport"), res, t, fwm, experiments);
			fw.write("\n" + t + "," + experiments);
			HashMap<String, Double> cache = new HashMap<String, Double>();
			finder(x86, new File(parent + t + "/x86report"), cache);
			finder(power, new File(parent + t + "/powerResult.txt"), cache);

			for (String s : x86) {
				fw.write("," + cache.get(s));
			}
			for (String s : power) {
				fw.write("," + cache.get(s));
			}
			fwm.close();
		}
		fw.close();
	}

	private static String[] listTopologies(File directory) {
		ArrayList<String> topologies = new ArrayList<String>();
		for (File fileEntry : directory.listFiles()) {
			if (fileEntry.isDirectory()) {
				topologies.add(fileEntry.getName());
			}
		}
		return topologies.toArray(new String[topologies.size()]);
	}

	private static String[] lister(File pros, String string) throws FileNotFoundException {
		Scanner in = new Scanner(pros);
		String sentence = in.nextLine().trim();
		while (true) {
			if (sentence.toLowerCase(Locale.ENGLISH).contains(string)) {
				sentence = sentence.substring(string.length()).replaceAll(" *, *", ",").replaceAll(" *= *", "");
				break;
			}
			try {
				sentence = in.nextLine().trim();
			} catch (Exception e) {
				in.close();
				return new String[0];
			}
		}
		in.close();
		return sentence.split(" *, *");
	}
}
