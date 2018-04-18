var map;
var directionsService;
var vehicleRouteLines;
var intervalTimer;

var vehicleRouteDirections;
var directionsTaskQueue;
var directionsTaskTimer;

function initMap() {
    var mapCanvas = document.getElementById('map-canvas');
    var mapOptions = {
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };
    map = new google.maps.Map(mapCanvas, mapOptions);
    directionsService = new google.maps.DirectionsService();
}

ajaxError = function (jqXHR, textStatus, errorThrown) {
    console.log("Error: " + errorThrown);
    console.log("TextStatus: " + textStatus);
    console.log("jqXHR: " + jqXHR);
    alert("Error: " + errorThrown);
};

loadSolution = function () {
    $.ajax({
        url: "<%=application.getContextPath()%>/rest/vehiclerouting/solution",
        type: "GET",
        dataType: "json",
        success: function (solution) {
            var zoomBounds = new google.maps.LatLngBounds();
            $.each(solution.customerList, function (index, customer) {
                var latLng = new google.maps.LatLng(customer.latitude, customer.longitude);
                zoomBounds.extend(latLng);
                var marker = new google.maps.Marker({
                    position: latLng,
                    title: customer.locationName + ": Deliver " + customer.demand + " items.",
                    map: map
                });
                google.maps.event.addListener(marker, 'click', function () {
                    new google.maps.InfoWindow({
                        content: customer.locationName + "</br>Deliver " + customer.demand + " items."
                    }).open(map, marker);
                })
            });
            map.fitBounds(zoomBounds);
        }, error: function (jqXHR, textStatus, errorThrown) {
            ajaxError(jqXHR, textStatus, errorThrown)
        }
    });
};

updateSolution = function () {
    $.ajax({
        url: "<%=application.getContextPath()%>/rest/vehiclerouting/solution",
        type: "GET",
        dataType: "json",
        success: function (solution) {
            if (vehicleRouteLines != undefined) {
                for (var i = 0; i < vehicleRouteLines.length; i++) {
                    vehicleRouteLines[i].setMap(null);
                }
            }
            if (vehicleRouteDirections != undefined) {
                for (var i = 0; i < vehicleRouteDirections.length; i++) {
                    vehicleRouteDirections[i].setMap(null);
                }
            }
            vehicleRouteLines = [];
            vehicleRouteDirections = undefined;
            $.each(solution.vehicleRouteList, function (index, vehicleRoute) {
                var locations = [new google.maps.LatLng(vehicleRoute.depotLatitude, vehicleRoute.depotLongitude)];
                $.each(vehicleRoute.customerList, function (index, customer) {
                    locations.push(new google.maps.LatLng(customer.latitude, customer.longitude));
                });
                locations.push(new google.maps.LatLng(vehicleRoute.depotLatitude, vehicleRoute.depotLongitude));
                var line = new google.maps.Polyline({
                    path: locations,
                    geodesic: true,
                    strokeColor: vehicleRoute.hexColor,
                    strokeOpacity: 0.8,
                    strokeWeight: 4
                });
                line.setMap(map);
                vehicleRouteLines.push(line);
            });
            $('#scoreValue').text(solution.feasible ? solution.distance : "Not solved");
        }, error: function (jqXHR, textStatus, errorThrown) {
            ajaxError(jqXHR, textStatus, errorThrown)
        }
    });
};

solve = function () {
    $('#solveButton').attr("disabled", "disabled");
    $('#resolveDirectionsButton').attr("disabled", "disabled");
    if (directionsTaskTimer != undefined) {
        window.clearInterval(directionsTaskTimer);
        directionsTaskTimer = undefined;
    }
    var form = $('#fileUploadForm')[0];
    var data = new FormData(form);
    $.ajax({
         url: "<%=application.getContextPath()%>/rest/vehiclerouting/solution/solve",
        type: "POST",
        enctype: 'multipart/form-data',
        processData: false, // Important!
        contentType: false,
        cache: false,
        data: data,
        success: function (message) {
            console.log(message.text);
            initMap();
            loadSolution();
            intervalTimer = setInterval(function () {
                updateSolution()
            }, 2000);
            $('#terminateEarlyButton').removeAttr("disabled");
        }, error: function (jqXHR, textStatus, errorThrown) {
            ajaxError(jqXHR, textStatus, errorThrown)
        }
    });
};

