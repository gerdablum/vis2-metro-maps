# Metro Maps


## Overview
As described in the paper "Metro Maps on Octilinear Grid Graphs" from Bast et al. (2020), our implementation is about mapping metro lines from cities onto a graph. Additionally, there are some constraints:
* Strict octilinearity
* Minimize line bends + small edge curves
* Preserve original station position

Overall, we have implemented the approximated, faster algorithm from the paper with a few deviations and enhancements. 

## Features
* Map visualization options:
  * choose a city (Vienna, Berlin, Freiburg, Suttgart)
  * too edgy/inaccurate? Change the grid cell size and the distance of the node to its true location and improve your map)
  * remove real world map (enjoy the graph without distractions)
  * add real world map (compare the position of the station markers with their true location)
  * show/remove the octilinear drawing (look at the real world lines or compare them with the octilinear graph)
  * show/remove the geografic drawing  (look at the octilinear graph or compare it with the real world lines)
  * display the station label or drop them
  * zoom in or out (depending on the zoom level stations and labels are shown)
* Octilinear graph is rendered in real-time.
* No equal distance between nodes to preserve true station position.

## Enhancement
* Labelling:
  * every station has its own label position & rotation
* PDF export possible:
  * choose from the various display options and save or print the current map immediately!

## Documentation & Resources

* [Backend documentation](./documentation/backend/index.html)
* [Frontend documentation](./documentation/frontend/index.html)
*  [Application demo](http://82.165.59.25:3000/)
*  [GitHub](https://github.com/gerdablum/vis2-metro-maps)




