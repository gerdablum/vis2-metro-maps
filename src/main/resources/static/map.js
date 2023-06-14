
var map = L.map('map').setView([48.210033, 16.363449], 13);
var stationMarker = [];
var gridMarker = [];
var gridEdges = [];
var geoLines = [];
var octiLines = [];
var labels = [];
var gridSize = 0.5;
var distanceR = 0.77;
var mapLayer = null;
var browserControl = null;
var selectedCity = "vienna";
var viewArray = {
    vienna: { lat: 48.210033, lon: 16.363449, zoom: 13, name: "Vienna" },
    berlin: { lat: 52.52437, lon: 13.41053, zoom: 13, name: "Berlin" },
    freiburg: { lat: 47.997791, lon: 7.842609, zoom: 13, name: "Freiburg"},
    london: { lat: 51.507359, lon: -0.136439, zoom: 13, name: "London" },
    stuttgart: { lat:  48.783333, lon:  9.183333, zoom: 13, name: "Stuttgart" }
};

var labelOptions = {
    className: 'leaflet-tooltip-own',
    permanent: true,
    direction: 'center',
    opacity: 1,
    interactive: false
};

var stationMarkerOptions = {
    color: 'black',
    fillColor: 'white',
    fillOpacity: 1
};
debug = false;

$( document ).ready(function() {
    const removeMapButton = document.getElementById('removeMap-button');
    removeMapButton.addEventListener('click', removeBackgroundMap);

    const addMapButton = document.getElementById('addMap-button');
    addMapButton.addEventListener('click', addMapLayer);

    const showOctiButton = document.getElementById("show-octi-lines-button");
    showOctiButton.addEventListener('click', refreshMap);
    const showGeoButton = document.getElementById("show-geo-lines-button");
    showGeoButton.addEventListener('click', refreshMap)
    const showLabelButton = document.getElementById("show-labels-button");
    showLabelButton.addEventListener('click', refreshMap)

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

    document.getElementById("changeGridCells").addEventListener("click", function() {
        var gridSizeInput = parseFloat(document.getElementById("gridSize").value);
        var distanceRInput = parseFloat(document.getElementById("distanceR").value);

        console.log("Grid Size:", gridSizeInput);
        console.log("Distance:", distanceRInput);

        gridSize = gridSizeInput ? parseFloat(gridSizeInput) : null;
        distanceR = distanceRInput ? parseFloat(distanceRInput) : null;
        loadMap();
    });

    loadMap();
});


function loadMap() {
    clearMap();
    stationMarker = [];
    labels = [];
    gridMarker = [];
    gridEdges = [];
    geoLines = [];
    octiLines = [];
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
    fetchOctilinear(cityData.name, refreshMap);
    fetchData(cityData.name, refreshMap);
    map.on("zoomend", function() {
        zoomEffect();
    });
}

function refreshMap() {

    var isOctiChecked = $('#show-octi-lines-button').is(':checked');
    var isGeoChecked = $('#show-geo-lines-button').is(':checked');
    var isLabelChecked = $('#show-labels-button').is(':checked');
    if (isOctiChecked) {
        octiLines.forEach(a => a.addTo(map));
    } else {
       octiLines.forEach(a => map.removeLayer(a));
       gridMarker.forEach(a => map.removeLayer(a));
       labels.forEach(a => map.removeLayer(a));
    }

    if (isLabelChecked && isOctiChecked) {
        labels.forEach(a => a.addTo(map));
    } else {
        labels.forEach(a => map.removeLayer(a));
    }

    if (isGeoChecked) {
        geoLines.forEach(a => a.addTo(map));
    } else {
        geoLines.forEach(a => map.removeLayer(a));
    }
    zoomEffect();
}

function zoomEffect() {
    var isOctiChecked = $('#show-octi-lines-button').is(":checked")
    var isLabelChecked = $('#show-labels-button').is(':checked');
    var zoom = map.getZoom();
    if (zoom > 12 && stationMarker != null) {
        if (isOctiChecked) {
            //stationMarker.forEach(a => a.addTo(map));
            if (zoom > 14 && isLabelChecked) {
                labels.forEach(a => a.addTo(map));
            }
            gridMarker.forEach(a => a.addTo(map));
        }
        gridEdges.forEach(a => a.addTo(map));
        console.log("add details!");
    }
    if (zoom <= 14) {
        labels.forEach(a => map.removeLayer(a));
    }
    if (zoom <= 12 && stationMarker != null) {
        stationMarker.forEach(a => map.removeLayer(a));
        gridMarker.forEach(a => map.removeLayer(a));
        gridEdges.forEach(a => map.removeLayer(a));
        console.log("remove details!");
    }
}


