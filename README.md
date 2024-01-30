# Process Comparison Using Petri Net Decomposition
This repository hosts the code for a ProM plugin that should be available in [ProM's nightly builds](https://promtools.org/prom-6-nightly-builds/).
Having downloaded the nightly build, you should be able to install the package via the package manager.
After installing the package, variants of the `Decompose Petri net for Process Comparison (RPST-based)` plugin should become available.

The plugin requires two event logs and a Petri net as input.
It then asks you to select a measurement with respect to which you want to compare the two process variants. 
Moreover, you can decide whether the plugin should attempt to simplify the Petri net in order to improve the RPST-based decomposition.

## Measurements
The current version of the plugin already provides a few measurement functions. 
Given an alignment and a subprocess, a measurement function extracts real-valued features from the alignment.
These features might, for example, assess the control flow or performance dimension of a process variant.
Ultimately, we use hypothesis tests to detect differences between feature populations extracted for the two process variants provided.

The current measurement framework facilitates the implementation of new measurements. 
You'll only have to add a new measurement class to the measurements package and add it to the list of measurements. 

## Decomposition
Despite the measurements are mostly agnostic to the decomposition applied, the decomposition strongly affects the diagnostics obtained.
In the current implementation, the decomposition is based on the Refined Process Structure Tree (RPST) of the Petri net.
Moreover, you can select whether an additional preprocessing procedure should be applied.
Since the RPST is based on single-entry single-exist subprocesses (i.e., subnets), it can help to remove places upfront in order to improve the overall decomposition.
These places are later added to the decomposition.

## Visualization
The visualization of the differences contains two major parts. 
First, the decomposition is shown where differences are projected on the subprocesses (i.e. vertices).
In case a statistical significant difference was detected for a subprocess (i.e., vertex), the color of the vertex depicts the effect size and for which process variant the average measurements value is lower or higher.
Second, we depict the Petri net.
If you select a subprocess in the decomposition, the adjacent nodes are highlighted.
Moreover, upon selection of a subprocess, you can press 'm' to collapse the subtree.

