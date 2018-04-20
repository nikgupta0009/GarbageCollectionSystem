package com.n3m.garbagecollection.controller;

import com.n3m.garbagecollection.model.*;
import com.n3m.garbagecollection.repository.SiteRepository;
import com.n3m.garbagecollection.repository.TruckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;

@RestController
public class ScheduleController {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private TruckRepository truckRepository;

    private RestTemplate restTemplate;

    public ScheduleController() {
        initRestTemplate();
    }

    @GetMapping("/schedule")
    public @ResponseBody String scheduleCollection() throws Exception {
        File file = createVrpData(siteRepository.findAll(),truckRepository.findAll());
        MultiValueMap<String, Object> parts =
                new LinkedMultiValueMap<String, Object>();
        parts.add ("file", new FileSystemResource(file));
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("enctype", "\'multipart/form-data\'");
        headers.set("processData", "false");
        headers.set("contentType", "false");
        headers.set("cache","false");

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<MultiValueMap<String, Object>>(parts, headers);

        System.out.println(requestEntity.getHeaders());
        System.out.println(requestEntity.getBody());

        ResponseEntity<String> response =
                restTemplate.exchange("https://vehiclerouting.herokuapp.com/rest/vehiclerouting/solution/solve",
                        HttpMethod.POST, requestEntity, String.class);
        List<String> cookies = response.getHeaders().get("Cookie");
        if (cookies == null) {
            cookies = response.getHeaders().get("Set-Cookie");
        }
        String cookie = cookies.get(cookies.size() - 1);
        int start = cookie.indexOf('=');
        int end = cookie.indexOf(';');
        String jsessionId = cookie.substring(start + 1, end);
        System.out.println(jsessionId);
        HttpHeaders sessionHeaders = new HttpHeaders();
        sessionHeaders.add("Cookie", "JSESSIONID=" + jsessionId);
        HttpEntity<String> sessionHttpEntity = new HttpEntity<>(sessionHeaders);
        ResponseEntity<String> solution = null;
        for(int i=0;i<50;i++)
        {
            solution = restTemplate.exchange("https://vehiclerouting.herokuapp.com/rest/vehiclerouting/solution",
                            HttpMethod.GET,sessionHttpEntity,String.class);
        }
        restTemplate.exchange("https://vehiclerouting.herokuapp.com/rest/vehiclerouting/solution/terminateEarly",
                HttpMethod.POST,sessionHttpEntity,String.class);
        return solution.getBody();
    }

    private void updatSiteData() {
        for (Site site : siteRepository.findAll()) {
            //TODO: add volume estimation API consumption code;
            siteRepository.save(site);
        }
    }

    @GetMapping("/vrp")
    public @ResponseBody String getFile() throws Exception {
        List<Site> siteList = siteRepository.findAll();
        List<Truck> truckList = truckRepository.findAll();
        createVrpData(siteList, truckList);
        return "See the command line output";
    }

    private void initRestTemplate() {
        restTemplate = new RestTemplateBuilder().build();
    }

