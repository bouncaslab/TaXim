'''
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
'''

try:
    import xml.etree.cElementTree as ET	
except ImportError:
    import xml.etree.ElementTree as ET

import sys

dict ={
	'Dispatch.Total'			:	'total_instructions',
	'Dispatch.Integer'			:	'int_instructions',
	'Dispatch.FloatingPoint'	:	'fp_instructions',
	'Dispatch.Ctrl'				:	'branch_instructions',
	'Commit.Mispred'			:	'branch_mispredictions',
	'Dispatch.Uop.load'			:	'load_instructions',
	'Dispatch.Uop.store'		:	'store_instructions',
	'Commit.Total'				:	'committed_instructions',
	'Commit.Integer'			:	'committed_int_instructions',
	'Commit.FloatingPoint'		:	'committed_fp_instructions',
	'Commit.DutyCycle'			:	'pipeline_duty_cycle',
	'Dispatch.Uop.call'			:	'function_calls',
	'Dispatch.WndSwitch'		:	'context_switches',
	'Issue.Integer'				:	'ialu_accesses',
	'Issue.FloatingPoint'		:	'fpu_accesses',
	'Issue.ComplexInteger'		:	'mul_accesses'
	
};

dict_thread = {
	'ROB.Reads'					:	'ROB_reads',
	'ROB.Writes'				:	'ROB_writes',	
	'RAT.IntReads'				:	'rename_reads',
	'RAT.IntWrites'				:	'rename_writes',	
	'IQ.Reads'					:	'inst_window_reads',
	'IQ.Writes'					:	'inst_window_writes',	
	'IQ.WakeupAccesses'			:	'inst_window_wakeup_accesses',
	'RF_Int.Reads'				:	'int_regfile_reads',
	'RF_Int.Writes'				:	'int_regfile_writes',	
	#'BTB.Reads'				:	'read_accesses',	
	#'BTB.Writes'				:	'write_accesses'
}
  
dict_thread_init_Values = {
	'ROB.Reads'					:	0,
	'ROB.Writes'				:	0,	
	'RAT.IntReads'				:	0,
	'RAT.IntWrites'				:	0,	
	'IQ.Reads'					:	0,
	'IQ.Writes'					:	0,	
	'IQ.WakeupAccesses'			:	0,
	'RF_Int.Reads'				:	0,
	'RF_Int.Writes'				:	0,	
	#'BTB.Reads'				:	0,	
	#'BTB.Writes'				:	0
}
 
dict_itlb ={
	'Accesses'				:	'total_accesses',
	'Misses'				:	'total_misses',
	'Evictions'				:	'conflicts'
};

dict_icache ={
	'Reads'					:	'read_accesses',
	'ReadMisses'			: 	'read_misses',
	'Evictions'				:	'conflicts'
};

dict_dtlb ={	
	'Accesses'				:	'total_accesses',
	'Misses'				:	'total_misses',
	'Evictions'				:	'conflicts'
};

dict_dcache ={	
	'Reads'					:	'read_accesses',
	'Writes'				:	'write_accesses',
	'ReadMisses'			:	'read_misses',
	'WriteMisses'			:	'write_misses',
	'Evictions'				:	'conflicts'
};

# default values



def getCoretree( coreNumber,tree):

	core_tree_string = ET.tostring(tree.find(".//*[@name='core0']"));
	core_tree = ET.ElementTree(ET.fromstring(core_tree_string))
	  
	core_root = core_tree.getroot()  
	core_root.attrib["id"]="system.core"+coreNumber
	core_root.attrib["name"]="core"+coreNumber
	core_root.find(".//*[@name='PBT']").attrib["id"]="system.core"+coreNumber+".predictor"
	core_root.find(".//*[@name='itlb']").attrib["id"]="system.core"+coreNumber+".itlb"
	core_root.find(".//*[@name='icache']").attrib["id"]="system.core"+coreNumber+".icache"
	core_root.find(".//*[@name='dtlb']").attrib["id"]="system.core"+coreNumber+".dtlb"
	core_root.find(".//*[@name='dcache']").attrib["id"]="system.core"+coreNumber+".dcache"
	core_root.find(".//*[@name='BTB']").attrib["id"]="system.core"+coreNumber+".BTB"	
		
	return core_tree

def getL2tree(L2Number,tree):

	L2_tree_string = ET.tostring(tree.find(".//*[@name='L20']"));
	L2_tree = ET.ElementTree(ET.fromstring(L2_tree_string))
	  
	L2_root = L2_tree.getroot()  
	L2_root.attrib["id"]="system.L2"+L2Number
	L2_root.attrib["name"]="L2"+L2Number
	
	return L2_tree

