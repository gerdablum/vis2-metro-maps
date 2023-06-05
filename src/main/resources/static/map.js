
var map = L.map('map').setView([48.210033, 16.363449], 13);
var stationMarker = [];
var gridMarker = [];
var gridEdges = [];
var mapLayer = null;
var browserControl = null;
var selectedCity = "vienna";
var viewArray = {
    vienna: { lat: 48.210033, lon: 16.363449, zoom: 13, name: "Vienna" },
    berlin: { lat: 52.52437, lon: 13.41053, zoom: 13, name: "Berlin" },
    freiburg: { lat: 47.997791, lon: 7.842609, zoom: 13, name: "Freiburg"},
    london: { lat: 51.507359, lon: -0.136439, zoom: 13, name: "London" },
    nyc_subway: { lat: 40.730610, lon: -73.935242, zoom: 11, name: "New York" }
};

loadMap();
function loadMap() {
    stationMarker = [];
    gridMarker = [];
    gridEdges = [];
    if (mapLayer) {map.removeLayer(mapLayer);}
    var cityData = viewArray[selectedCity];
    if (cityData) { map.setView([cityData.lat, cityData.lon], cityData.zoom); }
    var pdfOptions = {
        title: 'Print me!',
        documentTitle: 'Metro map of ' + cityData.name
    };
    if (!browserControl) {
        browserControl = L.control.browserPrint(pdfOptions).addTo(map);
    }
    addMapLayer();
    fetchOctilinear(cityData.name);
    fetchData(cityData.name);
    zoomEffect();
}
function zoomEffect() {
    map.on("zoomend", function() {
        var zoom = map.getZoom();
        console.log(zoom);
        if (zoom > 12 && stationMarker != null) {
            stationMarker.forEach(a => a.addTo(map));
            gridEdges.forEach(a => a.addTo(map));
            gridMarker.forEach(a => a.addTo(map));
            console.log("add details!");
        }
        if (zoom <= 12 && stationMarker != null) {
            stationMarker.forEach(a => map.removeLayer(a));
            gridMarker.forEach(a => map.removeLayer(a));
            gridEdges.forEach(a => map.removeLayer(a));
            console.log("remove details!");
        }
    });
}

/*
var saveAsImage = function () {
    return domtoimage.toPng(document.body)
        .then(function (dataUrl) {
            var link = document.createElement('a');
            link.download = map.printControl.options.documentTitle || "exportedMap" + '.png';
            link.href = dataUrl;
            link.click();
        });
};
L.control.browserPrint({
    documentTitle: "printImage",
    printModes: [
        L.BrowserPrint.Mode.Auto("Download PNG"),
    ],
    printFunction: saveAsImage
}).addTo(map);
*/


function fetchOctilinear(cityName) {

    axios.get('/' + cityName + '/octilinear')
        .then(function (response) {
            gridEdges = [];
            gridNode = [];
            for (var edge of response.data) {
                for(var points of edge) {
                     var text = points.bendCost;
                     L.polyline([points.source.coordinates,
                     points.destination.coordinates], {color: 'red'}).bindTooltip(text).openTooltip().addTo(map);
                }

            }
            fetchGrid(cityName)
        })
        .catch(function (error) {
                // handle error
                console.log(error);
              })
}

function fetchGrid(cityName) {
    axios.get('/' + cityName + '/gridgraph')          // ' + selectedCity + '
        .then(function (response) {
            gridEdges = [];
            gridNode = [];
            for (var gridEdge of response.data.edges) {
                var text = gridEdge.bendCost;
                gridEdges.push(L.polyline([gridEdge.source.coordinates,
                    gridEdge.destination.coordinates], {color: 'grey', opacity: 0.5, weight: 10}).bindTooltip(text).openTooltip());
            }
            for (var gridNode of response.data.gridVertices) {
                var text = gridNode.name + ", " + gridNode.stationName + ", " + gridNode.coordinates[0] + gridNode.coordinates[1];
                var color =  {color: 'blue'}
                if (gridNode.stationName !== null) {
                    color =  {color: 'red'}
                }
                gridMarker.push(L.circleMarker(gridNode.coordinates,color).bindTooltip(text).openTooltip());
                // TODO: DELETE: just for test reasons:
                if (gridNode.stationName === "Stephansplatz") {
                    var labelOptions = {
                        className: 'label-class',
                        permanent: true,
                        direction: 'center',
                        opacity: 1,
                        interactive: false
                    };
                    gridMarker.push(L.circleMarker(gridNode.coordinates,color).bindTooltip(gridNode.stationName, labelOptions).openTooltip());
                }
            }
        })
        .catch(function (error) {
            // handle error
            console.log(error);
        })
}

