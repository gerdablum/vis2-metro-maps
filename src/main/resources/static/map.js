
var map = L.map('map').setView([48.210033, 16.363449], 13);
initMap();
fetchData();


function initMap() {
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);
}

function fetchData() {
    axios.get('/vienna')
      .then(function (response) {
        // handle success
        for (var node of response.data) {
            var marker = L.marker(node.coordinates).addTo(map);
        }
      })
      .catch(function (error) {
        // handle error
        console.log(error);
      })
}