    private File createVrpData(List<Site> siteList, List<Truck> vehicleList) throws Exception {
        siteList.sort(new Comparator<Site>() {
            @Override
            public int compare(Site o1, Site o2) {
                if (o1.getId() < o2.getId())
                    return -1;
                if (o1.getId() == o2.getId())
                    return 0;
                return 1;
            }
        });
        File vrpFile = null;
        Integer capacity = 500;
        String distanceType = "km";
        String vrpType = "CVRP";
        String name = "DELHI";
        Integer depotListSize = 1;
        BufferedWriter vrpWriter;
        try {
            vrpFile = createVrpFile("Delhi", siteList.size(), vehicleList.size());
            vrpWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(vrpFile), "UTF-8"));
            writeHeaders(vrpWriter, siteList.size(), capacity, distanceType, vrpType, name);
            writeNodeCoordSection(vrpWriter, siteList);
            writeEdgeWeightSection(vrpWriter, distanceType, siteList);
            writeDemandSection(vrpWriter, siteList);
            writeDepotSection(vrpWriter, siteList, depotListSize);
            vrpWriter.flush();
            vrpWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return vrpFile;
    }

    private File createVrpFile(String location_name, Integer siteListSize, Integer vehicleListSize) throws IOException {
        String filename = location_name
                + "-n" + siteListSize + "-k" + vehicleListSize;
        String rootDir = System.getProperty("user.dir");
        String fileDirLocation = new File(rootDir + "/tmp").getAbsolutePath() + "/";
        File fileDir = new File(fileDirLocation);
        if (fileDir.exists()) {
            FileSystemUtils.deleteRecursively(fileDir);
        }
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File vrpOutputFile = new File(fileDirLocation + filename + ".vrp");
        System.out.println(vrpOutputFile.getAbsolutePath());
        vrpOutputFile.createNewFile();
        return vrpOutputFile;
    }

    private void writeHeaders(BufferedWriter vrpWriter, int locationListSize, int capacity,
                              String distanceType, String vrpType, String name) throws Exception {
        vrpWriter.write("NAME: " + name + "\n");
        vrpWriter.write("TYPE: " + vrpType + "\n");
        vrpWriter.write("DIMENSION: " + locationListSize + "\n");
        vrpWriter.write("EDGE_WEIGHT_TYPE: EXPLICIT\n");
        vrpWriter.write("EDGE_WEIGHT_FORMAT: FULL_MATRIX\n");
        vrpWriter.write("EDGE_WEIGHT_UNIT_OF_MEASUREMENT: " + distanceType + "\n");
        vrpWriter.write("CAPACITY: " + capacity + "\n");
    }

    private void writeNodeCoordSection(BufferedWriter vrpWriter, List<Site> locationList) throws Exception {
        vrpWriter.write("NODE_COORD_SECTION\n");
        for (Site location : locationList) {
            vrpWriter.write(location.getId() + " " + location.getSiteLocation().getLatitude() + " " + location.getSiteLocation().getLongitude()
                    + (location.getSiteName() != null ? " " + location.getSiteName().replaceAll(" ", "_") : "") + "\n");
        }
    }

    private void writeEdgeWeightSection(BufferedWriter vrpWriter, String distanceType, List<Site> locationList) throws Exception {
        DecimalFormat distanceFormat = new DecimalFormat("0.000");
        vrpWriter.write("EDGE_WEIGHT_SECTION\n");
        List<List<Double>> distances = fetchGhResponse(locationList);
        for(List<Double> distanceList : distances)
        {

            for(Double distance : distanceList)
            {
                vrpWriter.write(distanceFormat.format(distance/1000) + " ");
            }
            vrpWriter.write("\n");
        }
    }

    private List<List<Double>> fetchGhResponse(List<Site> siteList) {
        String baseUrl = "https://graphhopper.com/api/1/matrix";
        String apiKey = "c029f1ef-c694-4999-8c06-eeb1c338d7a5";
        UriBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl);
        URI uri;
        for(Site site : siteList) {
              uriBuilder = uriBuilder.queryParam("point", site.getSiteLocation().getLatitude() + "," + site.getSiteLocation().getLongitude());
        }
        uri = uriBuilder.queryParam("vehicle", "car").queryParam("locale", "de").queryParam("out_array","distances")
                .queryParam("key", apiKey).build();

        System.out.println(uri.toString());

        JsonResponse jsonResponse = restTemplate.getForObject(uri, JsonResponse.class);
        return jsonResponse.getDistances();
    }

    private void writeDemandSection(BufferedWriter vrpWriter, List<Site> locationList) throws IOException {
        vrpWriter.append("DEMAND_SECTION\n");
        int i = 0;
        for (Site location : locationList) {
            String line = location.getId() + " " + location.getDemandVolume();
            vrpWriter.append(line).append("\n");
        }
    }

    private void writeDepotSection(BufferedWriter vrpWriter, List<Site> locationList, Integer depotListSize) throws Exception {
        vrpWriter.append("DEPOT_SECTION\n");
        for (int i = 0; i < depotListSize && i < locationList.size(); i++) {
            Site location = locationList.get(i);
            vrpWriter.append(Long.toString(location.getId())).append("\n");
        }
        vrpWriter.append("-1\n");
        vrpWriter.append("EOF\n");
    }


}