function fetchData(cityName) {
    console.log(cityName);
    axios.get('/' + cityName + '/stations')
      .then(function (response) {
        // handle success
        stationMarker = [];
        for (var node of response.data) {
            var text = node.name;
            stationMarker.push(L.marker(node.coordinates).bindTooltip(text).openTooltip());
        }
      })
      .catch(function (error) {
        // handle error
        console.log(error);
      })
  axios.get('/' + cityName + '/lines?lineId=1')
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
  axios.get('/' + cityName + '/lines?lineId=2')
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
   axios.get('/' + cityName + '/lines?lineId=3')
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
  axios.get('/' + cityName + '/lines?lineId=4')
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
  axios.get('/' + cityName + '/lines?lineId=6')
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
//window.jsPDF = window.jspdf.jsPDF;
/*
function exportMapToPDF() {
    console.log("Exportieren!");

    const mapWidth = map.getSize().x;
    const mapHeight = map.getSize().y;

    const tempDiv = document.createElement('div');
    tempDiv.style.width = `${mapWidth}px`;
    tempDiv.style.height = `${mapHeight}px`;
    document.body.appendChild(tempDiv);

    const scale = 2;
    const resolution = 1000; // dpi

    leafletImage(map, function(err, canvas) {
        const scaledCanvas = document.createElement('canvas');
        scaledCanvas.width = mapWidth * scale;
        scaledCanvas.height = mapHeight * scale;
        const context = scaledCanvas.getContext('2d');
        context.scale(scale, scale);
        context.drawImage(canvas, 0, 0);

        const dataUrl = scaledCanvas.toDataURL('image/png', resolution / 72);

        const doc = new jsPDF('landscape');
        doc.text("Hello map fan!", 10, 10);
        doc.addImage(dataUrl, 'PNG', 5, 15, mapWidth / 3, mapHeight / 3);
        doc.save('map.pdf');

        document.body.removeChild(tempDiv);
    });
}*/

function toggleButtonState(button) {
    var buttons = document.querySelectorAll('.btn-group-toggle .btn');
    buttons.forEach(function(btn) {
        console.log(btn.id);
        btn.classList.remove('active');
    });

    var inputs = document.querySelectorAll('.btn-group-toggle input[type="radio"]');
    inputs.forEach(function(input) {
        input.removeAttribute('checked');
    });

    button.parentNode.classList.add('active');
    button.checked = true;
}

function addMapLayer() {
    mapLayer = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);
    console.log("Add map!");
    btn = document.getElementById('addMap-button');
    toggleButtonState(btn);
}
function removeBackgroundMap() {
    console.log("Remove background map!");
    map.removeLayer(mapLayer);
    btn = document.getElementById('removeMap-button');
    toggleButtonState(btn);
}

const removeMapButton = document.getElementById('removeMap-button');
removeMapButton.addEventListener('click', removeBackgroundMap);

const addMapButton = document.getElementById('addMap-button');
addMapButton.addEventListener('click', addMapLayer);

//toggleButtonState(addMapButton);

const dropdownButton = document.getElementById('dropdownMenuButton');
const dropdownItems = document.querySelectorAll('.dropdown-item');

dropdownItems.forEach(function(item) {
    item.addEventListener('click', async function() {
        const selectedText = item.textContent;
        dropdownButton.textContent = selectedText;
        selectedCity = item.dataset.value;
        console.log("selected: " + selectedCity);
        loadMap();
    });
});