def getL3tree(L3Number,tree):

	L3_tree_string = ET.tostring(tree.find(".//*[@name='L30']"));
	L3_tree = ET.ElementTree(ET.fromstring(L3_tree_string))
	  
	L3_root = L3_tree.getroot()  
	L3_root.attrib["id"]="system.L3"+L3Number
	L3_root.attrib["name"]="L3"+L3Number
	
	return L3_tree	

Dict_module ={
};

Dict_entry ={
};

def parse_x86Config():
	coreNumber=0
	moduleName=''
	with open(sys.argv[5]) as file_x86Report:
		for line in file_x86Report:
			if "[" in line and "Module" in line :
			 	moduleName = line.strip('\n ]').split(" ")[1]				
			elif "Geometry" in line and not "[" in line:
				geometry=line.split("=")[1].strip('\n ')
				Dict_module[moduleName]={'geometry' : geometry , 'isUsed' : '0'}
			elif "Entry" in line:
				entryName = line.strip('\n ]').split(" ")[1]
			elif "DataModule" in line:
				DataModule=line.split("=")[1].strip('\n ')
			elif "InstModule" in line:
				InstModule=line.split("=")[1].strip('\n ')
				Dict_entry[entryName] = {'cache':{'DataModule' : DataModule, 'InstModule' : InstModule}, "coreNumber":coreNumber}
				coreNumber = coreNumber+1
								
def parse_x86Report(tree,shiftAmount):
	
	root = tree.getroot()
	
	component="NotRelated";
    
	number_hardware_threads="1"

	coreNumber=0
	ThreadNumber=0
	total_cycles=0
	

	with open(sys.argv[3]) as file_x86Report:
		for line in file_x86Report:
			if "[" and "]" in line:

				if "[ Config.General ]" in line:
					component="General"
				elif "[ Global ]" in line:
					component="Global"
				elif "[ c" in line and not "t" in line and not "." in line:
					
					component="Core"										
					coreNumber=line.split("c")[1][0]
					if coreNumber is not "0":										
						core_tree = getCoretree(coreNumber,tree)
						shiftAmount=shiftAmount+1
						root[0].insert(29+shiftAmount,core_tree.getroot())
					
					#Default core cache values
					root.find(".//*[@name='icache']/*[@name='icache_config']").attrib["value"]="1,1,1,1, 0,0, 0,0"
					root.find(".//*[@name='icache']/*[@name='buffer_sizes']").attrib["value"]="0, 0, 0, 0"
					root.find(".//*[@name='icache']/*[@name='read_accesses']").attrib["value"]="0"
					root.find(".//*[@name='icache']/*[@name='read_misses']").attrib["value"]="0"
					root.find(".//*[@name='icache']/*[@name='conflicts']").attrib["value"]="0"

					root.find(".//*[@name='dcache']/*[@name='dcache_config']").attrib["value"]="1,1,1,1, 0,0, 0,0 "
					root.find(".//*[@name='dcache']/*[@name='buffer_sizes']").attrib["value"]="0, 0, 0, 0"
					root.find(".//*[@name='dcache']/*[@name='read_accesses']").attrib["value"]="0"
					root.find(".//*[@name='dcache']/*[@name='write_accesses']").attrib["value"]="0"
					root.find(".//*[@name='dcache']/*[@name='read_misses']").attrib["value"]="0"
					root.find(".//*[@name='dcache']/*[@name='write_misses']").attrib["value"]="0"
					root.find(".//*[@name='dcache']/*[@name='conflicts']").attrib["value"]="0"

					root.find(".//*[@name='core"+coreNumber+"']/*[@name='total_cycles']").attrib["value"]=total_cycles
					root.find(".//*[@name='core"+coreNumber+"']/*[@name='busy_cycles']").attrib["value"]=total_cycles

					ThreadNumber=0					
						
				elif "[ c" in line and "t" in line and not "." in line: #Thread
					component="Thread"					
					ThreadNumber=ThreadNumber+1					
				
				else:
					component="NotRelated" 
						
			elif component != "NotRelated":	
								
				if "=" in line:
					key = line.strip('\n ').split()[0]
					value = line.strip('\n ').split()[2]
					if component == "General":
						if key == "Cores":
							number_of_cores = value
						elif key == "Threads":	
							number_hardware_threads = value
							
					elif component == "Global":
						if key == "Cycles": 
							root.find(".//*[@name='system']/*[@name='total_cycles']").attrib["value"]=value
							root.find(".//*[@name='system']/*[@name='busy_cycles']").attrib["value"]=value	
							total_cycles= value					
					
					elif component ==  "Core":												
						if key in dict.keys():	
							root.find(".//*[@name='core"+coreNumber+"']/*[@name='"+dict[key]+"']").attrib["value"]=value												
						elif key == "BTB.Reads":
							root.find(".//*[@name='core"+coreNumber+"']/*[@name='BTB']/*[@name='read_accesses']").attrib["value"]=value
						elif key == "BTB.Writes":
							root.find(".//*[@name='core"+coreNumber+"']/*[@name='BTB']/*[@name='write_accesses']").attrib["value"]=value
					
					elif component == "Thread":							
						if key in dict_thread.keys():
							dict_thread_init_Values[key] = dict_thread_init_Values[key] + int(value)
							if ThreadNumber == int(number_hardware_threads):
								root.find(".//*[@name='core"+coreNumber+"']/*[@name='"+dict_thread[key]+"']").attrib["value"] = str(dict_thread_init_Values[key])
						
					
	

	tree.find(".//*[@name='number_of_cores']").attrib["value"]=number_of_cores	
	number_hardware_threads_EList=tree.findall(".//*[@name='number_hardware_threads']")
	for	number_hardware_threads_E in number_hardware_threads_EList:
		number_hardware_threads_E.attrib["value"]=number_hardware_threads

	return shiftAmount

