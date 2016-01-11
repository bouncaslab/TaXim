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

# -*- coding: utf-8 -*-
import os, re, multiprocessing, sys

def remover(pattern, string):
    scanner = re.search(pattern, string, re.IGNORECASE)
    return (string[:scanner.start()] + string[scanner.end():]).strip()

def simulation(aList):
    global path, remover
    experiment = aList[0]
    topology = aList[1]
    exResults = current + "/" + experiment.strip()
    tail = ""
    exe = ""
    m2sArgs = ""
    for line in open(path + "/Benchmarks/"
    + experiment.strip() + "/runspec"):
        if line.strip()[:3].lower() == "exe":
            exe = (path + "/Benchmarks/" + experiment.strip() 
            + "/" + remover("exe *= *", line))
        if line.strip()[:4].lower() == "args":
            args = remover("args *= *", line).split()
            for arg in args:
                if arg == "$NTHREADS":
                    tail += " " + topology[0]
                elif "." in arg:
                    tail += (" " + path + "/Benchmarks/" 
                    + experiment.strip() + "/" + arg)
                else:
                    tail += " " + arg
    if line.strip()[:3].lower() == "m2s":
        m2sArgs = remover("m2s *= *", line)
    if exe != "":
        os.mkdir(exResults + "/" + topology)
        topologyPath = path + "/Topologies/" + topology
        topologyResult = exResults + "/" + topology
        if topology[-2:] == "BP":
            os.system(m2s + " --x86-sim detailed --mem-config "
            + topologyPath + " --x86-config "
            + topologyPath + "_x86 --x86-report "
            + topologyResult + "/x86report --mem-report " 
            + topologyResult + "/memreport --net-config " 
            + topologyPath + "_net " 
            + m2sArgs + " " + exe + tail + log)    
        else:
            os.system(m2s + " --x86-sim detailed --mem-config "
            + topologyPath + " --x86-config "
            + topologyPath + "_x86 --x86-report "
            + topologyResult + "/x86report --mem-report " 
            + topologyResult + "/memreport " 
            + m2sArgs + " " + exe + tail + log)
        os.system("python " + path + "/Power/Parser.py " + path
        + "/Power/Processor.xml " + topologyResult + "/McpatInput.xml "
        + topologyResult + "/x86report " + topologyResult
        + "/memreport " + topologyPath)
        os.system(mcpat + " -infile " + topologyResult 
        + "/McpatInput.xml -print_level 5 > " + topologyResult
        + "/powerResult.txt")

try:
    variable = int(sys.argv[1])
except IndexError:
    variable = ""
if variable and variable <= multiprocessing.cpu_count()-1: #2*multiprocessing.cpu_count()-1 for more allocation and assuming multicore machine 
    repeat = variable
    print ("You have chosen to execute " + str(variable) + " simultaneous"
    + " scripts. Your thread count is " + str(multiprocessing.cpu_count()))
else:
    repeat = multiprocessing.cpu_count() - 1
    print ("You have " + str(multiprocessing.cpu_count()) + " cores. "
           + str(repeat) + " simultaneous scripts will be run by default.")

from datetime import datetime
path = os.path.dirname(os.path.abspath(__file__))
os.system("java -jar " + path + "/TopologyCreator-v.2.4.jar")
topList = []
for topology in open(path + "/TopologyList"):
    if str(topology.strip()) not in topList:
        topList.append(str(topology.strip()))
current = path + "/Results/" + datetime.now().strftime('%Y-%m-%d_%H:%M:%S')
os.makedirs(current)
log = " 2>&1 | tee -a " + current + "/ExperimentLogs.txt"
processor = path + "/Power/Processor.xml"
for line in open(path + "/Paths"):
    if line.strip()[:3].lower() == "m2s":
        m2s = "/" + remover("m2s *= */*", line)
    if line.strip()[:5].lower() == "mcpat":
        mcpat = "/" + remover("McPAT *= */*", line)
experimentList = []
for experiment in open(path + "/Experiments"):
    if not os.path.exists(path + "/Benchmarks/"
        + experiment.strip() + "/runspec"):
        continue
    if experiment.strip() in experimentList:
        continue
    experimentList.append(experiment.strip())
    os.mkdir(current + "/" + experiment.strip())
#Parallel Stuff
if __name__ == '__main__':
    aList = []
    for experiment in experimentList:
        for topology in topList:
            aList.append([experiment, topology])
    pool = multiprocessing.Pool(processes = repeat)
    pool.imap_unordered(simulation, aList)
    pool.close()
    pool.join()
#Parallel Stuff
os.system("java -jar " + path + "/Extractor.jar " + current)
allR = open(current + "/AggregatedResults.csv", "w")
flag = True
filenames = []
for aFile in os.listdir(current):
    if aFile[-4:] == ".csv" and aFile != "AggregatedResults.csv":
        aResult = open(current + "/" + aFile)
        i = 0  
        if flag == False:
            allR.write("\n")
        for line in aResult:
            if flag == True or i > 0:
                allR.write(line)
                flag = False
            i += 1
        aResult.close()
allR.close()
