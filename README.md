## Contributors & Work Breakdown
**Ruth Rudolph (Project Leader)**: Lead developer for the **Visualization Module**. Implemented the GUI playback suite, custom render components (`PaintComponent`), simulation parsing logic, and dynamic cluster-color formatting algorithms.
**Sonali Naidoo**: Lead developer for the **Graphing Module**. Developed Python analytics scripts, UI layouts, module unit testing, and simulation verification algorithms.
**Byron Clarke**: Lead developer for the **Simulation Module**. Built the core multithreaded backend framework, coordinate space matrices, and backend performance profiling.

## **Abstract:** 
The project outlined below consists of a multithreaded Java program which simulates early
stage biofilm formation. The system models a growing colony of rod-shaped bacteria with the 
ability to move, reproduce, secrete extracellular polymeric substances (EPS), and take in 
nutrients on a two-dimensional grid. The project is modular, consisting of three core sections; 
firstly, a Java-based multithreaded simulation module which accepts an input file containing 
simulation parameters, performs the simulation and outputs results to an output file, allowing 
users to rerun visual displays or produce various graphs without rerunning the simulation. 
The output file contains details including particle positions over time as well as quantifying 
variables including sparseness and roughness. The particle position data is used by the second 
Java-based program, the visualization module which produces a movie-like representation of 
the colony over time. Finally, the python-based graphing module allows the user to critically 
examine several aspects of the simulation, by producing various graphs based on the 
simulation output. The three modules together form a program which allows for thorough 
investigation of colony behaviour under various settings, making it a potentially useful basis 
from which to expand and investigate biofilm formation in more detail. 

## **Introduction:** 
The aim of this project is to develop a particle-based biophysical model that simulates early
stage biofilm formation, with focus cantered on the role of extracellular polymeric substances 
(EPS) in driving cell aggregation. The model will represent a growing and expanding 
bacterial colony of mobile, rod-shaped bacteria which can move, reproduce, and secrete EPS. 
The primary goal is to explore how secreted EPS impacts the clustering of bacterial cells into 
dense, aggregated structures, which are an important precursor to mature biofilms. The 
simulation will be implemented in Java as a multithreaded application. It will include a 
simulation module, a separate visualization module for presentation of the results, and a 
graphing module to present graphs of the results. The model will serve as a research tool for 
understanding the underlying biophysical mechanisms of biofilm formation, as well as a 
framework that can potentially be extended to incorporate more complex biological 
processes. The implications of this work extend beyond a single research group. Biofilms 
have a significant impact and create challenges in both medical and industrial contexts, 
making a broad community of researchers, clinicians, and engineers indirect stakeholders. In 
healthcare, biofilms are associated with persistent infections such as chronic wounds and 
dental plaque, and they frequently colonize medical implants—leading to severe 
complications and increased treatment costs. In industry, biofilms contribute to food 
contamination, reduce equipment lifespan, and create inefficiencies in water treatment 
systems. As such, this project may be valuable to both academic and applied research 
communities seeking insight into early-stage biofilm formation. 
Our approach was primarily an evolutionary prototype driven approach. We did an initial 
analysis and design of our project in the first few weeks and then would iteratively create and 
test prototypes with additional features on each cycle. These cycles were asynchronous, and 
not formally implemented, hence this is something between a waterfall-based approach and a 
proper agile approach, since there was only one main analysis and design window, but many 
iterations of the project. 
This simulation was designed to mimic the paper Bera et al. (2023) Our aim was to reproduce 
their results by following their methods. Another of the aims is to determine if their results 
are correct for the methods they provided. They did not have a code review in their paper, 
which is the primary reason it is not trivial to replicate. Their methods also were often 
ambiguous and did not fully explain many aspects of the calculations and implementations 
they used in their paper. Regardless this is important as we did not need to research the topic 
in detail to construct our model’s design. 

## **Appendix A: User manual**
## **Makefile:** 
There is a makefile that can run any of the 3 modules. The commands for each module are 
described in their separate sections. 

## **Using the Simulation:**
The simulation is the main set of calculations within the program. This Simulates the growth 
of a bacterial colony with a biofilm starting with a single bacterium inoculated at the centre of 
the simulation grid.  
To run the simulation, you need a configuration file. A sample file: “SimulationConfig.txt” is 
given. 
There are 3 ways to run the simulation: - - - 
Makefile (recommended method): run ‘make all’ to compile all files, then 
Use the command format 
<make run ARGS="simulationConfig.txt simulation_output.txt"> 
Swap out simulationConfig.txt for your input file, and simulation_output.txt for your 
output file. You can also just use “make run” which will use the 2 files stated above as 
defaults. 
Command line: after running ‘make’ you can use 
“java -cp bin Simulation.Simulation” instead of the makefile. This method does not 
let you choose your input and output files, however. 
IDE (not recommended): If you open the simulation in your IDE, make sure your 
terminal is in the ‘biofilm-main’ directory. Then you can run the main method of 
Simulation.java, and it will use simulationConfig.txt as its input, so you will need to 
edit that if you use this method. 
The full file format is below. The only simulation parameters worth changing in a practical 
scenario are: maxAgents, Motility, and the ElasticModuluses. Others can be changed for 
testing purposes when using the program too. 
Here is the format, and an example (This is also in the files with the code): 

