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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ParameterizedCreator {
	public static void main(String[] args) throws IOException {
		long startTime = System.nanoTime();
		File fparent = new File(ParameterizedCreator.class
				.getProtectionDomain().getCodeSource().getLocation().getPath());
		String parent = fparent.getParentFile() + "/Topologies/";
		new File(parent).mkdirs();
		System.out.println("Current directory is " + fparent.getParentFile()
				+ "/");
		File aList = new File(fparent.getParentFile() + "/" + "TopologyList");
		Scanner lister = new Scanner(aList);
		ArrayList<String> topologies = new ArrayList<String>();
		while (lister.hasNext()) {
			topologies.add(lister.nextLine().trim());
		}
		lister.close();
		int failures = 0;
		for (String topology : topologies) {
			String s1 = "";
			String[] thisTopology = topology.split("_");
			try {
				if (thisTopology[1].contains("DL1")) {
					if (thisTopology[3].contains("DL2")) {
						if (thisTopology[thisTopology.length - 1]
								.contains("BP")) {
							if (thisTopology[4].contains("IL2")) {
								s1 = "Third Level Bypass";
							} else {
								s1 = "Second Level Bypass";
							}
						} else {
							s1 = "Hybrid";
						}
					} else {
						s1 = "Semi-Hybrid";
					}
				} else {
					s1 = "Regular Topology";
				}
			} catch (Exception e) {
				System.out.println(topology + " could not be identified.");
				failures++;
				continue;
			}

			// ////////FIRST VERSION: STANDARD CREATOR//////////
			if (s1.equals("Regular Topology")) {
				try {
					int coreCount = Integer.valueOf(thisTopology[0].substring(
							0, thisTopology[0].length() - 1));
					int l1Count = Integer.valueOf(thisTopology[1].substring(0,
							thisTopology[1].length() - 2));
					int l2Count = Integer.valueOf(thisTopology[2].substring(0,
							thisTopology[2].length() - 2));
					int l3Count;
					try {
						l3Count = Integer.valueOf(thisTopology[3].substring(0,
								thisTopology[3].length() - 2));
					} catch (Exception outofboundsException) {
						l3Count = 0;
					}

					if (coreCount >= l1Count
							&& l1Count >= l2Count
							&& l2Count >= l3Count
							&& l1Count != 0
							&& l2Count != 0
							&& (thisTopology.length == 3 || (thisTopology.length == 4 && thisTopology[3]
									.endsWith("L3")))
							&& thisTopology[0].endsWith("C")
							&& thisTopology[1].endsWith("L1")
							&& thisTopology[2].endsWith("L2")) {
					} else {
						System.out.println(topology + " is an invalid setup.");
						failures++;
						continue;
					}
					File creation = new File(parent + topology);
					FileWriter fw = new FileWriter(creation);
					HT.geometry(fw, l3Count);
					HT.moduleXL1Writer(fw, "l1", "l2", l1Count, l2Count);

					if (l3Count != 0) {

						for (int i = 0; i < l2Count; i++) {
							fw.write("\n[Module mod-l2-" + i + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l2"
									+ "\nHighNetwork = net-l1-l2-" + i
									+ "\nLowModules = mod-l3-" + i % l3Count
									+ "\nLowNetwork = net-l2-l3-" + i % l3Count
									+ "\n");
						}

						for (int i = 0; i < l3Count; i++) {
							fw.write("\n[Module mod-l3-" + i + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l3"
									+ "\nHighNetwork = net-l2-l3-" + i
									+ "\nLowNetwork = net-l3-mm-0"
									+ "\nLowModules = mod-mm" + "\n");
						}
						HT.memWriter(fw, "l3", 0);
						HT.networker(fw, "l3", "mm", 1, 1024, 4096);
						HT.networker(fw, "l2", "l3", l3Count, 1024, 2048);
					}

					if (l3Count == 0) {
						for (int i = 0; i < l2Count; i++) {
							fw.write("\n[Module mod-l2-" + i + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l2"
									+ "\nHighNetwork = net-l1-l2-" + i
									+ "\nLowModules = mod-mm"
									+ "\nLowNetwork = net-l2-mm-0" + "\n");
						}
						HT.memWriter(fw, "l2", 0);
						fw.write("\n[Network net-l2-mm-0]"
								+ "\nDefaultInputBufferSize = 1024"
								+ "\nDefaultOutputBufferSize = 1024"
								+ "\nDefaultBandwidth = 2048" + "\n");
					}

					for (int i = 0; i < l2Count; i++) {
						fw.write("\n[Network net-l1-l2-" + i + "]"
								+ "\nDefaultInputBufferSize = 1024 "
								+ "\nDefaultOutputBufferSize = 1024"
								+ "\nDefaultBandwidth = 1024" + "\n");
					}

					for (int i = 0; i < coreCount; i++) {
						fw.write("\n[Entry core-" + i + "]" + "\nArch = x86"
								+ "\nCore = " + i + "\nThread = 0"
								+ "\nDataModule = mod-l1-" + i % l1Count
								+ "\nInstModule = mod-l1-" + i % l1Count + "\n");
					}
					fw.close();
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(topology + " is an invalid topology!");
					failures++;
					continue;
				}
			}

			// ////////SECOND VERSION: SEMIHYBRID CREATOR//////////
			if (s1.equals("Semi-Hybrid")) {
				try {
					int coreCount = Integer.valueOf(thisTopology[0].substring(
							0, thisTopology[0].length() - 1));
					int dl1Count = Integer.valueOf(thisTopology[1].substring(0,
							thisTopology[1].length() - 3));
					int il1Count = Integer.valueOf(thisTopology[2].substring(0,
							thisTopology[2].length() - 3));
					int l2Count = Integer.valueOf(thisTopology[3].substring(0,
							thisTopology[3].length() - 2));
					int l3Count;
					try {
						l3Count = Integer.valueOf(thisTopology[4].substring(0,
								thisTopology[4].length() - 2));
					} catch (Exception outofboundsException) {
						l3Count = 0;
					}

					if (coreCount >= dl1Count
							&& coreCount >= il1Count
							&& dl1Count >= l2Count
							&& il1Count >= l2Count
							&& l2Count >= l3Count
							&& dl1Count != 0
							&& il1Count != 0
							&& l2Count != 0
							&& (thisTopology.length == 4 || (thisTopology.length == 5 && thisTopology[4]
									.endsWith("L3")))
							&& thisTopology[0].endsWith("C")
							&& thisTopology[1].endsWith("DL1")
							&& thisTopology[2].endsWith("IL1")
							&& thisTopology[3].endsWith("L2")) {
					} else {
						System.out.println(topology + " is an invalid setup.");
						failures++;
						continue;
					}
					File creation = new File(parent + topology);
					FileWriter fw = new FileWriter(creation);

					HT.geometry(fw, l3Count);
					HT.moduleXL1Writer(fw, "dl1", "l2", dl1Count, l2Count);
					HT.moduleXL1Writer(fw, "il1", "l2", il1Count, l2Count);

					if (l3Count != 0) {

						for (int i = 0; i < l2Count; i++) {
							fw.write("\n[Module mod-l2-" + i + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l2"
									+ "\nHighNetwork = net-l1-l2-" + i
									+ "\nLowModules = mod-l3-" + i % l3Count
									+ "\nLowNetwork = net-l2-l3-" + i % l3Count
									+ "\n");
						}

						for (int i = 0; i < l3Count; i++) {
							fw.write("\n[Module mod-l3-" + i + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l3"
									+ "\nHighNetwork = net-l2-l3-" + i
									+ "\nLowNetwork = net-l3-mm-0"
									+ "\nLowModules = mod-mm" + "\n");
						}
						HT.memWriter(fw, "l3", 0);
						fw.write("\n[Network net-l3-mm-0]"
								+ "\nDefaultInputBufferSize = 1024"
								+ "\nDefaultOutputBufferSize = 1024"
								+ "\nDefaultBandwidth = 4096" + "\n");
						for (int i = 0; i < l3Count; i++) {
							fw.write("\n[Network net-l2-l3-" + i + "]"
									+ "\nDefaultInputBufferSize = 1024"
									+ "\nDefaultOutputBufferSize = 1024"
									+ "\nDefaultBandwidth = 2048" + "\n");
						}
					}

					if (l3Count == 0) {
						for (int i = 0; i < l2Count; i++) {
							fw.write("\n[Module mod-l2-" + i + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l2"
									+ "\nHighNetwork = net-l1-l2-" + i
									+ "\nLowModules = mod-mm"
									+ "\nLowNetwork = net-l2-mm-0" + "\n");
						}
						HT.memWriter(fw, "l2", 0);
						fw.write("\n[Network net-l2-mm-0]"
								+ "\nDefaultInputBufferSize = 1024"
								+ "\nDefaultOutputBufferSize = 1024"
								+ "\nDefaultBandwidth = 2048" + "\n");
					}

					for (int i = 0; i < l2Count; i++) {
						fw.write("\n[Network net-l1-l2-" + i + "]"
								+ "\nDefaultInputBufferSize = 1024 "
								+ "\nDefaultOutputBufferSize = 1024"
								+ "\nDefaultBandwidth = 1024" + "\n");
					}

					for (int i = 0; i < coreCount; i++) {
						fw.write("\n[Entry core-" + i + "]" + "\nArch = x86"
								+ "\nCore = " + i + "\nThread = 0"
								+ "\nDataModule = mod-l1-" + 2 * (i % dl1Count)
								+ "\nInstModule = mod-l1-"
								+ (2 * (i % il1Count) + 1) + "\n");
					}
					fw.close();
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(topology + " is an invalid topology!");
					failures++;
					continue;
				}

			}

			// ////////THIRD VERSION: HYBRID CREATOR//////////
			if (s1.equals("Hybrid")) {
				try {
					int coreCount = Integer.valueOf(thisTopology[0].substring(
							0, thisTopology[0].length() - 1));
					int dl1Count = Integer.valueOf(thisTopology[1].substring(0,
							thisTopology[1].length() - 3));
					int il1Count = Integer.valueOf(thisTopology[2].substring(0,
							thisTopology[2].length() - 3));
					int dl2Count = Integer.valueOf(thisTopology[3].substring(0,
							thisTopology[3].length() - 3));
					int il2Count = Integer.valueOf(thisTopology[4].substring(0,
							thisTopology[4].length() - 3));
					int l3Count;
					try {
						l3Count = Integer.valueOf(thisTopology[5].substring(0,
								thisTopology[5].length() - 2));
					} catch (Exception outofboundsException) {
						l3Count = 0;
					}

					if (coreCount >= dl1Count
							&& coreCount >= il1Count
							&& dl1Count >= dl2Count
							&& il1Count >= il2Count
							&& dl2Count >= l3Count
							&& il2Count >= l3Count
							&& dl1Count != 0
							&& il1Count != 0
							&& dl2Count != 0
							&& il2Count != 0
							&& (thisTopology.length == 5 || (thisTopology.length == 6 && thisTopology[5]
									.endsWith("L3")))
							&& thisTopology[0].endsWith("C")
							&& thisTopology[1].endsWith("DL1")
							&& thisTopology[2].endsWith("IL1")
							&& thisTopology[3].endsWith("DL2")
							&& thisTopology[4].endsWith("IL2")) {
					} else {
						System.out.println(topology + " is an invalid setup.");
						failures++;
						continue;
					}
					File creation = new File(parent + topology);
					FileWriter fw = new FileWriter(creation);

					HT.geometry(fw, l3Count);
					HT.moduleXL1Writer(fw, "dl1", "dl2", dl1Count, dl2Count);
					HT.moduleXL1Writer(fw, "il1", "il2", il1Count, il2Count);

					if (l3Count != 0) {

						for (int i = 0; i < dl2Count; i++) {
							fw.write("\n[Module mod-l2-" + 2 * i + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l2"
									+ "\nHighNetwork = net-l1-l2-" + 2 * i
									+ "\nLowModules = mod-l3-" + i % l3Count
									+ "\nLowNetwork = net-l2-l3-" + i % l3Count
									+ "\n");
						}

						for (int i = 0; i < il2Count; i++) {
							fw.write("\n[Module mod-l2-" + ((2 * i) + 1) + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l2"
									+ "\nHighNetwork = net-l1-l2-"
									+ ((2 * i) + 1) + "\nLowModules = mod-l3-"
									+ i % l3Count + "\nLowNetwork = net-l2-l3-"
									+ i % l3Count + "\n");
						}

						for (int i = 0; i < l3Count; i++) {
							fw.write("\n[Module mod-l3-" + i + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l3"
									+ "\nHighNetwork = net-l2-l3-" + i
									+ "\nLowNetwork = net-l3-mm-0"
									+ "\nLowModules = mod-mm" + "\n");
						}
						HT.memWriter(fw, "l3", 0);
						HT.networker(fw, "l3", "mm", 1, 1024, 4096);
						HT.networker(fw, "l2", "l3", l3Count, 1024, 2048);
					}

					if (l3Count == 0) {

						for (int i = 0; i < dl2Count; i++) {
							fw.write("\n[Module mod-l2-" + 2 * i + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l2"
									+ "\nHighNetwork = net-l1-l2-" + 2 * i
									+ "\nLowModules = mod-mm"
									+ "\nLowNetwork = net-l2-mm-0" + "\n");
						}

						for (int i = 0; i < il2Count; i++) {
							fw.write("\n[Module mod-l2-" + ((2 * i) + 1) + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l2"
									+ "\nHighNetwork = net-l1-l2-"
									+ ((2 * i) + 1) + "\nLowModules = mod-mm"
									+ "\nLowNetwork = net-l2-mm-0" + "\n");
						}
						HT.memWriter(fw, "l2", 0);
						HT.networker(fw, "l2", "mm", 1, 1024, 2048);
					}

					for (int i = 0; i < dl2Count; i++) {
						fw.write("\n[Network net-l1-l2-" + 2 * i + "]"
								+ "\nDefaultInputBufferSize = 1024 "
								+ "\nDefaultOutputBufferSize = 1024"
								+ "\nDefaultBandwidth = 1024" + "\n");
					}

					for (int i = 0; i < il2Count; i++) {
						fw.write("\n[Network net-l1-l2-" + ((2 * i) + 1) + "]"
								+ "\nDefaultInputBufferSize = 1024 "
								+ "\nDefaultOutputBufferSize = 1024"
								+ "\nDefaultBandwidth = 1024" + "\n");
					}

					for (int i = 0; i < coreCount; i++) {
						fw.write("\n[Entry core-" + i + "]" + "\nArch = x86"
								+ "\nCore = " + i + "\nThread = 0"
								+ "\nDataModule = mod-l1-" + 2 * (i % dl1Count)
								+ "\nInstModule = mod-l1-"
								+ (2 * (i % il1Count) + 1) + "\n");
					}
					fw.close();
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(topology + " is an invalid topology!");
					failures++;
					continue;
				}
			}

			// ////////FOURTH VERSION: 2ND LEVEL INSTRUCTION BYPASS//////////
			if (s1.equals("Second Level Bypass")) {
				try {
					int coreCount = Integer.valueOf(thisTopology[0].substring(
							0, thisTopology[0].length() - 1));
					int dl1Count = Integer.valueOf(thisTopology[1].substring(0,
							thisTopology[1].length() - 3));
					int il1Count = Integer.valueOf(thisTopology[2].substring(0,
							thisTopology[2].length() - 3));
					int dl2Count = Integer.valueOf(thisTopology[3].substring(0,
							thisTopology[3].length() - 3));
					int l3Count;
					try {
						l3Count = Integer.valueOf(thisTopology[4].substring(0,
								thisTopology[4].length() - 2));
					} catch (Exception outofboundsException) {
						l3Count = 0;
					}

					if (coreCount >= dl1Count
							&& coreCount >= il1Count
							&& dl1Count >= dl2Count
							&& dl2Count >= l3Count
							&& il1Count >= l3Count
							&& dl1Count != 0
							&& il1Count != 0
							&& dl2Count != 0
							&& ((thisTopology.length == 5 && thisTopology[4]
									.equals("BP")) || (thisTopology.length == 6 && thisTopology[4]
									.endsWith("L3"))
									&& thisTopology[5].equals("BP"))
							&& thisTopology[0].endsWith("C")
							&& thisTopology[1].endsWith("DL1")
							&& thisTopology[2].endsWith("IL1")
							&& thisTopology[3].endsWith("DL2")) {
					} else {
						System.out.println(topology + " is an invalid setup.");
						failures++;
						continue;
					}
					File creation = new File(parent + topology);
					FileWriter fw = new FileWriter(creation);

					HT.geometry(fw, l3Count);
					HT.moduleXL1Writer(fw, "dl1", "dl2", dl1Count, dl2Count);

					if (l3Count != 0) {
						HT.moduleXL1Writer(fw, "il1", "l3", il1Count, l3Count);

						for (int i = 0; i < dl2Count; i++) {
							fw.write("\n[Module mod-l2-" + 2 * i + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l2"
									+ "\nHighNetwork = net-l1-l2-" + 2 * i
									+ "\nLowModules = mod-l3-" + i % l3Count
									+ "\nLowNetwork = net-" + i % l3Count
									+ "\nLowNetworkNode = n" + 2 * i + "\n");
						}

						for (int i = 0; i < l3Count; i++) {
							fw.write("\n[Module mod-l3-" + i + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l3"
									+ "\nHighNetwork = net-" + i
									+ "\nHighNetworkNode = n"
									+ (2 * (il1Count + dl2Count) + i)
									+ "\nLowNetwork = net-l3-mm-0"
									+ "\nLowModules = mod-mm" + "\n");
						}
						HT.memWriter(fw, "l3", 0);
						fw.write("\n[Network net-l3-mm-0]"
								+ "\nDefaultInputBufferSize = 1024"
								+ "\nDefaultOutputBufferSize = 1024"
								+ "\nDefaultBandwidth = 4096" + "\n");
					}

					if (l3Count == 0) {
						HT.moduleXL1Writer(fw, "il1", "mm", il1Count, 0);

						for (int i = 0; i < dl2Count; i++) {
							fw.write("\n[Module mod-l2-" + 2 * i + "]"
									+ "\nType = Cache" + "\nGeometry = geo-l2"
									+ "\nHighNetwork = net-l1-l2-" + 2 * i
									+ "\nLowModules = mod-mm"
									+ "\nLowNetwork = net-0"
									+ "\nLowNetworkNode = n" + 2 * i + "\n");
						}
						HT.memWriter(fw, "0", 2 * (il1Count + dl2Count));
					}

					for (int i = 0; i < dl2Count; i++) {
						fw.write("\n[Network net-l1-l2-" + 2 * i + "]"
								+ "\nDefaultInputBufferSize = 1024 "
								+ "\nDefaultOutputBufferSize = 1024"
								+ "\nDefaultBandwidth = 1024" + "\n");
					}

					for (int i = 0; i < coreCount; i++) {
						fw.write("\n[Entry core-" + i + "]" + "\nArch = x86"
								+ "\nCore = " + i + "\nThread = 0"
								+ "\nDataModule = mod-l1-" + 2 * (i % dl1Count)
								+ "\nInstModule = mod-l1-"
								+ (2 * (i % il1Count) + 1) + "\n");
					}
					if (l3Count > 1) {
						File network = new File(parent + topology + "_net");
						FileWriter fwn = new FileWriter(network);
						
						for (int n = 0; n < l3Count; n++){
						fwn.write("\n[Network.net-" + n + "]"
								+ "\nDefaultInputBufferSize = 512"
								+ "\nDefaultOutputBufferSize = 512"
								+ "\nDefaultBandwidth = 8" + "\n");
						

						for (int i = 2; (dl2Count + l3Count - n - 1) / l3Count / i > 0; i = i << 1) {
							for (int j = 0; j < (dl2Count + l3Count - n - 1) / l3Count / i; j++) {
								fwn.write("\n[Network.net-" + n + ".Node.sw");
								for (int k = 2; k < i; k = k << 1) {
									fwn.write("0");
								}
								fwn.write(2 * (j * l3Count + n) + "]" + "\nType = Switch" + "\n");
							}
						}

						for (int i = 2; (il1Count + l3Count - n - 1) / l3Count / i > 0; i = i << 1) {
							for (int j = 0; j < (il1Count + l3Count - n - 1) / l3Count / i; j++) {
								fwn.write("\n[Network.net-" + n + ".Node.sw");
								for (int k = 2; k < i; k = k << 1) {
									fwn.write("0");
								}
								fwn.write(((2 * (j * l3Count + n)) + 1) + "]" + "\nType = Switch"
										+ "\n");
							}
						}

						fwn.write("\n[Network.net-" + n + ".Node.sw" + (2
								* (il1Count + dl2Count) + n) + "]" + "\nType = Switch"
								+ "\n");

						for (int i = 0; i < (il1Count + l3Count - n - 1) / l3Count; i++) {
							fwn.write("\n[Network.net-" + n + ".Node.n" + ((2 * (i * l3Count + n)) + 1)
									+ "]" + "\nType = EndNode" + "\n");
						}

						for (int i = 0; i < (dl2Count + l3Count - n - 1) / l3Count; i++) {
							fwn.write("\n[Network.net-" + n + ".Node.n" + (2 * (i * l3Count + n)) + "]"
									+ "\nType = EndNode" + "\n");
						}

						fwn.write("\n[Network.net-" + n + ".Node.n" + (2
								* (il1Count + dl2Count) + n) + "]" + "\nType = EndNode"
								+ "\n");
						
						fwn.write("\n[Network.net-" + n + ".Link.n" + (2
								* (il1Count + dl2Count) + n) + "-sw" + (2
								* (il1Count + dl2Count) + n) + "]" + "\nSource = n" + (2
								* (il1Count + dl2Count) + n) + "\nDest = sw" + (2
								* (il1Count + dl2Count) + n) + "\nType = Bidirectional"
								+ "\n");
						
						String s = "";
						String d = String.valueOf(2 * n + 1);

						StringBuilder sb = new StringBuilder();
						if ((il1Count + l3Count - n - 1) / l3Count == 1) {
							fwn.write("\n[Network.net-" + n + ".Link.n" + (2 * n + 1) + "-sw"
									+ (2 * (il1Count + dl2Count) + n) + "]"
									+ "\nSource = n" + (2 * n + 1) + "\nDest = sw"
									+ (2 * (il1Count + dl2Count) + n)
									+ "\nType = Bidirectional" + "\n");
						}
						if ((il1Count + l3Count - n - 1) / l3Count != 1) {
							sb.delete(0, sb.length());
							for (int k = 0; k < ((il1Count + l3Count - n - 1) / l3Count / 2) * 2; k++) {
								fwn.write("\n[Network.net-" + n + ".Link.n" + (2 * (k * l3Count + n) + 1)
										+ "-sw" + (2 * (k / 2 * l3Count + n) + 1) + "]" + "\nSource = n"
										+ (2 * (k * l3Count + n) + 1) + "\nDest = sw" + (2 * ((k / 2 * l3Count + n)) + 1)
										+ "\nType = Bidirectional" + "\n");
							}
							if ((il1Count + l3Count - n - 1) / l3Count % 2 == 1){
								fwn.write("\n[Network.net-" + n + ".Link.n" + (2 * ((((il1Count + l3Count - n - 1) / l3Count / 2) * 2) * l3Count + n) + 1)
										+ "-sw" + (2 * ((((il1Count + l3Count - n - 1) / l3Count / 2) * 2 - 1) / 2 * l3Count + n) + 1) + "]" + "\nSource = n"
										+ (2 * ((((il1Count + l3Count - n - 1) / l3Count / 2) * 2) * l3Count + n) + 1) + "\nDest = sw" + (2 * ((((il1Count + l3Count - n - 1) / l3Count / 2) * 2 - 1) / 2 * l3Count + n) + 1)
										+ "\nType = Bidirectional" + "\n");
							}
							for (int j = 2; (il1Count + l3Count - n - 1) / l3Count / j > 1; j = j << 1) {
								String j0 = sb.toString();
								if ((il1Count + l3Count - n - 1) / l3Count / j % 2 == 0) {
									for (int k = 0; k < (il1Count + l3Count - n - 1) / l3Count / j; k++) {
										s = j0 + (2 * (k * l3Count + n) + 1);
										d = 0 + j0 + (2 * (k / 2 * l3Count + n) + 1);
										fwn.write("\n[Network.net-" + n + ".Link.sw" + s
												+ "-sw" + d + "]" + "\nSource = sw"
												+ s + "\nDest = sw" + d
												+ "\nType = Bidirectional" + "\n");
									}
								}
								if ((il1Count + l3Count - n - 1) / l3Count / j % 2 == 1) {
									int k;
									for (k = 0; k < ((il1Count + l3Count - n - 1) / l3Count / j) - 1; k++) {
										s = j0 + (2 * (k * l3Count + n) + 1);
										d = 0 + j0 + (2 * (k / 2 * l3Count + n) + 1);
										fwn.write("\n[Network.net-" + n + ".Link.sw" + s
												+ "-sw" + d + "]" + "\nSource = sw"
												+ s + "\nDest = sw" + d
												+ "\nType = Bidirectional" + "\n");
									}
									fwn.write("\n[Network.net-" + n + ".Link.sw" + j0
											+ (2 * (k * l3Count + n) + 1) + "-sw" + d + "]"
											+ "\nSource = sw" + j0 + (2 * (k * l3Count + n) + 1)
											+ "\nDest = sw" + d
											+ "\nType = Bidirectional" + "\n");
								}
								sb.append("0");
							}
							fwn.write("\n[Network.net-" + n + ".Link.sw" + d + "-sw"
									+ (2 * (il1Count + dl2Count) + n) + "]"
									+ "\nSource = sw" + d + "\nDest = sw"
									+ (2 * (il1Count + dl2Count) + n)
									+ "\nType = Bidirectional" + "\n");
						}

						s = "";
						d = String.valueOf(2 * n);
						if ((dl2Count + l3Count - n - 1) / l3Count == 1) {
							fwn.write("\n[Network.net-" + n + ".Link.n" + (2 * n) + "-sw"
									+ (2 * (il1Count + dl2Count) + n) + "]"
									+ "\nSource = n" + (2 * n) + "\nDest = sw"
									+ (2 * (il1Count + dl2Count) + n)
									+ "\nType = Bidirectional" + "\n");
						}
						if ((dl2Count + l3Count - n - 1) / l3Count != 1) {
							sb.delete(0, sb.length());
							for (int k = 0; k < ((dl2Count + l3Count - n - 1) / l3Count / 2) * 2; k++) {
								fwn.write("\n[Network.net-" + n + ".Link.n" + 2 * (k * l3Count + n)
										+ "-sw" + 2 * (k / 2 * l3Count + n) + "]" + "\nSource = n"
										+ 2 * (k * l3Count + n) + "\nDest = sw" + 2 * (k / 2 * l3Count + n)
										+ "\nType = Bidirectional" + "\n");
							}
							if ((dl2Count + l3Count - n - 1) / l3Count % 2 == 1){
								fwn.write("\n[Network.net-" + n + ".Link.n" + (2 * ((((dl2Count + l3Count - n - 1) / l3Count / 2) * 2) * l3Count + n))
										+ "-sw" + (2 * ((((dl2Count + l3Count - n - 1) / l3Count / 2) * 2 - 1) / 2 * l3Count + n)) + "]" + "\nSource = n"
										+ (2 * ((((dl2Count + l3Count - n - 1) / l3Count / 2) * 2) * l3Count + n)) + "\nDest = sw" + (2 * ((((dl2Count + l3Count - n - 1) / l3Count / 2) * 2 - 1) / 2 * l3Count + n))
										+ "\nType = Bidirectional" + "\n");
							}
							for (int j = 2; (dl2Count + l3Count - n - 1) / l3Count / j > 1; j = j << 1) {
								String j0 = sb.toString();
								if ((dl2Count + l3Count - n - 1) / l3Count / j % 2 == 0) {
									for (int k = 0; k < (dl2Count + l3Count - n - 1) / l3Count / j; k++) {
										s = j0 + 2 * (k * l3Count + n);
										d = 0 + j0 + 2 * (k / 2 * l3Count + n);
										fwn.write("\n[Network.net-" + n + ".Link.sw" + s
												+ "-sw" + d + "]" + "\nSource = sw"
												+ s + "\nDest = sw" + d
												+ "\nType = Bidirectional" + "\n");
									}
								}
								if ((dl2Count + l3Count - n - 1) / l3Count / j % 2 == 1) {
									int k;
									for (k = 0; k < ((dl2Count + l3Count - n - 1) / l3Count / j) - 1; k++) {
										s = j0 + 2 * (k * l3Count + n);
										d = 0 + j0 + 2 * (k / 2 * l3Count + n);
										fwn.write("\n[Network.net-" + n + ".Link.sw" + s
												+ "-sw" + d + "]" + "\nSource = sw"
												+ s + "\nDest = sw" + d
												+ "\nType = Bidirectional" + "\n");
									}
									fwn.write("\n[Network.net-" + n + ".Link.sw" + j0
											+ 2 * (k * l3Count + n) + "-sw" + d + "]"
											+ "\nSource = sw" + j0 + 2 * (k * l3Count + n)
											+ "\nDest = sw" + d
											+ "\nType = Bidirectional" + "\n");
								}
								sb.append("0");
							}
							fwn.write("\n[Network.net-" + n + ".Link.sw" + d + "-sw"
									+ (2 * (il1Count + dl2Count) + n) + "]"
									+ "\nSource = sw" + d + "\nDest = sw"
									+ (2 * (il1Count + dl2Count) + n)
									+ "\nType = Bidirectional" + "\n");
						}
						}
						fwn.close();
					}
					if (l3Count == 0) {
						File network = new File(parent + topology + "_net");
						FileWriter fwn = new FileWriter(network);

						fwn.write("\n[Network.net-0]"
								+ "\nDefaultInputBufferSize = 512"
								+ "\nDefaultOutputBufferSize = 512"
								+ "\nDefaultBandwidth = 8" + "\n");

						for (int i = 2; dl2Count / i > 0; i = i << 1) {
							for (int j = 0; j < dl2Count / i; j++) {
								fwn.write("\n[Network.net-0.Node.sw");
								for (int k = 2; k < i; k = k << 1) {
									fwn.write("0");
								}
								fwn.write(2 * j + "]" + "\nType = Switch" + "\n");
							}
						}

						for (int i = 2; il1Count / i > 0; i = i << 1) {
							for (int j = 0; j < il1Count / i; j++) {
								fwn.write("\n[Network.net-0.Node.sw");
								for (int k = 2; k < i; k = k << 1) {
									fwn.write("0");
								}
								fwn.write(((2 * j) + 1) + "]" + "\nType = Switch"
										+ "\n");
							}
						}

						fwn.write("\n[Network.net-0.Node.sw" + 2
								* (il1Count + dl2Count) + "]" + "\nType = Switch"
								+ "\n");

						for (int i = 0; i < il1Count; i++) {
							fwn.write("\n[Network.net-0.Node.n" + ((2 * i) + 1)
									+ "]" + "\nType = EndNode" + "\n");
						}

						for (int i = 0; i < dl2Count; i++) {
							fwn.write("\n[Network.net-0.Node.n" + (2 * i) + "]"
									+ "\nType = EndNode" + "\n");
						}

						fwn.write("\n[Network.net-0.Node.n" + 2
								* (il1Count + dl2Count) + "]" + "\nType = EndNode"
								+ "\n");
						
						fwn.write("\n[Network.net-0.Link.n" + 2
								* (il1Count + dl2Count) + "-sw" + 2
								* (il1Count + dl2Count) + "]" + "\nSource = n" + 2
								* (il1Count + dl2Count) + "\nDest = sw" + 2
								* (il1Count + dl2Count) + "\nType = Bidirectional"
								+ "\n");
						
						String s = "";
						String d = "1";

						StringBuilder sb = new StringBuilder();
						if (il1Count == 1) {
							fwn.write("\n[Network.net-0.Link.n" + 1 + "-sw"
									+ (2 * (il1Count + dl2Count)) + "]"
									+ "\nSource = n" + 1 + "\nDest = sw"
									+ (2 * (il1Count + dl2Count))
									+ "\nType = Bidirectional" + "\n");
						}
						if (il1Count != 1) {
							sb.delete(0, sb.length());
							for (int k = 0; k < (il1Count / 2) * 2; k++) {
								fwn.write("\n[Network.net-0.Link.n" + (2 * k + 1)
										+ "-sw" + (2 * (k / 2) + 1) + "]" + "\nSource = n"
										+ (2 * k + 1) + "\nDest = sw" + (2 * (k / 2) + 1)
										+ "\nType = Bidirectional" + "\n");
							}
							if (il1Count % 2 == 1){
								fwn.write("\n[Network.net-0.Link.n" + (2 * il1Count - 1)
										+ "-sw" + (2 * (il1Count / 2) - 1) + "]" + "\nSource = n"
										+ (2 * il1Count - 1) + "\nDest = sw" + (2 * (il1Count / 2) - 1)
										+ "\nType = Bidirectional" + "\n");
							}
							for (int j = 2; il1Count / j > 1; j = j << 1) {
								String j0 = sb.toString();
								if (il1Count / j % 2 == 0) {
									for (int k = 0; k < il1Count / j; k++) {
										s = j0 + (2 * k + 1);
										d = 0 + j0 + (2 * (k / 2) + 1);
										fwn.write("\n[Network.net-0.Link.sw" + s
												+ "-sw" + d + "]" + "\nSource = sw"
												+ s + "\nDest = sw" + d
												+ "\nType = Bidirectional" + "\n");
									}
								}
								if (il1Count / j % 2 == 1) {
									int k;
									for (k = 0; k < (il1Count / j) - 1; k++) {
										s = j0 + (2 * k + 1);
										d = 0 + j0 + (2 * (k / 2) + 1);
										fwn.write("\n[Network.net-0.Link.sw" + s
												+ "-sw" + d + "]" + "\nSource = sw"
												+ s + "\nDest = sw" + d
												+ "\nType = Bidirectional" + "\n");
									}
									fwn.write("\n[Network.net-0.Link.sw" + j0
											+ (2 * k + 1) + "-sw" + d + "]"
											+ "\nSource = sw" + j0 + (2 * k + 1)
											+ "\nDest = sw" + d
											+ "\nType = Bidirectional" + "\n");
								}
								sb.append("0");
							}
							fwn.write("\n[Network.net-0.Link.sw" + d + "-sw"
									+ (2 * (il1Count + dl2Count)) + "]"
									+ "\nSource = sw" + d + "\nDest = sw"
									+ (2 * (il1Count + dl2Count))
									+ "\nType = Bidirectional" + "\n");
						}

						s = "";
						d = "0";
						if (dl2Count == 1) {
							fwn.write("\n[Network.net-0.Link.n" + 0 + "-sw"
									+ (2 * (il1Count + dl2Count)) + "]"
									+ "\nSource = n" + 0 + "\nDest = sw"
									+ (2 * (il1Count + dl2Count))
									+ "\nType = Bidirectional" + "\n");
						}
						if (dl2Count != 1) {
							sb.delete(0, sb.length());
							for (int k = 0; k < (dl2Count / 2) * 2; k++) {
								fwn.write("\n[Network.net-0.Link.n" + 2 * k
										+ "-sw" + 2 *(k / 2)+ "]" + "\nSource = n"
										+ 2 * k + "\nDest = sw" + 2 * (k / 2)
										+ "\nType = Bidirectional" + "\n");
							}
							if (dl2Count % 2 == 1){
								fwn.write("\n[Network.net-0.Link.n" + (2 * dl2Count - 2)
										+ "-sw" + (2 * (dl2Count / 2) - 2) + "]" + "\nSource = n"
										+ (2 * dl2Count - 2) + "\nDest = sw" + (2 * (dl2Count / 2) - 2)
										+ "\nType = Bidirectional" + "\n");
							}
							for (int j = 2; dl2Count / j > 1; j = j << 1) {
								String j0 = sb.toString();
								if (dl2Count / j % 2 == 0) {
									for (int k = 0; k < dl2Count / j; k++) {
										s = j0 + (2 * k);
										d = 0 + j0 + (2 * (k / 2));
										fwn.write("\n[Network.net-0.Link.sw" + s
												+ "-sw" + d + "]" + "\nSource = sw"
												+ s + "\nDest = sw" + d
												+ "\nType = Bidirectional" + "\n");
									}
								}
								if (dl2Count / j % 2 == 1) {
									int k;
									for (k = 0; k < (dl2Count / j) - 1; k++) {
										s = j0 + (2 * k);
										d = 0 + j0 + (2 * (k / 2));
										fwn.write("\n[Network.net-0.Link.sw" + s
												+ "-sw" + d + "]" + "\nSource = sw"
												+ s + "\nDest = sw" + d
												+ "\nType = Bidirectional" + "\n");
									}
									fwn.write("\n[Network.net-0.Link.sw" + j0
											+ (2 * k) + "-sw" + d + "]"
											+ "\nSource = sw" + j0 + (2 * k)
											+ "\nDest = sw" + d
											+ "\nType = Bidirectional" + "\n");
								}
								sb.append("0");
							}
							fwn.write("\n[Network.net-0.Link.sw" + d + "-sw"
									+ (2 * (il1Count + dl2Count)) + "]"
									+ "\nSource = sw" + d + "\nDest = sw"
									+ (2 * (il1Count + dl2Count))
									+ "\nType = Bidirectional" + "\n");
						}
						fwn.close();
					}
					if (l3Count == 1) {
						File network = new File(parent + topology + "_net");
						FileWriter fwn = new FileWriter(network);

						fwn.write("\n[Network.net-0]"
								+ "\nDefaultInputBufferSize = 512"
								+ "\nDefaultOutputBufferSize = 512"
								+ "\nDefaultBandwidth = 8" + "\n");

						for (int i = 2; dl2Count / i > 0; i = i << 1) {
							for (int j = 0; j < dl2Count / i; j++) {
								fwn.write("\n[Network.net-0.Node.sw");
								for (int k = 2; k < i; k = k << 1) {
									fwn.write("0");
								}
								fwn.write(2 * j + "]" + "\nType = Switch" + "\n");
							}
						}

						for (int i = 2; il1Count / i > 0; i = i << 1) {
							for (int j = 0; j < il1Count / i; j++) {
								fwn.write("\n[Network.net-0.Node.sw");
								for (int k = 2; k < i; k = k << 1) {
									fwn.write("0");
								}
								fwn.write(((2 * j) + 1) + "]" + "\nType = Switch"
										+ "\n");
							}
						}

						fwn.write("\n[Network.net-0.Node.sw" + 2
								* (il1Count + dl2Count) + "]" + "\nType = Switch"
								+ "\n");

						for (int i = 0; i < il1Count; i++) {
							fwn.write("\n[Network.net-0.Node.n" + ((2 * i) + 1)
									+ "]" + "\nType = EndNode" + "\n");
						}

						for (int i = 0; i < dl2Count; i++) {
							fwn.write("\n[Network.net-0.Node.n" + (2 * i) + "]"
									+ "\nType = EndNode" + "\n");
						}

						fwn.write("\n[Network.net-0.Node.n" + 2
								* (il1Count + dl2Count) + "]" + "\nType = EndNode"
								+ "\n");
						
						fwn.write("\n[Network.net-0.Link.n" + 2
								* (il1Count + dl2Count) + "-sw" + 2
								* (il1Count + dl2Count) + "]" + "\nSource = n" + 2
								* (il1Count + dl2Count) + "\nDest = sw" + 2
								* (il1Count + dl2Count) + "\nType = Bidirectional"
								+ "\n");
						
						String s = "";
						String d = "1";

						StringBuilder sb = new StringBuilder();
						if (il1Count == 1) {
							fwn.write("\n[Network.net-0.Link.n" + 1 + "-sw"
									+ (2 * (il1Count + dl2Count)) + "]"
									+ "\nSource = n" + 1 + "\nDest = sw"
									+ (2 * (il1Count + dl2Count))
									+ "\nType = Bidirectional" + "\n");
						}
						if (il1Count != 1) {
							sb.delete(0, sb.length());
							for (int k = 0; k < (il1Count / 2) * 2; k++) {
								fwn.write("\n[Network.net-0.Link.n" + (2 * k + 1)
										+ "-sw" + (2 * (k / 2) + 1) + "]" + "\nSource = n"
										+ (2 * k + 1) + "\nDest = sw" + (2 * (k / 2) + 1)
										+ "\nType = Bidirectional" + "\n");
							}
							if (il1Count % 2 == 1){
								fwn.write("\n[Network.net-0.Link.n" + (2 * il1Count - 1)
										+ "-sw" + (2 * (il1Count / 2) - 1) + "]" + "\nSource = n"
										+ (2 * il1Count - 1) + "\nDest = sw" + (2 * (il1Count / 2) - 1)
										+ "\nType = Bidirectional" + "\n");
							}
							for (int j = 2; il1Count / j > 1; j = j << 1) {
								String j0 = sb.toString();
								if (il1Count / j % 2 == 0) {
									for (int k = 0; k < il1Count / j; k++) {
										s = j0 + (2 * k + 1);
										d = 0 + j0 + (2 * (k / 2) + 1);
										fwn.write("\n[Network.net-0.Link.sw" + s
												+ "-sw" + d + "]" + "\nSource = sw"
												+ s + "\nDest = sw" + d
												+ "\nType = Bidirectional" + "\n");
									}
								}
								if (il1Count / j % 2 == 1) {
									int k;
									for (k = 0; k < (il1Count / j) - 1; k++) {
										s = j0 + (2 * k + 1);
										d = 0 + j0 + (2 * (k / 2) + 1);
										fwn.write("\n[Network.net-0.Link.sw" + s
												+ "-sw" + d + "]" + "\nSource = sw"
												+ s + "\nDest = sw" + d
												+ "\nType = Bidirectional" + "\n");
									}
									fwn.write("\n[Network.net-0.Link.sw" + j0
											+ (2 * k + 1) + "-sw" + d + "]"
											+ "\nSource = sw" + j0 + (2 * k + 1)
											+ "\nDest = sw" + d
											+ "\nType = Bidirectional" + "\n");
								}
								sb.append("0");
							}
							fwn.write("\n[Network.net-0.Link.sw" + d + "-sw"
									+ (2 * (il1Count + dl2Count)) + "]"
									+ "\nSource = sw" + d + "\nDest = sw"
									+ (2 * (il1Count + dl2Count))
									+ "\nType = Bidirectional" + "\n");
						}

						s = "";
						d = "0";
						if (dl2Count == 1) {
							fwn.write("\n[Network.net-0.Link.n" + 0 + "-sw"
									+ (2 * (il1Count + dl2Count)) + "]"
									+ "\nSource = n" + 0 + "\nDest = sw"
									+ (2 * (il1Count + dl2Count))
									+ "\nType = Bidirectional" + "\n");
						}
						if (dl2Count != 1) {
							sb.delete(0, sb.length());
							for (int k = 0; k < (dl2Count / 2) * 2; k++) {
								fwn.write("\n[Network.net-0.Link.n" + 2 * k
										+ "-sw" + 2 *(k / 2)+ "]" + "\nSource = n"
										+ 2 * k + "\nDest = sw" + 2 * (k / 2)
										+ "\nType = Bidirectional" + "\n");
							}
							if (dl2Count % 2 == 1){
								fwn.write("\n[Network.net-0.Link.n" + (2 * dl2Count - 2)
										+ "-sw" + (2 * (dl2Count / 2) - 2) + "]" + "\nSource = n"
										+ (2 * dl2Count - 2) + "\nDest = sw" + (2 * (dl2Count / 2) - 2)
										+ "\nType = Bidirectional" + "\n");
							}
							for (int j = 2; dl2Count / j > 1; j = j << 1) {
								String j0 = sb.toString();
								if (dl2Count / j % 2 == 0) {
									for (int k = 0; k < dl2Count / j; k++) {
										s = j0 + (2 * k);
										d = 0 + j0 + (2 * (k / 2));
										fwn.write("\n[Network.net-0.Link.sw" + s
												+ "-sw" + d + "]" + "\nSource = sw"
												+ s + "\nDest = sw" + d
												+ "\nType = Bidirectional" + "\n");
									}
								}
								if (dl2Count / j % 2 == 1) {
									int k;
									for (k = 0; k < (dl2Count / j) - 1; k++) {
										s = j0 + (2 * k);
										d = 0 + j0 + (2 * (k / 2));
										fwn.write("\n[Network.net-0.Link.sw" + s
												+ "-sw" + d + "]" + "\nSource = sw"
												+ s + "\nDest = sw" + d
												+ "\nType = Bidirectional" + "\n");
									}
									fwn.write("\n[Network.net-0.Link.sw" + j0
											+ (2 * k) + "-sw" + d + "]"
											+ "\nSource = sw" + j0 + (2 * k)
											+ "\nDest = sw" + d
											+ "\nType = Bidirectional" + "\n");
								}
								sb.append("0");
							}
							fwn.write("\n[Network.net-0.Link.sw" + d + "-sw"
									+ (2 * (il1Count + dl2Count)) + "]"
									+ "\nSource = sw" + d + "\nDest = sw"
									+ (2 * (il1Count + dl2Count))
									+ "\nType = Bidirectional" + "\n");
						}
						fwn.close();
					}
					fw.close();
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(topology + " is an invalid topology!");
					failures++;
					continue;
				}
			}
			// ////////FIFTH VERSION: 3RD LEVEL INSTRUCTION BYPASS//////////
			if (s1.equals("Third Level Bypass")) {
				try {
					int coreCount = Integer.valueOf(thisTopology[0].substring(
							0, thisTopology[0].length() - 1));
					int dl1Count = Integer.valueOf(thisTopology[1].substring(0,
							thisTopology[1].length() - 3));
					int il1Count = Integer.valueOf(thisTopology[2].substring(0,
							thisTopology[2].length() - 3));
					int dl2Count = Integer.valueOf(thisTopology[3].substring(0,
							thisTopology[3].length() - 3));
					int il2Count = Integer.valueOf(thisTopology[4].substring(0,
							thisTopology[4].length() - 3));
					int l3Count;
					try {
						l3Count = Integer.valueOf(thisTopology[5].substring(0,
								thisTopology[5].length() - 2));
					} catch (Exception outofboundsException) {
						l3Count = 0;
					}

					if (coreCount >= dl1Count
							&& coreCount >= il1Count
							&& dl1Count >= dl2Count
							&& il1Count >= il2Count
							&& dl2Count >= l3Count
							&& il2Count >= l3Count
							&& dl1Count != 0
							&& il1Count != 0
							&& dl2Count != 0
							&& il2Count != 0
							&& l3Count != 0
							&& ((thisTopology.length == 6 && thisTopology[5]
									.equals("BP")) || (thisTopology.length == 7 && thisTopology[5]
									.endsWith("L3"))
									&& thisTopology[6].equals("BP"))
							&& thisTopology[0].endsWith("C")
							&& thisTopology[1].endsWith("DL1")
							&& thisTopology[2].endsWith("IL1")
							&& thisTopology[3].endsWith("DL2")
							&& thisTopology[4].endsWith("IL2")) {
					} else {
						System.out.println(topology + " is an invalid setup.");
						failures++;
						continue;
					}
					File creation = new File(parent + topology);
					FileWriter fw = new FileWriter(creation);

					HT.geometry(fw, l3Count);
					HT.moduleXL1Writer(fw, "dl1", "dl2", dl1Count, dl2Count);
					HT.moduleXL1Writer(fw, "il1", "il2", il1Count, il2Count);

					for (int i = 0; i < dl2Count; i++) {
						fw.write("\n[Module mod-l2-" + 2 * i + "]"
								+ "\nType = Cache" + "\nGeometry = geo-l2"
								+ "\nHighNetwork = net-l1-l2-" + 2 * i
								+ "\nLowModules = mod-l3-" + i % l3Count
								+ "\nLowNetwork = net-l2-l3-" + i % l3Count
								+ "\n");
					}

					for (int i = 0; i < il2Count; i++) {
						fw.write("\n[Module mod-l2-" + ((2 * i) + 1) + "]"
								+ "\nType = Cache" + "\nGeometry = geo-l2"
								+ "\nHighNetwork = net-l1-l2-" + ((2 * i) + 1)
								+ "\nLowModules = mod-mm"
								+ "\nLowNetwork = net-0"
								+ "\nLowNetworkNode = n" + ((2 * i) + 1) + "\n");
					}

					for (int i = 0; i < l3Count; i++) {
						fw.write("\n[Module mod-l3-" + i + "]"
								+ "\nType = Cache" + "\nGeometry = geo-l3"
								+ "\nHighNetwork = net-l2-l3-" + i
								+ "\nLowModules = mod-mm"
								+ "\nLowNetwork = net-0"
								+ "\nLowNetworkNode = n" + 2 * i + "\n");
					}

					HT.memWriter(fw, "0", 2 * (il2Count + l3Count));

					for (int i = 0; i < l3Count; i++) {
						fw.write("\n[Network net-l2-l3-" + i + "]"
								+ "\nDefaultInputBufferSize = 1024"
								+ "\nDefaultOutputBufferSize = 1024"
								+ "\nDefaultBandwidth = 2048" + "\n");
					}

					for (int i = 0; i < dl2Count; i++) {
						fw.write("\n[Network net-l1-l2-" + 2 * i + "]"
								+ "\nDefaultInputBufferSize = 1024 "
								+ "\nDefaultOutputBufferSize = 1024"
								+ "\nDefaultBandwidth = 1024" + "\n");
					}

					for (int i = 0; i < il2Count; i++) {
						fw.write("\n[Network net-l1-l2-" + ((2 * i) + 1) + "]"
								+ "\nDefaultInputBufferSize = 1024 "
								+ "\nDefaultOutputBufferSize = 1024"
								+ "\nDefaultBandwidth = 1024" + "\n");
					}

					for (int i = 0; i < coreCount; i++) {
						fw.write("\n[Entry core-" + i + "]" + "\nArch = x86"
								+ "\nCore = " + i + "\nThread = 0"
								+ "\nDataModule = mod-l1-" + 2 * (i % dl1Count)
								+ "\nInstModule = mod-l1-"
								+ (2 * (i % il1Count) + 1) + "\n");
					}
					File network = new File(parent + topology + "_net");
					FileWriter fwn = new FileWriter(network);

					fwn.write("\n[Network.net-0]"
							+ "\nDefaultInputBufferSize = 512"
							+ "\nDefaultOutputBufferSize = 512"
							+ "\nDefaultBandwidth = 8" + "\n");

					for (int i = 2; l3Count / i > 0; i = i << 1) {
						for (int j = 0; j < l3Count / i; j++) {
							fwn.write("\n[Network.net-0.Node.sw");
							for (int k = 2; k < i; k = k << 1) {
								fwn.write("0");
							}
							fwn.write(2 * j + "]" + "\nType = Switch" + "\n");
						}
					}

					for (int i = 2; il2Count / i > 0; i = i << 1) {
						for (int j = 0; j < il2Count / i; j++) {
							fwn.write("\n[Network.net-0.Node.sw");
							for (int k = 2; k < i; k = k << 1) {
								fwn.write("0");
							}
							fwn.write(((2 * j) + 1) + "]" + "\nType = Switch"
									+ "\n");
						}
					}

					fwn.write("\n[Network.net-0.Node.sw" + 2
							* (il2Count + l3Count) + "]" + "\nType = Switch"
							+ "\n");

					for (int i = 0; i < il2Count; i++) {
						fwn.write("\n[Network.net-0.Node.n" + ((2 * i) + 1)
								+ "]" + "\nType = EndNode" + "\n");
					}

					for (int i = 0; i < l3Count; i++) {
						fwn.write("\n[Network.net-0.Node.n" + (2 * i) + "]"
								+ "\nType = EndNode" + "\n");
					}

					fwn.write("\n[Network.net-0.Node.n" + 2
							* (il2Count + l3Count) + "]" + "\nType = EndNode"
							+ "\n");
					
					fwn.write("\n[Network.net-0.Link.n" + 2
							* (il2Count + l3Count) + "-sw" + 2
							* (il2Count + l3Count) + "]" + "\nSource = n" + 2
							* (il2Count + l3Count) + "\nDest = sw" + 2
							* (il2Count + l3Count) + "\nType = Bidirectional"
							+ "\n");
					
					String s = "";
					String d = "1";

					StringBuilder sb = new StringBuilder();
					if (il2Count == 1) {
						fwn.write("\n[Network.net-0.Link.n" + 1 + "-sw"
								+ (2 * (il2Count + l3Count)) + "]"
								+ "\nSource = n" + 1 + "\nDest = sw"
								+ (2 * (il2Count + l3Count))
								+ "\nType = Bidirectional" + "\n");
					}
					if (il2Count != 1) {
						sb.delete(0, sb.length());
						for (int k = 0; k < (il2Count / 2) * 2; k++) {
							fwn.write("\n[Network.net-0.Link.n" + (2 * k + 1)
									+ "-sw" + (2 * (k / 2) + 1) + "]" + "\nSource = n"
									+ (2 * k + 1) + "\nDest = sw" + (2 * (k / 2) + 1)
									+ "\nType = Bidirectional" + "\n");
						}
						if (il2Count % 2 == 1){
							fwn.write("\n[Network.net-0.Link.n" + (2 * il2Count - 1)
									+ "-sw" + (2 * (il2Count / 2) - 1) + "]" + "\nSource = n"
									+ (2 * il2Count - 1) + "\nDest = sw" + (2 * (il2Count / 2) - 1)
									+ "\nType = Bidirectional" + "\n");
						}
						for (int j = 2; il2Count / j > 1; j = j << 1) {
							String j0 = sb.toString();
							if (il2Count / j % 2 == 0) {
								for (int k = 0; k < il2Count / j; k++) {
									s = j0 + (2 * k + 1);
									d = 0 + j0 + (2 * (k / 2) + 1);
									fwn.write("\n[Network.net-0.Link.sw" + s
											+ "-sw" + d + "]" + "\nSource = sw"
											+ s + "\nDest = sw" + d
											+ "\nType = Bidirectional" + "\n");
								}
							}
							if (il2Count / j % 2 == 1) {
								int k;
								for (k = 0; k < (il2Count / j) - 1; k++) {
									s = j0 + (2 * k + 1);
									d = 0 + j0 + (2 * (k / 2) + 1);
									fwn.write("\n[Network.net-0.Link.sw" + s
											+ "-sw" + d + "]" + "\nSource = sw"
											+ s + "\nDest = sw" + d
											+ "\nType = Bidirectional" + "\n");
								}
								fwn.write("\n[Network.net-0.Link.sw" + j0
										+ (2 * k + 1) + "-sw" + d + "]"
										+ "\nSource = sw" + j0 + (2 * k + 1)
										+ "\nDest = sw" + d
										+ "\nType = Bidirectional" + "\n");
							}
							sb.append("0");
						}
						fwn.write("\n[Network.net-0.Link.sw" + d + "-sw"
								+ (2 * (il2Count + l3Count)) + "]"
								+ "\nSource = sw" + d + "\nDest = sw"
								+ (2 * (il2Count + l3Count))
								+ "\nType = Bidirectional" + "\n");
					}

					s = "";
					d = "0";
					if (l3Count == 1) {
						fwn.write("\n[Network.net-0.Link.n" + 0 + "-sw"
								+ (2 * (il2Count + l3Count)) + "]"
								+ "\nSource = n" + 0 + "\nDest = sw"
								+ (2 * (il2Count + l3Count))
								+ "\nType = Bidirectional" + "\n");
					}
					if (l3Count != 1) {
						sb.delete(0, sb.length());
						for (int k = 0; k < (l3Count / 2) * 2; k++) {
							fwn.write("\n[Network.net-0.Link.n" + 2 * k
									+ "-sw" + 2 *(k / 2)+ "]" + "\nSource = n"
									+ 2 * k + "\nDest = sw" + 2 * (k / 2)
									+ "\nType = Bidirectional" + "\n");
						}
						if (l3Count % 2 == 1){
							fwn.write("\n[Network.net-0.Link.n" + (2 * l3Count - 2)
									+ "-sw" + (2 * (l3Count / 2) - 2) + "]" + "\nSource = n"
									+ (2 * l3Count - 2) + "\nDest = sw" + (2 * (l3Count / 2) - 2)
									+ "\nType = Bidirectional" + "\n");
						}
						for (int j = 2; l3Count / j > 1; j = j << 1) {
							String j0 = sb.toString();
							if (l3Count / j % 2 == 0) {
								for (int k = 0; k < l3Count / j; k++) {
									s = j0 + (2 * k);
									d = 0 + j0 + (2 * (k / 2));
									fwn.write("\n[Network.net-0.Link.sw" + s
											+ "-sw" + d + "]" + "\nSource = sw"
											+ s + "\nDest = sw" + d
											+ "\nType = Bidirectional" + "\n");
								}
							}
							if (l3Count / j % 2 == 1) {
								int k;
								for (k = 0; k < (l3Count / j) - 1; k++) {
									s = j0 + (2 * k);
									d = 0 + j0 + (2 * (k / 2));
									fwn.write("\n[Network.net-0.Link.sw" + s
											+ "-sw" + d + "]" + "\nSource = sw"
											+ s + "\nDest = sw" + d
											+ "\nType = Bidirectional" + "\n");
								}
								fwn.write("\n[Network.net-0.Link.sw" + j0
										+ (2 * k) + "-sw" + d + "]"
										+ "\nSource = sw" + j0 + (2 * k)
										+ "\nDest = sw" + d
										+ "\nType = Bidirectional" + "\n");
							}
							sb.append("0");
						}
						fwn.write("\n[Network.net-0.Link.sw" + d + "-sw"
								+ (2 * (il2Count + l3Count)) + "]"
								+ "\nSource = sw" + d + "\nDest = sw"
								+ (2 * (il2Count + l3Count))
								+ "\nType = Bidirectional" + "\n");
					}

					fwn.close();
					fw.close();
				} catch (Exception e) {
					System.out.println(topology + " is an invalid topology!");
					failures++;
					continue;
				}
			}
			FileWriter x86Writer = new FileWriter(new File(parent + topology
					+ "_x86"));
			x86Writer.write("[ General ]\nCores = "
					+ Integer.valueOf(thisTopology[0].substring(0,
							thisTopology[0].length() - 1)) + "\nThreads = 1");
			x86Writer.close();
		}
		if (failures > 0) {
			System.out.println(failures
					+ " topologies couldn't be constructed!");
		}
		long endTime = System.nanoTime();
		System.out.println("Took " + (endTime - startTime) + " ns to write "
				+ (topologies.size() - failures) + " topologies.");
	}
}
