import tkinter as tk
from tkinter import ttk, filedialog, messagebox, scrolledtext
import os
from matplotlib import pyplot as plt
from matplotlib.lines import Line2D
import numpy as np
import matplotlib
matplotlib.use('TkAgg')
import threading

class GraphingModule:
    def __init__(self, root):
        self.root = root
        self.root.title("Graphing Module")
        self.root.geometry("800x700")
        
        # Store loaded simulation data
        self.simulations = []
        self.file_names = []
        
        self.setup_ui()
    
    def setup_ui(self):
        # Main frame
        main_frame = ttk.Frame(self.root, padding="10")
        main_frame.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        # Configure grid weights
        self.root.columnconfigure(0, weight=1)
        self.root.rowconfigure(0, weight=1)
        main_frame.columnconfigure(1, weight=1)
        
        # Title
        title_label = ttk.Label(main_frame, text="EPS Particle Simulation Analysis", 
                               font=("Arial", 16, "bold"))
        title_label.grid(row=0, column=0, columnspan=3, pady=(0, 20))
        
        # File loading section
        file_frame = ttk.LabelFrame(main_frame, text="Load Simulation Files", padding="10")
        file_frame.grid(row=1, column=0, columnspan=3, sticky=(tk.W, tk.E), pady=(0, 10))
        file_frame.columnconfigure(1, weight=1)
        
        # File input methods
        ttk.Label(file_frame, text="Method:").grid(row=0, column=0, sticky=tk.W)
        self.input_method = tk.StringVar(value="browser")
        ttk.Radiobutton(file_frame, text="File Browser", variable=self.input_method, 
                       value="browser").grid(row=0, column=1, sticky=tk.W)
        ttk.Radiobutton(file_frame, text="Manual Input", variable=self.input_method, 
                       value="manual").grid(row=0, column=2, sticky=tk.W)
        
        # File browser section
        browser_frame = ttk.Frame(file_frame)
        browser_frame.grid(row=1, column=0, columnspan=3, sticky=(tk.W, tk.E), pady=(10, 0))
        browser_frame.columnconfigure(1, weight=1)
        
        ttk.Button(browser_frame, text="Select Files", 
                  command=self.select_files).grid(row=0, column=0, padx=(0, 10))
        
        self.selected_files_var = tk.StringVar(value="No files selected")
        ttk.Label(browser_frame, textvariable=self.selected_files_var, 
                 foreground="gray").grid(row=0, column=1, sticky=tk.W)
        
        # Manual input section
        manual_frame = ttk.Frame(file_frame)
        manual_frame.grid(row=2, column=0, columnspan=3, sticky=(tk.W, tk.E), pady=(10, 0))
        manual_frame.columnconfigure(0, weight=1)
        
        ttk.Label(manual_frame, text="Enter file names (one per line):").grid(row=0, column=0, sticky=tk.W)
        self.file_text = scrolledtext.ScrolledText(manual_frame, height=4, width=60)
        self.file_text.grid(row=1, column=0, sticky=(tk.W, tk.E), pady=(5, 0))
        
        # Load button
        ttk.Button(file_frame, text="Load Simulations", 
                  command=self.load_simulations).grid(row=3, column=0, columnspan=3, pady=(10, 0))
        
        # Status section
        status_frame = ttk.LabelFrame(main_frame, text="Status", padding="10")
        status_frame.grid(row=2, column=0, columnspan=3, sticky=(tk.W, tk.E), pady=(0, 10))
        status_frame.columnconfigure(0, weight=1)
        
        self.status_var = tk.StringVar(value="Ready to load simulations")
        self.status_label = ttk.Label(status_frame, textvariable=self.status_var)
        self.status_label.grid(row=0, column=0, sticky=tk.W)
        
        # Loaded files display
        self.loaded_files_text = scrolledtext.ScrolledText(status_frame, height=3, width=60)
        self.loaded_files_text.grid(row=1, column=0, sticky=(tk.W, tk.E), pady=(5, 0))
        
        # Graph selection section
        graph_frame = ttk.LabelFrame(main_frame, text="Select Graph to Generate", padding="10")
        graph_frame.grid(row=3, column=0, columnspan=3, sticky=(tk.W, tk.E), pady=(0, 10))
        
        self.graph_var = tk.StringVar()
        graphs = [
            ("EPS Cluster Size Distribution", "eps_clusters"),
            ("EPS Phase Diagram", "eps_phase"),
            ("Bacterial Cluster Size Distribution", "bacterial_clusters"),
            ("Temporal Roughness Evolution", "roughness_evolution"),
            ("Final Roughness and Sparseness", "colony_morphology"),
        ]
        
        for i, (text, value) in enumerate(graphs):
            ttk.Radiobutton(graph_frame, text=text, variable=self.graph_var, 
                           value=value).grid(row=i//2, column=i%2, sticky=tk.W, padx=(0, 20))
        
        # Options section
        options_frame = ttk.LabelFrame(main_frame, text="Graph Options", padding="10")
        options_frame.grid(row=4, column=0, columnspan=3, sticky=(tk.W, tk.E), pady=(0, 10))
        
        ttk.Label(options_frame, text="Top N clusters:").grid(row=0, column=0, sticky=tk.W)
        self.top_n_var = tk.StringVar(value="50")
        top_n_entry = ttk.Entry(options_frame, textvariable=self.top_n_var, width=10)
        top_n_entry.grid(row=0, column=1, sticky=tk.W, padx=(10, 0))
        
        # Action buttons
        button_frame = ttk.Frame(main_frame)
        button_frame.grid(row=5, column=0, columnspan=3, pady=(10, 0))
        
        ttk.Button(button_frame, text="Generate Graph", 
                  command=self.generate_graph).pack(side=tk.LEFT, padx=(0, 10))
        
        ttk.Button(button_frame, text="Debug Data", 
                  command=self.debug_data).pack(side=tk.LEFT, padx=(0, 10))
        
        ttk.Button(button_frame, text="Clear All", 
                  command=self.clear_all).pack(side=tk.LEFT)
    
    def select_files(self):
        files = filedialog.askopenfilenames(
            title="Select simulation files",
            filetypes=[("Text files", "*.txt"), ("All files", "*.*")]
        )
        if files:
            self.selected_files_var.set(f"{len(files)} files selected")
            # Store the selected files for loading
            self.selected_file_paths = files
        else:
            self.selected_files_var.set("No files selected")
            self.selected_file_paths = []
    
    def load_simulations(self):
        try:
            self.simulations = []
            self.file_names = []
            
            # Get file names based on input method
            if self.input_method.get() == "browser":
                if not hasattr(self, 'selected_file_paths') or not self.selected_file_paths:
                    messagebox.showerror("Error", "Please select files first")
                    return
                file_paths = self.selected_file_paths
            else:
                file_names = [name.strip() for name in self.file_text.get("1.0", tk.END).strip().split("\n") if name.strip()]
                if not file_names:
                    messagebox.showerror("Error", "Please enter file names")
                    return
                file_paths = file_names
            
            self.status_var.set("Loading simulations...")
            self.root.update()
            
            # Load each file
            loaded_files = []
            for file_path in file_paths:
                try:
                    if self.input_method.get() == "browser":
                        filename = os.path.basename(file_path)
                    else:
                        filename = file_path
                    
                    file_paths = [fp for fp in file_paths if fp.endswith('.txt')]
                    if not file_paths:
                        messagebox.showerror("Error", "No valid .txt simulation files selected")

                    data = self.load_simulation_data(file_path)
                    self.simulations.append(data)
                    self.file_names.append(filename)
                    loaded_files.append(f"✓ {filename}")
                    
                except Exception as e:
                    loaded_files.append(f"✗ {filename}: {str(e)}")
            
            # Update status
            self.status_var.set(f"Loaded {len(self.simulations)} simulations successfully")
            self.loaded_files_text.delete("1.0", tk.END)
            self.loaded_files_text.insert("1.0", "\n".join(loaded_files))
            
            if len(self.simulations) == 0:
                messagebox.showerror("Error", "No simulations were loaded successfully")
        
        except Exception as e:
            messagebox.showerror("Error", f"Error loading simulations: {str(e)}")
            self.status_var.set("Error loading simulations")
    
    def generate_graph(self):
        if not self.simulations:
            messagebox.showerror("Error", "Please load simulations first")
            return
        
        if not self.graph_var.get():
            messagebox.showerror("Error", "Please select a graph type")
            return
        
        try:
            self.status_var.set("Generating graph...")
            self.root.update()
            
            # Run graph generation in a separate thread to prevent GUI freezing
            thread = threading.Thread(target=self._generate_graph_thread)
            thread.daemon = True
            thread.start()
        
        except Exception as e:
            messagebox.showerror("Error", f"Error generating graph: {str(e)}")
            self.status_var.set("Error generating graph")
    
    def _generate_graph_thread(self):
        try:
            graph_type = self.graph_var.get()
            top_n = int(self.top_n_var.get()) if self.top_n_var.get().isdigit() else 50

            # Schedule plotting on the main thread
            def plot_on_main():
                try:
                    if graph_type == "eps_clusters":
                        self.plot_eps_clusters(self.simulations, top_n)
                    elif graph_type == "eps_phase":
                        self.plot_eps_phase_diagram(self.simulations)
                    elif graph_type == "bacterial_clusters":
                        self.plot_bacterial_clusters(self.simulations, top_n)
                    elif graph_type == "roughness_evolution":
                        self.plot_roughness_evolution(self.simulations)
                    elif graph_type == "colony_morphology":
                        self.plot_colony_morphology(self.simulations)
                    self.status_var.set("Graph generated successfully")
                except Exception as e:
                    messagebox.showerror("Error", f"Error generating graph: {str(e)}")
                    self.status_var.set("Error generating graph")

            self.root.after(0, plot_on_main)

        except Exception as e:
            self.root.after(0, lambda: messagebox.showerror("Error", f"Error generating graph: {str(e)}"))
            self.root.after(0, lambda: self.status_var.set("Error generating graph"))
    
    def debug_data(self):
        if not self.simulations:
            messagebox.showerror("Error", "Please load simulations first")
            return
        
        try:
            debug_window = tk.Toplevel(self.root)
            debug_window.title("Debug Information")
            debug_window.geometry("600x400")
            
            debug_text = scrolledtext.ScrolledText(debug_window, wrap=tk.WORD)
            debug_text.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
            
            debug_info = self.get_debug_info()
            debug_text.insert("1.0", debug_info)
        
        except Exception as e:
            messagebox.showerror("Error", f"Error generating debug info: {str(e)}")
    
    def clear_all(self):
        self.simulations = []
        self.file_names = []
        self.selected_files_var.set("No files selected")
        self.file_text.delete("1.0", tk.END)
        self.loaded_files_text.delete("1.0", tk.END)
        self.status_var.set("Ready to load simulations")
        self.graph_var.set("")
        self.top_n_var.set("50")
    
    def get_debug_info(self):
        debug_info = "=== DEBUGGING SIMULATION DATA ===\n\n"

        for i, data in enumerate(self.simulations):
            debug_info += f"Simulation {i+1} ({self.file_names[i]}):\n"
            debug_info += f"  Motility: {data['bacteria_values']['bacteria_motility']}\n"
            debug_info += f"  Cell-Cell Elastic: {data['bacteria_values']['Bacteria_Bacteria_Elastic_Modulus']}\n"
            debug_info += f"  Cell-EPS Elastic: {data['bacteria_values']['Bacteria_EPS_Elastic_Modulus']}\n"
            debug_info += f"  Number of timepoints: {len(data['timepoints'])}\n"

            if data['timepoints']:
                final_tp = data['timepoints'][-1]
                debug_info += f"  Final timepoint:\n"

                # Use get() with defaults to avoid KeyError
                eps_list = final_tp.get('eps', [])
                bacteria_list = final_tp.get('bacteria', [])
                eps_clusters = final_tp.get('EPS_clusters', [])
                bacterial_clusters = final_tp.get('bacterial_clusters', [])

                debug_info += f"    EPS clusters: {len(eps_list)}\n"
                debug_info += f"    Bacterial clusters: {len(bacterial_clusters)}\n"
                debug_info += f"    Bacteria count: {len(bacteria_list)}\n"
                debug_info += f"    EPS count: {len(eps_list)}\n"

                if eps_clusters:
                    cluster_sizes = [c['numberOfParticles'] for c in eps_clusters]
                    debug_info += f"    EPS cluster sizes: min={min(cluster_sizes)}, max={max(cluster_sizes)}, mean={np.mean(cluster_sizes):.1f}\n"
                    debug_info += f"    Top 5 EPS cluster sizes: {sorted(cluster_sizes, reverse=True)[:5]}\n"

                if bacterial_clusters:
                    cluster_sizes = [c['numberOfParticles'] for c in bacterial_clusters]
                    debug_info += f"    Bacterial cluster sizes: min={min(cluster_sizes)}, max={max(cluster_sizes)}, mean={np.mean(cluster_sizes):.1f}\n"
                    debug_info += f"    Top 5 bacterial cluster sizes: {sorted(cluster_sizes, reverse=True)[:5]}\n"

                debug_info += f"    Final roughness: {final_tp.get('roughness', 0)}\n"
                debug_info += f"    Final sparseness: {final_tp.get('sparseness', 0)}\n"

            debug_info += "\n"

        return debug_info
    
    # Include all the plotting functions here (load_simulation_data and all plot functions)
    def load_simulation_data(self, filename):

        if not os.path.isfile(filename):
            raise FileNotFoundError(f"File not found: {filename}")
        
        data = {
        "simulation_values": {},
        "medium_values": {},
        "eps_values": {},
        "bacteria_values": {},
        "timepoints": []
        }

        with open(filename, "r") as f:
            lines = [line.strip() for line in f if line.strip()]

        if not lines:
            raise ValueError("File is empty or improperly formatted")

        i = 0

        try:
            # --- Simulation values ---
            data["simulation_values"]["max_agents"] = int(lines[i]); i += 1
            data["simulation_values"]["output_interval"] = float(lines[i]); i += 1

            # --- Medium values ---
            data["medium_values"]["simulation_boundary"] = float(lines[i]); i += 1
            data["medium_values"]["initial_nutrient_concentration"] = float(lines[i]); i += 1
            data["medium_values"]["consumption_rate"] = float(lines[i]); i += 1
            data["medium_values"]["diffusion_rate"] = float(lines[i]); i += 1

            # --- EPS values ---
            data["eps_values"]["EPS_size"] = float(lines[i]); i += 1
            data["eps_values"]["EPS_EPS_Elastic_Modulus"] = float(lines[i]); i += 1

            # --- Bacteria values ---
            bacteria_keys = [
                "bacteria_lmax", "bacteria_diameter", "bacteria_motility",
                "bacteria_growth_rate", "bacteria_reproduction_rate",
                "bacteria_EPS_production_rate", "Bacteria_EPS_Elastic_Modulus",
                "Bacteria_Bacteria_Elastic_Modulus", "Friction",
                "EPS_Production_Min_Cell_Density", "EPS_Production_Max_EPS_Density"
            ]
            for key in bacteria_keys:
                data["bacteria_values"][key] = float(lines[i]) if '.' in lines[i] else int(lines[i])
                i += 1

            # --- Timepoints ---
            while i < len(lines):
                tp = {}
                tp["time_point"] = int(lines[i]); i += 1
                tp["ticks_since_start"] = int(lines[i]); i += 1
                tp["colony_center"] = tuple(map(float, lines[i].split(','))); i += 1
                tp["colony_diameter"] = float(lines[i]); i += 1
                tp["roughness"] = float(lines[i]); i += 1
                tp["sparseness"] = float(lines[i]); i += 1

                # --- Bacteria ---
                if lines[i] != "Bacteria:":
                    raise ValueError(f"Expected 'Bacteria:' at line {i}, got {lines[i]}")
                i += 1
                tp["bacteria_values"] = []
                while i < len(lines) and lines[i] != "EPS:":
                    parts = lines[i].split(',')
                    bacterium = {
                        "ID": int(parts[0]),
                            "x": float(parts[1]),
                        "y": float(parts[2]),
                        "xFacing": float(parts[3]),
                        "yFacing": float(parts[4]),
                        "length": float(parts[5]),
                        "clusterNumber": int(parts[6])
                    }
                    tp["bacteria_values"].append(bacterium)
                    i += 1

                # --- EPS particles ---
                if i < len(lines) and lines[i] == "EPS:":
                    i += 1
                    tp["eps_particles"] = []
                    while i < len(lines) and lines[i] not in ["Bacterial Clusters:", "EPS Clusters:"]:
                        parts = lines[i].split(',')
                        eps = {
                            "ID": int(parts[0]),
                            "x": float(parts[1]),
                            "y": float(parts[2]),
                            "clusterNumber": int(parts[3])
                        }
                        tp["eps_particles"].append(eps)
                        i += 1

                # --- Bacterial Clusters ---
                if i < len(lines) and lines[i] == "Bacterial Clusters:":
                    i += 1
                    tp["bacterial_clusters"] = []
                    while i < len(lines) and lines[i] != "EPS Clusters:":
                        parts = lines[i].split(',')
                        cluster = {
                            "ID": int(parts[0]),
                            "numberOfParticles": int(parts[1])
                        }
                        tp["bacterial_clusters"].append(cluster)
                        i += 1

                # --- EPS Clusters ---
                if i < len(lines) and lines[i] == "EPS Clusters:":
                    i += 1
                    tp["eps_clusters"] = []
                    while i < len(lines) and ',' in lines[i]:
                        parts = lines[i].split(',')
                        cluster = {
                            "ID": int(parts[0]),
                            "numberOfParticles": int(parts[1])
                        }
                        tp["eps_clusters"].append(cluster)
                        i += 1

                data["timepoints"].append(tp)

        except (IndexError, ValueError) as e:
            raise ValueError(f"Error parsing file {filename} at line {i+1}: {str(e)}")

        return data
    
    # Plotting functions
    def plot_eps_clusters(self, simulations, top_n=50):
        """Create Figure 3: EPS cluster size distribution plots"""
        # Group simulations by depletion values
        depletion_groups = {}
        
        for data in simulations:
            cell_cell_elastic = data["bacteria_values"]["Bacteria_Bacteria_Elastic_Modulus"]
            cell_eps_elastic = data["bacteria_values"]["Bacteria_EPS_Elastic_Modulus"]
            depletion_key = f"{int(cell_cell_elastic)}-{int(cell_eps_elastic)}"
            
            if depletion_key not in depletion_groups:
                depletion_groups[depletion_key] = []
            depletion_groups[depletion_key].append(data)
        
        # Create subplots for each depletion condition
        n_groups = len(depletion_groups)
        n_cols = 2
        n_rows = (n_groups+1) // n_cols
        fig, ax_array = plt.subplots(n_rows, n_cols, figsize=(8, 6))
        axes = list(ax_array.flatten())
        
        # Define colors for different motility values
        motility_values = sorted({data["bacteria_values"]["bacteria_motility"] for data in simulations})
        cmap = plt.get_cmap('tab10')
        motility_colors = {mot: cmap(i % 10) for i, mot in enumerate(motility_values)}
        
        plot_idx = 0
        for depletion_key, sims in sorted(depletion_groups.items()):
            if plot_idx >= len(axes):
                break
                
            ax = axes[plot_idx]
            has_data = False
            
            for sim_data in sims:
                final_tp = sim_data["timepoints"][-1]
                eps_clusters = final_tp["eps_clusters"]
                motility = sim_data["bacteria_values"]["bacteria_motility"]
                
                print(f"Processing depletion {depletion_key}, motility {motility}")
                print(f"EPS clusters found: {len(eps_clusters)}")
                
                if eps_clusters and len(eps_clusters) > 0:
                    eps_clusters_sorted = sorted(eps_clusters, key=lambda c: c["numberOfParticles"], reverse=True)[:top_n]
                    
                    x = list(range(len(eps_clusters_sorted)))
                    y = [c["numberOfParticles"] for c in eps_clusters_sorted]

                    color = motility_colors.get(int(motility), 'black')
                    
                    print(f"Plotting {len(y)} points")
                    if len(y) > 0:
                        ax.plot(x, y, color=color, marker='o', markersize=2, linewidth=1.5, 
                               label=f'f_mot = {int(motility)}')
                        has_data = True
            
            ax.set_yscale('log')
            ax.set_ylim(bottom=1)
            ax.set_xlabel('Cluster Identifier')
            ax.set_ylabel('Cluster size')
            ax.set_title(f'({chr(97+plot_idx)}) {depletion_key}', fontsize=12, color='blue')
            ax.grid(True, alpha=0.3)
            ax.set_xlim(0, 50)        
            ax.legend(fontsize=8)
            plot_idx += 1
        
        # Hide unused subplots
        for idx in range(plot_idx, len(axes)):
            axes[idx].set_visible(False)
        
        plt.tight_layout()
        plt.suptitle('First 50 clusters of the cells, in descending size order', fontsize=11, y=0.98)
        plt.subplots_adjust(top=0.9)
        plt.show()
    
    def plot_eps_phase_diagram(self, simulations):
        """Create Figure 4: EPS phase diagram"""
        plt.figure(figsize=(10, 8))
        
        data_points = []
        
        print(f"Processing {len(simulations)} simulations for phase diagram")
        
        for i, data in enumerate(simulations):
            motility = data["bacteria_values"]["bacteria_motility"]
            cell_cell_elastic = data["bacteria_values"]["Bacteria_Bacteria_Elastic_Modulus"]
            cell_eps_elastic = data["bacteria_values"]["Bacteria_EPS_Elastic_Modulus"]
            
            print(f"Sim {i+1}: motility={motility}, cell-cell={cell_cell_elastic}, cell-eps={cell_eps_elastic}")
            
            final_tp = data["timepoints"][-1]
            eps_clusters = final_tp["eps_clusters"]
            
            if not eps_clusters:
                max_cluster_size = 0
                print(f"  No EPS clusters found")
            else:
                max_cluster_size = max(c["numberOfParticles"] for c in eps_clusters)
                print(f"  Max cluster size: {max_cluster_size}")
            
            depletion_value = f"{int(cell_cell_elastic)}-{int(cell_eps_elastic)}"
            
            data_points.append({
                'motility': motility,
                'depletion_str': depletion_value,
                'depletion_num': cell_cell_elastic,
                'max_cluster_size': max_cluster_size
            })
        
        # Determine thresholds for classification
        cluster_sizes = [dp['max_cluster_size'] for dp in data_points if dp['max_cluster_size'] > 0]
        if cluster_sizes and len(cluster_sizes) > 1:
            small_thresh = np.percentile(cluster_sizes, 33)
            moderate_thresh = np.percentile(cluster_sizes, 67)
            print(f"Cluster size thresholds: small<={small_thresh:.1f}, moderate<={moderate_thresh:.1f}")
        else:
            small_thresh = 10
            moderate_thresh = 50
            print(f"Using default thresholds: small<={small_thresh}, moderate<={moderate_thresh}")
        
        color_map = {'small': '#2ca02c', 'moderate': '#ff7f0e', 'large': '#1f77b4'}
        
        plotted_points = {'small': 0, 'moderate': 0, 'large': 0}

        depletion_labels = sorted({dp['depletion_str'] for dp in data_points})
        depletion_to_y = {label: i for i, label in enumerate(depletion_labels)}
        
        for dp in data_points:
            motility = dp['motility']
            depletion_str = dp['depletion_str']
            max_cluster_size = dp['max_cluster_size']
            
            # Classify cluster type
            if max_cluster_size <= small_thresh:
                cluster_type = 'small'
            elif max_cluster_size <= moderate_thresh:
                cluster_type = 'moderate'
            else:
                cluster_type = 'large'
            
            plotted_points[cluster_type] += 1
            print(f"  Plotting: motility={motility}, depletion={depletion_str}, size={max_cluster_size}, type={cluster_type}")
            
            plt.scatter(motility, depletion_to_y[depletion_str], c=color_map[cluster_type], 
                       s=100, alpha=0.8, edgecolors='black', linewidth=1)
        
        print(f"Plotted points: {plotted_points}")
        
        plt.yticks(list(depletion_to_y.values()), list(depletion_to_y.keys()))
        plt.xlabel('Motility', fontsize=14)
        plt.ylabel('Depletion', fontsize=14)
        
        # Create legend
        legend_elements = [
            Line2D([0], [0], marker='o', color='w', label='Large-cluster', 
                   markerfacecolor=color_map['large'], markersize=10, markeredgecolor='black'),
            Line2D([0], [0], marker='o', color='w', label='Moderate-cluster', 
                   markerfacecolor=color_map['moderate'], markersize=10, markeredgecolor='black'),
            Line2D([0], [0], marker='o', color='w', label='Small-cluster', 
                   markerfacecolor=color_map['small'], markersize=10, markeredgecolor='black')
        ]
        plt.legend(handles=legend_elements, loc='center left', bbox_to_anchor=(0.4, 0.5))
        
        plt.grid(True, alpha=0.3)
        plt.title('EPS Phase Diagram', fontsize=11, pad=20)
        plt.tight_layout()
        plt.show()
    
    def plot_bacterial_clusters(self, simulations, top_n=50):
        """
        Create Figure 6: Bacterial cluster size distribution plots for different depletion conditions
        """
        # Group simulations by depletion values
        depletion_groups = {}
    
        for data in simulations:
            cell_cell_elastic = data["bacteria_values"]["Bacteria_Bacteria_Elastic_Modulus"]
            cell_eps_elastic = data["bacteria_values"]["Bacteria_EPS_Elastic_Modulus"]
            depletion_key = f"{int(cell_cell_elastic)}-{int(cell_eps_elastic)}"
            depletion_groups.setdefault(depletion_key, []).append(data)
    
        # Create subplots for different depletion conditions (only show a few)
        n_groups = len(depletion_groups)
        n_cols = 2
        n_rows = (n_groups + 1) // n_cols
        fig, axes = plt.subplots(n_rows, n_cols, figsize=(12, 10))
        axes = axes.flatten()
    
        # Define colors for different motility values
        motility_values = sorted({data["bacteria_values"]["bacteria_motility"] for data in simulations})
        cmap = plt.get_cmap('tab10')
        motility_colors = {mot: cmap(i % 10) for i, mot in enumerate(motility_values)}
    
        # Select specific depletion conditions to match Figure 6 (4-5, 4-6, 4-7)
        target_depletions = list(depletion_groups.keys())[:n_groups]
    
        plot_idx = 0
        for depletion_key in target_depletions:
            if depletion_key in depletion_groups and plot_idx < n_groups:
                sims = depletion_groups[depletion_key]
                ax = axes[plot_idx]
            
                for sim_data in sims:
                    final_tp = sim_data["timepoints"][-1]  # final time point
                    bacterial_clusters = final_tp["bacterial_clusters"]
                    motility = sim_data["bacteria_values"]["bacteria_motility"]
                
                    # If there are bacterial clusters, sort descending by size
                    if bacterial_clusters and len(bacterial_clusters) > 0:
                        bacterial_clusters_sorted = sorted(bacterial_clusters, key=lambda c: c["numberOfParticles"], reverse=True)[:top_n]
                    
                        # X-axis: cluster identifier (0-based index)
                        x = range(len(bacterial_clusters_sorted))
                        y = [c["numberOfParticles"] for c in bacterial_clusters_sorted]
                    
                        # Get color for this motility value
                        color = motility_colors.get(int(motility), 'black')
                    
                        # Plot with appropriate style
                        if len(y) > 0:
                            ax.plot(x, y, color=color, marker='o', markersize=2, linewidth=1.5, 
                            label=f'f_mot = {int(motility)}' if plot_idx == 0 else "")
            
                # Customize subplot
                ax.set_yscale('log')
                ax.set_ylim(bottom=1)
                ax.set_xlabel('Cluster identifier')
                ax.set_ylabel('Cluster size')
                ax.set_title(f'({chr(97+plot_idx)}) {depletion_key}', fontsize=12, color='blue')
                ax.grid(True, alpha=0.3)

                print(f"Depletion: {depletion_key}, Motility: {motility}")
                print(f"Bacterial clusters: {bacterial_clusters}")
                if bacterial_clusters:
                    sorted_clusters = sorted(bacterial_clusters, key=lambda c: c["numberOfParticles"], reverse=True)[:top_n]
                    print(f"Top clusters (numberOfParticles): {[c['numberOfParticles'] for c in sorted_clusters]}")
                else:
                    print("No bacterial clusters found!")
            
                # Add legend only to the first subplot
                if plot_idx == 0:
                    ax.legend(fontsize=8)
            
                plot_idx += 1
    
        plt.tight_layout()
        plt.suptitle('First 50 clusters of the cells, in descending size order', 
                 fontsize=11, y=0.98)
        plt.show()
    
    def plot_roughness_evolution(self, simulations):
        """
        Create Figure 8: Temporal evolution of roughness for different motility and depletion conditions
        """
        # Group simulations by motility
        motility_groups = {}
    
        for data in simulations:
            motility = data["bacteria_values"]["bacteria_motility"]
            motility_groups.setdefault(motility, []).append(data)
    
        # Create subplots for different motility values
        motility_values = sorted(motility_groups.keys())
        n_motilities = min(4, len(motility_values))
        n_cols = 2
        n_rows = (n_motilities + 1) // n_cols
        fig, axes = plt.subplots(n_rows, n_cols, figsize=(12, 10))
        axes = axes.flatten()
    
        # Define colors for different depletion values
        depletion_colors = sorted({f"{int(data['bacteria_values']['Bacteria_Bacteria_Elastic_Modulus'])}-"
                                   f"{int(data['bacteria_values']['Bacteria_EPS_Elastic_Modulus'])}" 
                                   for data in simulations})
        cmap = plt.get_cmap('tab10')
        depletion_colors = {dep: cmap(i % 10) for i, dep in enumerate(depletion_colors)}
    
        for idx, motility in enumerate(motility_values[:4]):
            ax = axes[idx]
            sims = motility_groups[motility]
        
            for sim_data in sims:
                cell_cell_elastic = sim_data["bacteria_values"]["Bacteria_Bacteria_Elastic_Modulus"]
                cell_eps_elastic = sim_data["bacteria_values"]["Bacteria_EPS_Elastic_Modulus"]
                depletion_key = f"{int(cell_cell_elastic)}-{int(cell_eps_elastic)}"
            
                # Extract roughness values over time
                time_points = []
                roughness_values = []
            
                for tp_idx, tp in enumerate(sim_data["timepoints"]):
                    # Scale time from 0 to 1
                    scaled_time = tp_idx / (len(sim_data["timepoints"]) - 1) if len(sim_data["timepoints"]) > 1 else 0
                    time_points.append(scaled_time)
                    roughness_values.append(tp["roughness"])
            
                # Get color for this depletion value
                color = depletion_colors.get(depletion_key, 'black')
            
                # Plot with error bars (simulated - you'd need multiple runs for real error bars)
                ax.plot(time_points, roughness_values, color=color, marker='o', 
                   markersize=2, linewidth=1.5, 
                   label=depletion_key if idx == 0 else "")
            
                # Add simulated error bars for demonstration
                if len(time_points) > 10:  # Only add error bars if we have enough points
                    error_indices = range(0, len(time_points), max(1, len(time_points)//10))
                    error_x = [time_points[i] for i in error_indices]
                    error_y = [roughness_values[i] for i in error_indices]
                    error_bars = [abs(y * 0.1) for y in error_y]  # Simulate 10% error
                    ax.errorbar(error_x, error_y, yerr=error_bars, fmt='none', 
                           color=color, alpha=0.3, capsize=2)
        
            # Customize subplot
            ax.set_xlabel('Scaled time')
            ax.set_ylabel('Roughness(σᵣ)')
            ax.set_title(f'({chr(97+idx)}) f_mot = {int(motility)}', fontsize=12)
            ax.grid(True, alpha=0.3)
            ax.set_xlim(0, 1)
        
            # Add legend only to the first subplot
            if idx == 0:
                ax.legend(fontsize=8)
    
        plt.tight_layout()
        plt.suptitle('Temporal evolution of roughness as a function of time', 
                 fontsize=11, y=0.98)
        plt.subplots_adjust(top=0.9)
        plt.show()
    
    def plot_colony_morphology(self, simulations):
        """
        Create Figure 11: roughness/sparseness evolution
        """
        # This function creates a simplified version since we don't have actual colony images
        fig, (roughness_ax, sparseness_ax) = plt.subplots(1, 2, figsize=(14, 6))

        # Define colors for different initial nutrient concentrations (mock values)
        nutrient_concentrations = sorted({data["medium_values"]["initial_nutrient_concentration"] for data in simulations})
        cmap = plt.get_cmap('tab10')
        colors_conc = {conc: cmap(idx%10) for idx, conc in enumerate(nutrient_concentrations)}

        for sim in simulations:
            nutrient_conc = sim["medium_values"]["initial_nutrient_concentration"]
            color = colors_conc.get(nutrient_conc, 'black')

            # Scale time 0-1
            time_points = [tp_idx / (len(sim["timepoints"]) - 1) if len(sim["timepoints"]) > 1 else 0
                       for tp_idx, tp in enumerate(sim["timepoints"])]

            roughness_values = [tp["roughness"] for tp in sim["timepoints"]]
            sparseness_values = [tp["sparseness"] for tp in sim["timepoints"]]

            # Plot roughness
            roughness_ax.plot(time_points, roughness_values, color=color, marker='o',
                          markersize=2, linewidth=1.5, label=f'C₀ = {nutrient_conc}')

            # Plot sparseness
            sparseness_ax.plot(time_points, sparseness_values, color=color, marker='o',
                           markersize=2, linewidth=1.5)

        # Customize plots
        roughness_ax.set_xlabel('Scaled time')
        roughness_ax.set_ylabel('Roughness (σᵣ)')
        roughness_ax.set_title('Roughness evolution', fontsize=12)
        roughness_ax.grid(True, alpha=0.3)
        roughness_ax.legend(fontsize=8)

        sparseness_ax.set_xlabel('Scaled time')
        sparseness_ax.set_ylabel('Sparseness (Sᵣ)')
        sparseness_ax.set_title('Sparseness evolution', fontsize=12)
        sparseness_ax.grid(True, alpha=0.3)

        plt.suptitle(
        'Temporal evolution of roughness and sparseness for non-motile colonies',
        fontsize=11, y=1.02
    )
        plt.tight_layout()
        plt.show()
    
if __name__ == "__main__":
    root = tk.Tk()
    app = GraphingModule(root)
    root.mainloop()