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
import os
import re

def remover(pattern, string):
    scanner = re.search(pattern, string, re.IGNORECASE)
    return (string[:scanner.start()] + string[scanner.end():]).strip()

from datetime import datetime
path = os.path.dirname(os.path.abspath(__file__))
topList = ["4C_4L1_4L2_1L3"]
current = path + "/Results/" + datetime.now().strftime('%Y-%m-%d_%H:%M:%S')
os.makedirs(current)
log = " 2>&1 | tee -a " + current + "/ExperimentLogs.txt"
m2sArgs = ""
for line in open(path + "/Paths"):
    if line.strip()[:3].lower() == "m2s":
        m2s = remover("m2s *= *", line)
for experiment in open(path + "/Experiments"):
    exResults = current + "/" + experiment.strip()
    if not os.path.exists(path + "/Benchmarks/"
        + experiment.strip() + "/runspec"):
        continue
    os.mkdir(exResults)
    for topology in topList:
        tail = ""
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
        os.mkdir(exResults + "/" + topology)
        topologyPath = path + "/Topologies/" + topology
        topologyResult = exResults + "/" + topology
        os.system(m2s + " --x86-sim functional " 
        + exe + tail + log)
    print ("Experiments completed, check the output files under "
    + "benchmark folders to confirm current settings!")
