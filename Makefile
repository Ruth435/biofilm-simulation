# Compiler
JAVAC = javac
JAVA = java
JAVADOC = Javadoc
GPH_DIR = Graphing

ifeq ($(OS), Windows_NT)
	PYTHON = py
else
	PYTHON = python3
endif

# Directories
SRC_DIRS = Simulation Visualization
BIN_DIR = bin
DOC_DIR = doc

# Find all .java files
SOURCES := $(foreach dir,$(SRC_DIRS),$(wildcard $(dir)/*.java))

# Default (compile everything)
all:
	$(JAVAC) -d $(BIN_DIR) Simulation/*.java
	$(JAVAC) -d $(BIN_DIR) -cp $(BIN_DIR) Visualization/*.java

# Javadoc simulation and visualization modules
javadoc:
	$(JAVADOC) -d $(DOC_DIR) $(SOURCES)

# Clean bin and docs
clean:
	@rm -rf $(BIN_DIR) $(DOC_DIR)

# Run simulation (Args are optional - there is a default input and output)
# (ARGS are arg1: InputFilename, arg2: OutputFilename)
# Defaults are: simulationConfigExample.txt and simulation_output.txt
run:
	$(JAVA) -cp $(BIN_DIR) Simulation.Simulation $(ARGS)

# Run the visualization module
visualization:
	$(JAVA) -cp $(BIN_DIR) Visualization

# Run the graphing module
.PHONY: graphing
graphing:
	sudo apt-get install python3-tk
	sudo apt-get install python3-matplotlib
	$(PYTHON) $(GPH_DIR)/GraphingModule.py

.PHONY: visualization all clean javadoc run