**Format:**  
(the words before a ‘#’ are the names for the values, the words after a ‘#’ are comments about 
sections of parameters or whether the number is an integer or float, and what the number 
represents. Note that sometimes the comments are too long for this document and flow onto 
the next line) 
origin_x, origin_y #int, int: starting position of the first bacterium (recommended: 0, 0) 
#simulation values 
max_agents #int: max number of particles – the simulation stops once this is reached 
(recommended: 10000 - 100000) 
output_interval #int: how often (in in-simulation hours) output is sent to the output file 
(recommended: 0.1) 
 
#medium values 
simulation_boundary #int, distance from (0;0) to the edge of the simulation grid – make 
this big enough that no particle reaches the edge ideally (recommended: 100-1000) 
initial_nutrient_concentration #float, starting nutrients per um^2. (recommended: ~10) 
consumption_rate #float, rate that a bacteria consumes nutrients (recommended: ~4) 
diffusion_rate #float, rate that nutrients diffuse to nearby gridsquares (recommended: 
~300) 
 
#EPS values 
EPS_size #float, diameter of EPS in um (recommended: 0.5 - 1) 
EPS_EPS_Elastic_Modulus #float, constant that increases repulsive forces between 2 EPS, 
in Pa (recommended: 20000 - 70000) 
 
#bacteria values 
bacteria_lmax #float, maximum length of a bacterial cell in um (recommended: 5) 
bacteria_diameter #float, diameter of bacteria in um (recommended: 1) 
bacteria_motility #float, motility force of bacteria in Pa*um^2 (recommended: 0 - 1100) 
bacteria_growth_rate #float, bacteria growth rate in um/hr (recommended: ~3.5) 
bacteria_reproduction_rate #float, bacteria reproduction rate, in bacteria/hr (recommended: 
1) 
bacteria_EPS_production_rate #float, EPS production rate, in EPS/hr (recommended: 1) 
Bacteria_EPS_Elastic_Modulus #float, constant that increases repulsive forces between an 
EPS and a Bacteria, in Pa (recommended: 20000 - 70000) 
 
Bacteria_Bacteria_Elastic_Modulus #float, constant that increases repulsive forces 
between 2 Bacteria, in Pa (recommended: 20000 - 70000) 
Friction #float, constant that decreases all movement (recommended: 200) 
EPS_Production_Min_Cell_Density #minimum bacteria density required to produce EPS, 
in um^2 of bacteria/um^2 (recommended: 5) 
EPS_Production_Max_EPS_Density #maximum eps density before bacteria stop 
producing eps, in um^2 of eps/um^2 (recommended: 0.3)
 
**Example:** 
0, 0 
 
1000 
0.1 
 
500 
10 
4 
300 
 
0.5 
40000 
5 
1 
100 
3.5 
1 
1 
40000 
70000 
200 
5 
0.3 

## **Using the graphing module** 
The graphing module allows the user to input the names of multiple simulation output files 
and reproduces figure from the paper using these files. Similarly to the simulation module, 
there are 3 ways to run the simulation: - - - 
Makefile (recommended): run “make all” to compile all files, then use the command “make 
graphing” to start the Graphing Module. 
Command Line: python3 GraphingModule.py 
IDE (not recommended): run GraphingModule.py in your IDE terminal, ensuring that the 
terminal is in the “biofilm-main” directory. If your desired input files are in the same 
directory, the file will run as with the makefile. 
After starting the graphing module, you are given the option to upload your files by manually typing 
the file names into a text box, or by browsing through and selecting files from your file browser. After 
the files have been selected, pressing “Load Simulations” button will read in the data from each file. 
After this, you are able to select from five possible graphical outputs. 
The input files are the files generated by the simulation module, and take on the same format as 
specified above. Note that the graphing module works best with multiple simulation output files 
where various parameters in the config file have been changed. 


## **Using the visualization module** 
The visualization is the section of the program which allows users to view the results of a 
given simulation. This provides a visual depiction of the simulated growth of the bacterial 
colony, as generated in the simulation module. When run, it allows the user to adjust several 
parameters which allow the user to view the simulation as they desire, to highlight key 
aspects of the simulation if needed. 
To run the visualization, you need a configuration file in the same format as is produced by 
the Simulation module. A sample file: “SimulationOutput.txt” is given. 
There are 3 ways to run the visualization: - 
Makefile (recommended method): run ‘make all’ to compile all files, then 
Use the command “make visualization” which will start the module and direct you to 
the options menu - Command line - IDE: If you open the visualization in your IDE, make sure your terminal is in the 
‘biofilm-main’ directory. Then you can run the main method of Visualization.java, and 
it will run as with the Makefile, giving a list of settings to adjust before viewing the 
simulation results. 
 
