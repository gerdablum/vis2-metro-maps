# Metro Maps
https://github.com/gerdablum/vis2-metro-maps

## Overview
Some important classes:
* MetroDataProvider: reads json resource files and creates station and lineEdges from information
* M10Service: implementation of MetroDataProvider for Vienna dataset
* InputGraph: Represents stations in combination with lineEdges logically connected. Contains methods to sort edges and calculate bounding box as described in paper
* GridGraph: Grid with set distance d based on bounding box of input graph. Contains method to map input lineEdges onto grid (not finished yet)

## current state
At the moment we can read in a data set file and map the data from it exactly on an openstreet map.
We are in the middle of implementing the octilinear graph algorithm. On frontend, you can see circle markes as the grid when you zoom in, as well as the original station positions.

## TBD
* finish implementing approximate algorithm
* include more cities in dataset
* investigate adding labels
* PDF export
