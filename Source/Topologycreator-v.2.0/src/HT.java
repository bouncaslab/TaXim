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

import java.io.FileWriter;
import java.io.IOException;

public class HT {

	/**
	 * Writes the cache geometries requried by Multi2Sim, checks L3 Count to
	 * write L3s.
	 */
	public static void geometry(FileWriter fw, int l3count) throws IOException {
		fw.write("[CacheGeometry geo-l1]" + "\nSets = 64" + "\nAssoc = 8"
				+ "\nBlockSize = 64" + "\nLatency = 5" + "\n"
				+ "\n[CacheGeometry geo-l2]" + "\nSets = 512" + "\nAssoc = 8"
				+ "\nBlockSize = 64" + "\nLatency = 12" + "\n");
		if (l3count > 0) {
			fw.write("\n[CacheGeometry geo-l3]" + "\nSets = 8192"
					+ "\nAssoc = 16" + "\nBlockSize = 64" + "\nLatency = 36"
					+ "\n");
		}
	}

	/**
	 * Create first level modules, enter the type of l1 as string and its count,
	 * and the type and count of l2 modules.
	 */
	public static void moduleXL1Writer(FileWriter fw, String src, String des,
			int in, int out) throws IOException {
		switch (des) {
		case "l2":
			if (src == "l1") {
				for (int i = 0; i < in; i++) {
					fw.write("\n[Module mod-l1-" + i + "]" + "\nType = Cache"
							+ "\nGeometry = geo-l1" + "\nLowModules = mod-l2-"
							+ i % out + "\nLowNetwork = net-l1-l2-" + i % out
							+ "\n");
				}
			}
			if (src == "dl1") {
				for (int i = 0; i < in; i++) {
					fw.write("\n[Module mod-l1-" + 2 * i + "]"
							+ "\nType = Cache" + "\nGeometry = geo-l1"
							+ "\nLowModules = mod-l2-" + i % out
							+ "\nLowNetwork = net-l1-l2-" + i % out + "\n");
				}
			}
			if (src == "il1") {
				for (int i = 0; i < in; i++) {
					fw.write("\n[Module mod-l1-" + ((2 * i) + 1) + "]"
							+ "\nType = Cache" + "\nGeometry = geo-l1"
							+ "\nLowModules = mod-l2-" + i % out
							+ "\nLowNetwork = net-l1-l2-" + i % out + "\n");
				}
			}
			break;
		case "dl2":
			for (int i = 0; i < in; i++) {
				fw.write("\n[Module mod-l1-" + 2 * i + "]" + "\nType = Cache"
						+ "\nGeometry = geo-l1" + "\nLowModules = mod-l2-" + 2
						* (i % out) + "\nLowNetwork = net-l1-l2-" + 2
						* (i % out) + "\n");
			}
			break;
		case "il2":
			for (int i = 0; i < in; i++) {
				fw.write("\n[Module mod-l1-" + ((2 * i) + 1) + "]"
						+ "\nType = Cache" + "\nGeometry = geo-l1"
						+ "\nLowModules = mod-l2-" + ((2 * (i % out)) + 1)
						+ "\nLowNetwork = net-l1-l2-" + ((2 * (i % out)) + 1)
						+ "\n");
			}
			break;
		case "l3":
			for (int i = 0; i < in; i++) {
				fw.write("\n[Module mod-l1-" + ((2 * i) + 1) + "]"
						+ "\nType = Cache" + "\nGeometry = geo-l1"
						+ "\nLowModules = mod-l3-" + i % out
						+ "\nLowNetwork = net-" + i % out
						+ "\nLowNetworkNode = n" + ((2 * i) + 1) + "\n");
			}
			break;
		case "mm":
			for (int i = 0; i < in; i++) {
				fw.write("\n[Module mod-l1-" + ((2 * i) + 1) + "]"
						+ "\nType = Cache" + "\nGeometry = geo-l1"
						+ "\nLowModules = mod-mm" + "\nLowNetwork = net-0"
						+ "\nLowNetworkNode = n" + ((2 * i) + 1) + "\n");
			}
			break;
		}
	}

	public static void memWriter(FileWriter fw, String src, int Node)
			throws IOException {
		fw.write("\n[Module mod-mm]" + "\nType = MainMemory"
				+ "\nBlockSize = 256" + "\nLatency = 93");
		if (Node == 0) {
			fw.write("\nHighNetwork = net-" + src + "-mm-0" + "\n");
		} else {
			fw.write("\nHighNetwork = net-0" + "\nHighNetworkNode = n" + Node
					+ "\n");
		}
	}

	public static void networker(FileWriter fw, String src, String des,
			int desCount, int buffer, int bandwidth) throws IOException {
		for (int i = 0; i < desCount; i++) {
			fw.write("\n[Network net-" + src + "-" + des + "-" + i + "]"
					+ "\nDefaultInputBufferSize = " + buffer
					+ "\nDefaultOutputBufferSize = " + buffer
					+ "\nDefaultBandwidth = " + bandwidth + "\n");
		}
	}

	public static void networkerEven(FileWriter fw, String src, String des,
			int desCount, int buffer, int bandwidth) throws IOException {
		for (int i = 0; i < desCount; i++) {
			fw.write("\n[Network net-" + src + "-" + des + "-" + 2 * i + "]"
					+ "\nDefaultInputBufferSize = " + buffer
					+ "\nDefaultOutputBufferSize = " + buffer
					+ "\nDefaultBandwidth = " + bandwidth + "\n");
		}
	}
}
