[![](https://github.com/tischi/imagej-cluster/actions/workflows/build-main.yml/badge.svg)](https://github.com/tischi/imagej-cluster/actions/workflows/build-main.yml)

# imagej-cluster

Java code for running ImageJ macros and commands on a computer cluster.

Currently supported computer cluster management systems:

- slurm

## Usage examples

- [BigDataProcessor2](https://github.com/bigdataprocessor/bigdataprocessor2) uses imagej-cluster for distributed processing of 5D image data (one channel and timepoint per job). For example, a 3D image with 100 timepoints and 3 channels would be processed with 300 jobs, potentially resulting in a 300-fold increase of the processing speed.