def parse_memreport(tree, number_of_L2s, number_of_L3s,shiftAmount):
	
	root = tree.getroot()
	memType = "NotCache"
	L2Number=0
	L3Number=0
	amountTransfer=0
		
	#Dict_entry[entryName] = {'cache':{'DataModule' : DataModule, 'InstModule' : InstModule}, "coreNumber":coreNumber}
	
	with open(sys.argv[4]) as f:						
		for line in f:
			if "Transfers" in line:
				Transfer = line.strip('\n ').split()[2]
				
				amountTransfer=amountTransfer+int(Transfer)
			
			if "[" and "]" in line:
				coreNumber=0
				isDataCache=0
				isInstructionCache=0
				moduleName=line.strip("\n [ ]")	
				#print "modulename",moduleName
				if moduleName in Dict_module.keys():
					if Dict_module[moduleName]['geometry'] == "geo-l1":
						memType="L1"
						breakOuter=0
						
						for core in Dict_entry.keys():
							#print "core",core
							for cacheType in Dict_entry[core]['cache'].keys():			
						
								#print "cacheType",cacheType
								#print "cacheName",Dict_entry[core]['cache'][cacheType]
								if(Dict_entry[core]['cache'][cacheType]==moduleName):
									
									if(cacheType=="DataModule"):
										isDataCache=1
									else:
										isDataCache=0

									if(cacheType=="InstModule"):	
										isInstructionCache=1
									else:
										isInstructionCache=0

									coreNumber=str(Dict_entry[core]["coreNumber"])
									
									breakOuter=1
									break
							if(breakOuter==1):
								break
						
						if isDataCache:

							#root.find(".//*[@name='core"+coreNumber+"']/*[@name='dcache']/*[@name='dcache_config']").attrib["value"]="1,1,1,1, 0,0, 0,0 "
							root.find(".//*[@name='core"+coreNumber+"']/*[@name='dcache']/*[@name='buffer_sizes']").attrib["value"]="16, 16, 16, 16"
							#root.find(".//*[@name='core"+coreNumber+"']/*[@name='dcache']/*[@name='read_accesses']").attrib["value"]="0"
							#root.find(".//*[@name='core"+coreNumber+"']/*[@name='dcache']/*[@name='write_accesses']").attrib["value"]="0"
							#root.find(".//*[@name='core"+coreNumber+"']/*[@name='dcache']/*[@name='read_misses']").attrib["value"]="0"
							#root.find(".//*[@name='core"+coreNumber+"']/*[@name='dcache']/*[@name='write_misses']").attrib["value"]="0"
							root.find(".//*[@name='core"+coreNumber+"']/*[@name='dcache']/*[@name='conflicts']").attrib["value"]="0"

						elif isInstructionCache:
							root.find(".//*[@name='core"+coreNumber+"']/*[@name='icache']/*[@name='buffer_sizes']").attrib["value"]="16, 16, 16,0"
							root.find(".//*[@name='core"+coreNumber+"']/*[@name='icache']/*[@name='conflicts']").attrib["value"]="0"
							
						#elif isInstructionCache:		


				
									
					if Dict_module[moduleName]['geometry'] == "geo-l2":		
						memType="L2"
						L2Number=number_of_L2s
						if L2Number is not "0":
							L2_tree = getL2tree(L2Number,tree)
							shiftAmount=shiftAmount+1
							root[0].insert(32+shiftAmount,L2_tree.getroot())
						
						number_of_L2s = str(int(number_of_L2s)+1)
						#print "number_of_L2ss",number_of_L2s
					
					if Dict_module[moduleName]['geometry'] == "geo-l3":		
						memType="L3"						
						L3Number=number_of_L3s
						if L3Number is not "0":
							L3_tree = getL3tree(L3Number,tree)
							shiftAmount=shiftAmount+1
							root[0].insert(33+shiftAmount,L3_tree.getroot())
						number_of_L3s = str(int(number_of_L3s)+1)
				else:
					memType = "NotCache"					
				
			elif   memType is not "NotCache":
				
				#print memType
				if "=" in line:
					if memType=="L1":
						if isDataCache:	
							onChipCacheType="dcache"
						elif isInstructionCache:	
							onChipCacheType="icache"
							
						key = line.strip('\n ').split()[0]
						value = line.strip('\n ').split()[2]
						
						if key == "Sets":
							Sets = value
						elif key == "Assoc":
							Assoc = value
						elif key == "BlockSize":
							BlockSize = value	
						elif key == "Latency":
							Latency = value
						elif key == "Ports":
							 							
							if isDataCache:	
								Dir_configValue= str(int(BlockSize)*int(Assoc)*int(Sets))+", "+BlockSize+", "+Assoc+", "+"1"+", "+"3"+", "+Latency+", "+"16" +", "+"1"
								tree.find(".//*[@name='core"+coreNumber+"']/*[@name='dcache']/*[@name='dcache_config']").attrib["value"]=Dir_configValue
							elif isInstructionCache:
								Dir_configValue= str(int(BlockSize)*int(Assoc)*int(Sets))+", "+BlockSize+", "+Assoc+", "+"1"+", "+"8"+", "+Latency+", "+"32" +", "+"0" 	
								tree.find(".//*[@name='core"+coreNumber+"']/*[@name='icache']/*[@name='icache_config']").attrib["value"]=Dir_configValue	
							
						elif key == "Reads":
							
							if isDataCache:	
								tree.find(".//*[@name='core"+coreNumber+"']/*[@name='dcache']/*[@name='read_accesses']").attrib["value"]=value
							elif isInstructionCache:	
								tree.find(".//*[@name='core"+coreNumber+"']/*[@name='icache']/*[@name='read_accesses']").attrib["value"]=value

							
							
						elif key == "Writes":
							if isDataCache:	
								tree.find(".//*[@name='core"+coreNumber+"']/*[@name='dcache']/*[@name='write_accesses']").attrib["value"]=value
							
							
							
						elif key == "ReadMisses":
							if isDataCache:	
								tree.find(".//*[@name='core"+coreNumber+"']/*[@name='dcache']/*[@name='read_misses']").attrib["value"]=value
							elif isInstructionCache:	
								tree.find(".//*[@name='core"+coreNumber+"']/*[@name='icache']/*[@name='read_misses']").attrib["value"]=value
							
						
						elif key == "WriteMisses":
							if isDataCache:	
								tree.find(".//*[@name='core"+coreNumber+"']/*[@name='dcache']/*[@name='write_misses']").attrib["value"]=value
							
							
							
					
					elif memType=="L2":
						
						key = line.strip('\n ').split()[0]
						value = line.strip('\n ').split()[2]
						
						if key == "Sets":
							Sets = value
						elif key == "Assoc":
							Assoc = value
						elif key == "BlockSize":
							BlockSize = value	
						elif key == "Latency":
							Latency = value
						elif key == "Ports":
							Dir_configValue= str(int(BlockSize)*int(Assoc)*int(Sets))+", "+BlockSize+", "+Assoc+", "+"8"+", "+"8"+", "+Latency+", "+"32"+", "+"1"
							#<!-- the parameters are capacity,block_width,associativity,bank, throughput w.r.t. core clock, latency w.r.t. core clock,-->	
							#print "L2Number",L2Number
							tree.find(".//*[@name='L2"+L2Number+"']/*[@name='L2_config']").attrib["value"]=Dir_configValue
						elif key == "Reads":
							tree.find(".//*[@name='L2"+L2Number+"']/*[@name='read_accesses']").attrib["value"]=value
						elif key == "Writes":
							tree.find(".//*[@name='L2"+L2Number+"']/*[@name='write_accesses']").attrib["value"]=value
						elif key == "ReadMisses":
							tree.find(".//*[@name='L2"+L2Number+"']/*[@name='read_misses']").attrib["value"]=value
						elif key == "WriteMisses":						
							tree.find(".//*[@name='L2"+L2Number+"']/*[@name='write_misses']").attrib["value"]=value
					
					elif memType=="L3":
						
						key = line.strip('\n ').split()[0]
						value = line.strip('\n ').split()[2]
						
						if key == "Sets":
							Sets = value
						elif key == "Assoc":
							Assoc = value
						elif key == "BlockSize":
							BlockSize = value	
						elif key == "Latency":
							Latency = value
						elif key == "Ports":
							Dir_configValue= str(int(BlockSize)*int(Assoc)*int(Sets))+", "+BlockSize+", "+Assoc+", "+"16"+", "+"16"+", "+Latency+", "+"100"+", "+"1"
							#<!-- the parameters are capacity,block_width,associativity,bank, throughput w.r.t. core clock, latency w.r.t. core clock,-->	
							#print "L3Number",L3Number
							tree.find(".//*[@name='L3"+L3Number+"']/*[@name='L3_config']").attrib["value"]=Dir_configValue
						elif key == "Reads":
							tree.find(".//*[@name='L3"+L3Number+"']/*[@name='read_accesses']").attrib["value"]=value
						elif key == "Writes":
							tree.find(".//*[@name='L3"+L3Number+"']/*[@name='write_accesses']").attrib["value"]=value
						elif key == "ReadMisses":
							tree.find(".//*[@name='L3"+L3Number+"']/*[@name='read_misses']").attrib["value"]=value
						elif key == "WriteMisses":						
							tree.find(".//*[@name='L3"+L3Number+"']/*[@name='write_misses']").attrib["value"]=value		
						
	
				

	root.find(".//*[@name='noc0']/*[@name='total_accesses']").attrib["value"]=str(amountTransfer)
	return number_of_L2s, number_of_L3s
	
