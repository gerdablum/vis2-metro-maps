
var map = L.map('map').setView([48.210033, 16.363449], 13);
var stationMarker = [];
var gridMarker = [];
var gridEdges = [];
initMap();
fetchData();
fetchGrid();
map.on("zoomend", function() {
  var zoom = map.getZoom();
  console.log(zoom);
  if (zoom > 12 && stationMarker != null) {
    stationMarker.forEach(a => a.addTo(map));
    gridMarker.forEach(a => a.addTo(map));
    gridEdges.forEach(a => a.addTo(map));
  }
  if (zoom <= 12 && stationMarker != null) {
    stationMarker.forEach(a => map.removeLayer(a));
    gridMarker.forEach(a => map.removeLayer(a));
    gridEdges.forEach(a => map.removeLayer(a));
  }
});


function initMap() {
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);
}

function fetchGrid() {
    axios.get('/vienna/gridgraph')
    .then(function (response) {
        gridEdges = [];
        gridNode = [];
        for (var gridEdge of response.data.edges) {
             gridEdges.push(L.polyline([gridEdge.source.coordinates,
             gridEdge.destination.coordinates], {color: 'grey'}));
        }
        for (var gridNode of response.data.gridVertices) {
        var text = gridNode.name + ", " + gridNode.coordinates[0] + gridNode.coordinates[1];
            gridMarker.push(L.circleMarker(gridNode.coordinates).bindTooltip(text).openTooltip());
        }

    })
    .catch(function (error) {
            // handle error
            console.log(error);
          })

    axios.get('/vienna/octilinear')
        .then(function (response) {
            gridEdges = [];
            gridNode = [];
            for (var edge of response.data) {
                for(var points of edge) {
                     L.polyline([points.source.coordinates,
                     points.destination.coordinates], {color: 'red'}).addTo(map);
                }

            }
        })
        .catch(function (error) {
                // handle error
                console.log(error);
              })
}


function fetchData() {
    axios.get('/vienna/stations')
      .then(function (response) {
        // handle success
        stationMarker = [];
        for (var node of response.data) {
            stationMarker.push(L.marker(node.coordinates));
        }
      })
      .catch(function (error) {
        // handle error
        console.log(error);
      })

  axios.get('/vienna/lines?lineId=1')
       .then(function (response) {
         // handle success
         for (var node of response.data) {
             console.log(node)
             var polyline = L.polyline(node.coordinates, {color: 'red'}).addTo(map);
         }
       })
       .catch(function (error) {
         // handle error
         console.log(error);
       })
  axios.get('/vienna/lines?lineId=2')
      .then(function (response) {
        // handle success
        for (var node of response.data) {
            console.log(node)
            var polyline = L.polyline(node.coordinates, {color: 'purple'}).addTo(map);
        }
      })
      .catch(function (error) {
        // handle error
        console.log(error);
      })
   axios.get('/vienna/lines?lineId=3')
         .then(function (response) {
            // handle success
            for (var node of response.data) {
                console.log(node)
                var polyline = L.polyline(node.coordinates, {color: 'orange'}).addTo(map);
            }
          })
          .catch(function (error) {
            // handle error
            console.log(error);
          })
  axios.get('/vienna/lines?lineId=4')
           .then(function (response) {
              // handle success
              for (var node of response.data) {
                  console.log(node)
                  var polyline = L.polyline(node.coordinates, {color: 'green'}).addTo(map);
              }
            })
            .catch(function (error) {
              // handle error
              console.log(error);
            })
  axios.get('/vienna/lines?lineId=6')
              .then(function (response) {
                 // handle success
                 for (var node of response.data) {
                     console.log(node)
                     var polyline = L.polyline(node.coordinates, {color: 'brown'}).addTo(map);
                 }
               })
               .catch(function (error) {
                 // handle error
                 console.log(error);
               })

}