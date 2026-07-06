import unittest
from unittest.mock import patch, mock_open
import tkinter as tk
from GraphingModule import GraphingModule  # replace with actual filename

class TestGraphingModule(unittest.TestCase):
    def setUp(self):
        # Create a hidden Tkinter root
        self.root = tk.Tk()
        self.root.withdraw()
        self.app = GraphingModule(self.root)

    def tearDown(self):
        # Update idle tasks before destroying to avoid Tkinter event errors
        self.root.update_idletasks()
        self.root.destroy()

    def test_clear_all_resets_state(self):
        self.app.simulations = [1, 2, 3]
        self.app.file_names = ["file1.txt"]
        self.app.selected_files_var.set("some file")
        self.app.file_text.insert("1.0", "file.txt")
        self.app.loaded_files_text.insert("1.0", "loaded")
        self.app.graph_var.set("eps_clusters")
        self.app.top_n_var.set("100")

        self.app.clear_all()

        self.assertEqual(self.app.simulations, [])
        self.assertEqual(self.app.file_names, [])
        self.assertEqual(self.app.selected_files_var.get(), "No files selected")
        self.assertEqual(self.app.file_text.get("1.0", tk.END).strip(), "")
        self.assertEqual(self.app.loaded_files_text.get("1.0", tk.END).strip(), "")
        self.assertEqual(self.app.graph_var.get(), "")
        self.assertEqual(self.app.top_n_var.get(), "50")

    @patch("os.path.isfile", return_value=True)
    @patch("builtins.open", new_callable=mock_open, read_data="""\
10
1
           
100
1
0.1
0.01
           
5
1
           
2
1        
1
0.5
0.1
0.01
1
1
1
1
1

1        
1
1
0.1
0.05
0.2
Bacteria:
0,1,2,0,1,2,0
EPS:
0,5,5,0
Bacterial Clusters:
0,1
EPS Clusters:
0,1
""")
    def test_load_simulation_data(self, mock_isfile, mock_file):
        data = self.app.load_simulation_data("fake_file.txt")
        # Check top-level keys
        for key in ["simulation_values", "medium_values", "eps_values", "bacteria_values", "timepoints"]:
            self.assertIn(key, data)
        self.assertEqual(len(data["timepoints"]), 1)

        tp = data["timepoints"][0]
        # Ensure all relevant sub-keys exist and are lists
        self.assertIsInstance(tp.get("bacteria_values", []), list)
        self.assertIsInstance(tp.get("eps", []), list)
        self.assertIsInstance(tp.get("bacterial_clusters", []), list)
        self.assertIsInstance(tp.get("eps_clusters", []), list)

        # Basic length checks
        self.assertGreaterEqual(len(tp["bacteria_values"]), 1)
        self.assertGreaterEqual(len(tp["eps_particles"]), 1)
        self.assertGreaterEqual(len(tp["bacterial_clusters"]), 1)
        self.assertGreaterEqual(len(tp["eps_clusters"]), 1)

    @patch("matplotlib.pyplot.show")
    def test_plot_eps_clusters_runs_without_error(self, mock_show):
        # Provide minimal dummy simulation data
        self.app.simulations = [
            {
                "bacteria_values": {
                    "Bacteria_Bacteria_Elastic_Modulus": 10,
                    "Bacteria_EPS_Elastic_Modulus": 5,
                    "bacteria_motility": 1
                },
                "timepoints": [
                    {"eps_clusters":[{"ID":0,"numberOfParticles":10}], "roughness":0, "sparseness":0}
                ]
            }
        ]
        # Should not raise error
        self.app.plot_eps_clusters(self.app.simulations, top_n=5)
        mock_show.assert_called_once()

    def test_get_debug_info_returns_string(self):
        # Minimal simulation data
        self.app.simulations = [
            {
                "bacteria_values": {
                    "Bacteria_Bacteria_Elastic_Modulus": 10,
                    "Bacteria_EPS_Elastic_Modulus": 5,
                    "bacteria_motility": 1
                },
                "timepoints": [
                    {
                        "bacteria":[1],
                        "eps":[1],
                        "bacteria_clusters": [{"ID":0,"numberOfParticles":5}],
                        "eps_clusters": [{"ID":0,"numberOfParticles":10}],
                        "roughness": 0.1,
                        "sparseness": 0.2
                    }
                ]
            }
        ]
        self.app.file_names = ["file1.txt"]
        debug_info = self.app.get_debug_info()
        self.assertIn("Simulation 1", debug_info)
        self.assertIsInstance(debug_info, str)

if __name__ == "__main__":
    unittest.main()
