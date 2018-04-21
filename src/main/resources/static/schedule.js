var map;
var directionsService;
var vehicleRouteLines;
var intervalTimer;

var vehicleRouteDirections;
var directionsTaskQueue;
var directionsTaskTimer;
var solution;

$(document).ready(function() {
    $("#info-div").show();
    $("#solution-div").hide();
});


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

showSolutionDiv = function(){
    $("#info-div").hide();
	$("#solution-div").show();
};

schedule = function () {
    $.ajax({
            url: "/schedule",
            type: "GET",
            contentType: "application/json",
            url: "/schedule",
            dataType: 'json',
            cache: false,
            timeout: 600000,
            success: function (response) {
                solution = response;
                initMap();
                showSolutionDiv();
                loadSolution();
            }, error: function (jqXHR, textStatus, errorThrown) {
                         ajaxError(jqXHR, textStatus, errorThrown)}
    });
};

updateScheduleList = function () {
    $('#schedule-list').empty();
	var lineOutput = "";
    var countTrucks = 0;
    $.each(solution.vehicleRouteList, function (index, vehicleRoute) {
        if(vehicleRoute.customerList.length>0){
            countTrucks++;
        }
    });
    lineOutput = lineOutput.concat("<p>Number of trucks used : " + countTrucks + "</p>");
    lineOutput = lineOutput.concat("<ol>");
    $.each(solution.vehicleRouteList, function (index, vehicleRoute) {
        if(vehicleRoute.customerList.length>0){
            lineOutput = lineOutput.concat("<li>Truck " + index + " :");
            lineOutput = lineOutput.concat("<ul>");
            $.each(vehicleRoute.customerList, function (index, customer) {
                lineOutput = lineOutput.concat("<li>Location Name : " + customer.locationName + " (" + customer.latitude + "," + customer.longitude + ")</li>");
            });
            lineOutput = lineOutput.concat("</ul>");
            lineOutput = lineOutput.concat("</li>");
        }
    });
    lineOutput = lineOutput.concat("</ol>");
	console.log(lineOutput);
	$('#schedule-list').append(lineOutput);
};

loadSolution = function () {
    var zoomBounds = new google.maps.LatLngBounds();
    $.each(solution.customerList, function (index, customer) {
        var latLng = new google.maps.LatLng(customer.latitude, customer.longitude);
        zoomBounds.extend(latLng);
        var marker = new google.maps.Marker({
            position: latLng,
            title: customer.locationName + ": Collection of: " + customer.demand + " Litres.",
            map: map
        });
        google.maps.event.addListener(marker, 'click', function () {
            new google.maps.InfoWindow({
                content: customer.locationName + "</br>Collection of:" + customer.demand + " Litres."
            }).open(map, marker);
        })
    });
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
    updateScheduleList();
    $.each(solution.vehicleRouteList, function (index, vehicleRoute) {
        var depotLocation = new google.maps.LatLng(vehicleRoute.depotLatitude, vehicleRoute.depotLongitude);
        var marker = new google.maps.Marker({
                    position: depotLocation,
                    title: vehicleRoute.depotLocationName + ": Depot",
                    map: map
                });
        google.maps.event.addListener(marker, 'click', function () {
            new google.maps.InfoWindow({
                content: vehicleRoute.depotLocationName + "<br/>Depot"
            }).open(map, marker)});
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
    }, 1000);
    map.fitBounds(zoomBounds);
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