def main():	

	number_of_L1Directories = "1"
	number_of_L2Directories = "1"
	
	number_of_L2s = "0"
	Private_L2 = "0"
	number_of_L3s = "0"
	number_of_NoCs = "1"

	homogeneous_cores = "0"
	homogeneous_L2s = "0"
	homogeneous_L1Directories = "0" 
	homogeneous_L2Directories = "0"	

	homogeneous_L3s = "0"

	tree = ET.parse(sys.argv[1])	

		
	shiftAmount=-1;
	parse_x86Config()
	shiftAmount = parse_x86Report(tree,shiftAmount)

	#print Dict_module
	#print Dict_entry

	number_of_L2s, number_of_L3s=parse_memreport(tree, number_of_L2s, number_of_L3s,shiftAmount)
	#print "number_of_L2s",number_of_L2s

	tree.find(".//*[@name='number_of_L1Directories']").attrib["value"]=number_of_L1Directories	
	tree.find(".//*[@name='number_of_L2Directories']").attrib["value"]=number_of_L2Directories		
	
	tree.find(".//*[@name='number_of_L2s']").attrib["value"]=number_of_L2s		
	tree.find(".//*[@name='Private_L2']").attrib["value"]=Private_L2		
	tree.find(".//*[@name='number_of_L3s']").attrib["value"]=number_of_L3s	
	tree.find(".//*[@name='homogeneous_L3s']").attrib["value"]=homogeneous_L3s	
	tree.find(".//*[@name='number_of_NoCs']").attrib["value"]=number_of_NoCs		
	
	tree.find(".//*[@name='homogeneous_cores']").attrib["value"]=homogeneous_cores		
	tree.find(".//*[@name='homogeneous_L2s']").attrib["value"]=homogeneous_L2s		
	tree.find(".//*[@name='homogeneous_L1Directories']").attrib["value"]=homogeneous_L1Directories		
	tree.find(".//*[@name='homogeneous_L2Directories']").attrib["value"]=homogeneous_L2Directories		
	
	if number_of_L3s == "0":
		system=tree.getroot()[0]
		system.remove(tree.find(".//*[@name='L30']"))

	tree.write(sys.argv[2])	
	
	
	
if __name__ == "__main__": main()