terminateEarly = function () {
    $('#terminateEarlyButton').attr("disabled", "disabled");
    window.clearInterval(intervalTimer);
    if (directionsTaskTimer != undefined) {
        window.clearInterval(directionsTaskTimer);
        directionsTaskTimer = undefined;
    }
    $.ajax({
        url: "<%=application.getContextPath()%>/rest/vehiclerouting/solution/terminateEarly",
        type: "POST",
        data: "",
        dataType: "json",
        success: function (message) {
            console.log(message.text);
            updateSolution();
            $('#solveButton').removeAttr("disabled");
            $('#resolveDirectionsButton').removeAttr("disabled");
        }, error: function (jqXHR, textStatus, errorThrown) {
            ajaxError(jqXHR, textStatus, errorThrown)
        }
    });
};

resolveDirections = function () {
    $.ajax({
        url: "<%=application.getContextPath()%>/rest/vehiclerouting/solution",
        type: "GET",
        dataType: "json",
        success: function (solution) {
            if (vehicleRouteLines != undefined) {
                for (var i = 0; i < vehicleRouteLines.length; i++) {
                    vehicleRouteLines[i].setMap(null);
                }
            }
            if (vehicleRouteDirections != undefined) {
                for (var i = 0; i < vehicleRouteDirections.length; i++) {
                    vehicleRouteDirections[i].setMap(null);
                }
            }
            vehicleRouteLines = undefined;
            vehicleRouteDirections = [];
            directionsTaskQueue = [];
            $.each(solution.vehicleRouteList, function (index, vehicleRoute) {
                var depotLocation = new google.maps.LatLng(vehicleRoute.depotLatitude, vehicleRoute.depotLongitude);
                var previousLocation = depotLocation;
                $.each(vehicleRoute.customerList, function (index, customer) {
                    var location = new google.maps.LatLng(customer.latitude, customer.longitude);
                    directionsTaskQueue.push([previousLocation, location, vehicleRoute.hexColor]);
                    previousLocation = location;
                });
                directionsTaskQueue.push([previousLocation, depotLocation, vehicleRoute.hexColor]);
            });
            $('#scoreValue').text(solution.feasible ? solution.distance : "Not solved");
            directionsTaskTimer = setInterval(function () {
                sendDirectionsRequest()
            }, 1000); // 1 per second to avoid limit set by Google API for freeloaders
        }, error: function (jqXHR, textStatus, errorThrown) {
            ajaxError(jqXHR, textStatus, errorThrown)
        }
    });
};
sendDirectionsRequest = function () {
    var task = directionsTaskQueue.shift();
    if (task == undefined) {
        window.clearInterval(directionsTaskTimer);
        directionsTaskTimer = undefined;
        return;
    }
    var request = {
        origin: task[0],
        destination: task[1],
        travelMode: google.maps.TravelMode.DRIVING
    };
    var hexColor = task[2];
    directionsService.route(request, function (response, status) {
        if (status == google.maps.DirectionsStatus.OK) {
            var directionsRenderer = new google.maps.DirectionsRenderer({
                map: map,
                polylineOptions: {
                    geodesic: true,
                    strokeColor: hexColor,
                    strokeOpacity: 0.8,
                    strokeWeight: 4
                },
                markerOptions: {
                    visible: false
                },
                preserveViewport: true
            });
            directionsRenderer.setDirections(response);
            vehicleRouteDirections.push(directionsRenderer);
        } else if (status == google.maps.DirectionsStatus.OVER_QUERY_LIMIT) {
            console.log("Google directions API request failed wtih status (" + status + "), but retrying...");
            directionsTaskQueue.push(task);
        } else {
            console.log("Google directions API request failed with status (" + status + ").");
        }
    });
};

renderDirections = function (origin, destination, hexColor) {
    var request = {
        origin: origin,
        destination: destination,
        travelMode: google.maps.TravelMode.DRIVING
    };
    directionsService.route(request, function (response, status) {
        if (status == google.maps.DirectionsStatus.OK) {
            var directionsRenderer = new google.maps.DirectionsRenderer({
                map: map,
                polylineOptions: {
                    geodesic: true,
                    strokeColor: hexColor,
                    strokeOpacity: 0.8,
                    strokeWeight: 4
                },
                markerOptions: {
                    visible: false
                }
            });
            directionsRenderer.setDirections(response);
            vehicleRouteDirections.push(directionsRenderer);
        } else {
            console.log("Google directions error status: " + status);
        }
    });
};
function sleep(milliseconds) {
    console.log("Sleeping " + milliseconds + " ms");
    // TODO Don't hang the browser page like this
    var start = new Date().getTime();
    for (var i = 0; i < 1e7; i++) {
        if ((new Date().getTime() - start) > milliseconds) {
            break;
        }
    }
}

google.maps.event.addDomListener(window, 'load', initMap);