function fetchOctilinear(cityName, callback) {
    var url = '/' + encodeURIComponent(cityName) + '/octilinear';
    url += '?gridSize=' + encodeURIComponent(gridSize);
    url += '&distanceR=' + encodeURIComponent(distanceR);
    axios.get(url)
        .then(function (response) {
            gridEdges = [];
            gridNode = [];
            const colorLineMap = new Map();
            for (let edge of response.data) {
                for(let points of edge) {
                     let colors = points.colors;
                    for (let i = 0; i < colors.length; i++) {
                        let polyline = L.polyline([points.source.coordinates,
                            points.destination.coordinates], {color: '#' + colors[i]});
                        polyline.setOffset(i*3);
                        octiLines.push(polyline)
                    }

                }
            }
            fetchGrid(cityName, callback)
        })
        .catch(function (error) {
                // handle error
                console.log(error);
              })
}

function fetchGrid(cityName, callback) {
    axios.get('/' + cityName + '/gridgraph')          // ' + selectedCity + '
        .then(function (response) {
            console.log('URL:', response.config.url);
            gridEdges = [];
            if (debug) {
                for (var gridEdge of response.data.edges) {
                    var text = gridEdge.bendCost;
                    gridEdges.push(L.polyline([gridEdge.source.coordinates,
                        gridEdge.destination.coordinates], {color: 'grey', opacity: 0.5, weight: 10}).bindTooltip(text).openTooltip());
                }
            }
            for (var gridNode of response.data.gridVertices) {
                var text = gridNode.name + ", " + gridNode.stationName + ", " + gridNode.coordinates[0] + gridNode.coordinates[1];
                if (gridNode.stationName !== null) {
                    gridMarker.push(L.circleMarker(gridNode.coordinates,stationMarkerOptions).bindTooltip(text).openTooltip());
                    var rotation = gridNode.labelRotation;
                    var myIcon = L.divIcon({className: "rotated-labels", html: "<div style='transform: rotate(" + rotation + "deg)'>" + gridNode.stationName + "</div>"});

                    var customTooltip = L.tooltip(labelOptions);
                    customTooltip.setContent(gridNode.stationName);
                    labels.push(L.marker(gridNode.labelCoordinates, {icon: myIcon}));
                }
                if (debug) {               // DEBUG
                    color =  {color: 'blue'}
                    gridMarker.push(L.circleMarker(gridNode.coordinates,color).bindTooltip(text).openTooltip());
                }
                callback();
            }
        })
        .catch(function (error) {
            // handle error
            console.log(error);
        })
}

function fetchData(cityName, callback) {
    console.log(cityName);
    axios.all([
        axios.get('/' + cityName + '/stations'),
        axios.get('/' + cityName + '/lines')
    ]).then(function (response) {
        // handle success
        stationMarker = [];
        if (debug) {
            for (var node of response[0].data) {
                var text = node.name + " " + node.coordinates;
                stationMarker.push(L.marker(node.coordinates).bindTooltip(text).openTooltip());
            }
        }
        for (const node of response[1].data) {
            for (const line of node.lines) {
                let color = '#' + line.color;
                var polyline = L.polyline(node.coordinates, {color: color});
                geoLines.push(polyline)
            }
        }
        callback();
      })
      .catch(function (error) {
        // handle error
        console.log(error);
      })
}

function clearMap() {
    stationMarker.forEach(marker => map.removeLayer(marker));
    gridMarker.forEach(marker => map.removeLayer(marker));
    gridEdges.forEach(edge => map.removeLayer(edge));
    labels.forEach(label => map.removeLayer(label));
    map.eachLayer(layer => {
        if (layer instanceof L.Polyline) {
            map.removeLayer(layer);
        }
    });
    if (mapLayer) {
        map.removeLayer(mapLayer);
        mapLayer = null;
    }
}

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