Here is the format, and an example: 
**Format:** 
#simulation values 
max_agents #int: max number of particles – the simulation stops once this is reached 
output_interval #int: how often (in in-simulation hours) output is sent to the output file 
 
#EPS values 
EPS_size #float, diameter of EPS in um 
EPS_EPS_Elastic_Modulus #float, constant that increases repulsive forces between 2 EPS, 
in Pa 
 
#bacteria values 
bacteria_lmax #float, maximum length of a bacterial cell in um 
bacteria_diameter #float, diameter of bacteria in um 
bacteria_motility #float, motility force of bacteria in Pa*um^2 
bacteria_growth_rate #float, bacteria growth rate in um/hr 
bacteria_reproduction_rate #float, bacteria reproduction rate, in bacteria/hr 
bacteria_EPS_production_rate #float, EPS production rate, in EPS/hr 
Bacteria_EPS_Elastic_Modulus #float, constant that increases repulsive forces between an 
EPS and a Bacteria, in Pa 
Bacteria_Bacteria_Elastic_Modulus #float, constant that increases repulsive forces 
between 2 Bacteria, in Pa 
Friction #float, constant that decreases all movement 
EPS_Production_Min_Cell_Density #minimum bacteria density required to produce EPS, 
in bacteria/um^2 
EPS_Production_Max_EPS_Density #maximum eps density before bacteria stop 
producing eps, in eps/um^2 
 
#medium values 
simulation_boundary #int, distance from (0;0) to the edge of the simulation grid – make 
this big enough that no particle reaches the edge ideally 
initial_nutrient_concentration #float, starting nutrients per um^2. 
consumption_rate #float, rate that a bacteria consumes nutrients 
diffusion_rate #float, rate that nutrients diffuse to nearby gridsquares 
 
#Output at each time point 
time_point #int, the current time point for which the following data applies 
ticks_since_start #int the time in ticks (360*simulation hours) since the simulation began 
colony_centreX, colony_centreY #int, int  
colony_diameter #float  
roughness #float 
sparseness #float 
Bacteria: #Title to indicate the start of the Bacteria details and to increase readability 
ID, x, y, xFacing, yFacing, length, clusterNumber #For each bacterium (int, int, int, float, 
float, float, int) 
EPS: #Title to indicate the start of the EPS details 
ID, x, y, clusterNumber #For each eps (int, int, int, int) 
Bacterial Clusters: #Title to indicate the start of the Bacteria cluster details 
ID, numberOfParticles #For each cluster (int, int) 
EPS Clusters: #Title to indicate the start of the EPS cluster details 
ID, numberOfParticles #For each cluster (int, int) 
 
**Example:** 
10000 
0.1 
 
200 
3.0 
4.0 
300.0 
 
0.5 
40000.0 
 
5.0 
1.0 
500.0 
3.5 
1.0 
360.0 
40000.0 
70000.0 
200.0 
5.0 
0.3 
 
0 
0 
0.0, 0.0 
1.0 
0.0 
0.0 
Bacteria: 
0, 0, 0, -0.6628665521644692, 0.7487375601781903, 1.0, 0 
EPS: 
Bacterial Clusters: 
0, 1 
EPS Clusters: 
 
1 
36 -2.0, 2.0 
4.444131728146501 
0.0 
0.0 
Bacteria: 
0, -2, 2, -0.6628665521644692, 0.7487375601781903, 4.444131728146501, 0 
EPS: 
0, 0, 0, 0 
Bacterial Clusters: 
0, 1 
EPS Clusters: 
0, 1 
 
2 
72 -3.0, 3.0 
7.594131728146498 
0.0 
0.0 
Bacteria: 
0, -3, 3, -0.6628665521644692, 0.7487375601781903, 7.594131728146498, 0 
EPS: 
0, 0, 0, 0 
Bacterial Clusters: 
0, 1 
EPS Clusters: 
0, 1 
 
3 
108 -3.0, 3.0 
7.1718264127284606 
0.0 
0.9953379953379954 
Bacteria: 
0, -74, 4, -0.6628665521644692, 0.7487375601781903, 7.256993321505826, 0 
1, 68, 2, 0.6869828175375697, -0.7266736601860164, 7.086659503951095, 0 
EPS: 
0, 0, 0, 0 
1, -1, 3, 0 
Bacterial Clusters: 
0, 1 
1, 1 
EPS Clusters: 
0, 2
