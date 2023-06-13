
var map = L.map('map').setView([48.210033, 16.363449], 13);
var stationMarker = [];
var gridMarker = [];
var gridEdges = [];
var geoLines = [];
var octiLines = [];
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
var invisibleMarkerOptions = {
    /*icon: L.divIcon({
        className: 'invisible-marker',
        html: '',
        iconSize: [1, 1]
    }),*/
    interactive: false,
    rotationAngle: 45       // this is for rotating blue markers
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

    var isOctiChecked = $('#show-octi-lines-button').is(":checked")
    var isGeoChecked = $('#show-geo-lines-button').is(":checked")
    if (isOctiChecked) {
        octiLines.forEach(a => a.addTo(map));
    } else {
       octiLines.forEach(a => map.removeLayer(a));
       gridMarker.forEach(a => map.removeLayer(a));
    }

    if (isGeoChecked) {
        geoLines.forEach(a => a.addTo(map));
    } else {
        geoLines.forEach(a => map.removeLayer(a));
        stationMarker.forEach(a => map.removeLayer(a))
    }
    zoomEffect();
}

function zoomEffect() {
    var isOctiChecked = $('#show-octi-lines-button').is(":checked")
    var zoom = map.getZoom();
    if (zoom > 12 && stationMarker != null) {
        if (isOctiChecked) {
            stationMarker.forEach(a => a.addTo(map));
            gridMarker.forEach(a => a.addTo(map));
        }
        gridEdges.forEach(a => a.addTo(map));
        console.log("add details!");
    }
    if (zoom <= 12 && stationMarker != null) {
        stationMarker.forEach(a => map.removeLayer(a));
        gridMarker.forEach(a => map.removeLayer(a));
        gridEdges.forEach(a => map.removeLayer(a));
        console.log("remove details!");
    }
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


function fetchOctilinear(cityName, callback) {
    var url = '/' + encodeURIComponent(cityName) + '/octilinear';
    url += '?gridSize=' + encodeURIComponent(gridSize);
    url += '&distanceR=' + encodeURIComponent(distanceR);
    axios.get(url)
        .then(function (response) {
            gridEdges = [];
            gridNode = [];
            for (let edge of response.data) {
                for(let points of edge) {
                     let text = points.bendCost;
                     let colors = points.colors;
                    for (let i = 0; i < colors.length; i++) {
                        let polyline = L.polyline([points.source.coordinates,
                            points.destination.coordinates], {color: '#' + colors[i]}).bindTooltip(text).openTooltip();
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
            gridNode = [];
            if (debug) {
                for (var gridEdge of response.data.edges) {
                    var text = gridEdge.bendCost;
                    gridEdges.push(L.polyline([gridEdge.source.coordinates,
                        gridEdge.destination.coordinates], {color: 'grey', opacity: 0.5, weight: 10}).bindTooltip(text).openTooltip());
                }
            }
            for (var gridNode of response.data.gridVertices) {
                var text = gridNode.name + ", " + gridNode.stationName + ", " + gridNode.coordinates[0] + gridNode.coordinates[1];
                //var color =  {color: 'red'}
                if (gridNode.stationName !== null) {
                    gridMarker.push(L.circleMarker(gridNode.coordinates,stationMarkerOptions).bindTooltip(text).openTooltip());
                    var rotation = gridNode.labelRotation;
                    var myIcon = L.divIcon({className: "rotated-labels", html: "<div style='transform: rotate(" + rotation + "deg)'>" + gridNode.stationName + "</div>"});

                    var customTooltip = L.tooltip(labelOptions);
                    customTooltip.setContent(gridNode.stationName);
                    gridMarker.push(L.marker(gridNode.labelCoordinates, {icon: myIcon}));

                    if (gridNode.stationName === "Stephansplatz" || gridNode.stationName === "Karlsplatz" || gridNode.stationName === "Taubstummengasse") {
                        //gridMarker.push(invisibleMarker);
                        //gridMarker.push(L.circleMarker(gridNode.coordinates, color).bindTooltip(customTooltip).openTooltip());
                        //gridMarker.push(L.circleMarker(gridNode.labelCoordinates, color).bindTooltip(customTooltip).openTooltip());
                    }
                }
                if (debug) {               // DEBUG
                    color =  {color: 'blue'}
                    gridMarker.push(L.circleMarker(gridNode.coordinates,color).bindTooltip(text).openTooltip());
                }
                callback();
            }
            /*
            for (var stationLabel of response.data.stationLabelling) {
                var text = gridNode.name + ", " + gridNode.stationName + ", " + gridNode.coordinates[0] + gridNode.coordinates[1];
                var color =  {color: 'blue'}
                if (gridNode.stationName !== null) {
                    color =  {color: 'red'}
                }
                gridMarker.push(L.circleMarker(gridNode.coordinates,color).bindTooltip(text).openTooltip());
                if (gridNode.stationName === "Stephansplatz") {
                    var labelOptions = {
                        className: 'leaflet-tooltip-own',
                        permanent: true,
                        direction: 'center',
                        opacity: 1,
                        interactive: false
                    };
                    var customTooltip = L.tooltip(labelOptions);
                    customTooltip.setContent(gridNode.stationName);
                    gridMarker.push(L.circleMarker(gridNode.coordinates, color).bindTooltip(customTooltip).openTooltip());
                }
            }
            */
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

function clearMap() {
    stationMarker.forEach(marker => map.removeLayer(marker));
    gridMarker.forEach(marker => map.removeLayer(marker));
    gridEdges.forEach(edge => map.removeLayer(edge));
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

