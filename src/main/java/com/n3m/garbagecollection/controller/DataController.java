package com.n3m.garbagecollection.controller;

import com.n3m.garbagecollection.model.Location;
import com.n3m.garbagecollection.model.Site;
import com.n3m.garbagecollection.model.Truck;
import com.n3m.garbagecollection.repository.SiteRepository;
import com.n3m.garbagecollection.repository.TruckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
public class DataController {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private TruckRepository truckRepository;

    @GetMapping("/data")
    public @ResponseBody Iterable<Site> greeting() {
        return siteRepository.findAll();
    }

    @PostMapping("/upload/images")
    public @ResponseBody String addImage(@RequestParam("image1") MultipartFile image1,@RequestParam("image2") MultipartFile image2,
                           @RequestParam("name") String siteName) {
        String result;
        try {
            Site site = siteRepository.findBySiteName(siteName);
            site.setImage1(image1.getBytes());
            site.setImage2(image2.getBytes());
            siteRepository.save(site);
            result = "You successfully uploaded the images !";
        }catch(Exception e)
        {
            result = "Image upload failed: check the site name again !!!";
        }
        return result;
    }

    @PostMapping("/upload/truck")
    public @ResponseBody String addTruck()
    {
        String result;
        try {
            Truck truck = Truck.builder().build();
            Long truckId = truckRepository.count();
            truck.setId(truckId);
            truckRepository.save(truck);
            result = "You successfully created the truck !";
        }catch(Exception e)
        {
            e.printStackTrace();
            result = "Truck Creation Failed !!!";
        }
        return result;
    }

    @PostMapping("/upload/site")
    public @ResponseBody String addSite(@RequestParam("lng") Double longitude,@RequestParam("lat") Double latitude,
                          @RequestParam("name") String siteName,@RequestParam("volume") Integer volume, RedirectAttributes redirectAttributes)
    {
        String result;
        try {
            Site site = Site.builder().build();
            long siteId = siteRepository.count();
            site.setId(siteId);
            site.setSiteName(siteName);
            Location siteLocation = Location.builder().latitude(latitude).longitude(longitude).build();
            site.setSiteLocation(siteLocation);
            site.setDemandVolume(volume);
            site.setCollectionRequired(true);
            siteRepository.save(site);
            result = "You successfully created the site !";
        }catch(Exception e)
        {
            e.printStackTrace();
            result = "Site Creation Failed !!!";
        }
        return result;
    }




}
