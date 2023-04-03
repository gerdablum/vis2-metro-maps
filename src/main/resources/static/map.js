
var map = L.map('map').setView([48.210033, 16.363449], 13);
var stationMarker = [];
initMap();
fetchData();
map.on("zoomend", function() {
  var zoom = map.getZoom();
  console.log(zoom);
  if (zoom > 12 && stationMarker != null) {
    stationMarker.forEach(a => a.addTo(map));
  }
  if (zoom <= 12 && stationMarker != null) {
    stationMarker.forEach(a => map.removeLayer(a));
    ;
  }
});


function initMap() {
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);
}


function fetchData() {
    axios.get('/vienna/stations')
      .then(function (response) {
        // handle success
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
