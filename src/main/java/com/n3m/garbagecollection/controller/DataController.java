package com.n3m.garbagecollection.controller;

import com.n3m.garbagecollection.model.Location;
import com.n3m.garbagecollection.model.Site;
import com.n3m.garbagecollection.repository.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;

@RestController
public class DataController {

    @Autowired
    private SiteRepository siteRepository;

    @GetMapping("/data")
    public @ResponseBody Iterable<Site> greeting() {
        return siteRepository.findAll();
    }

    @PostMapping("/upload/images")
    public String addImage(@RequestParam("image1") MultipartFile image1,@RequestParam("image2") MultipartFile image2,
                           @RequestParam("name") String siteName, RedirectAttributes redirectAttributes) {

        try {
            Site site = siteRepository.findBySiteName(siteName);
            site.setImage1(image1.getBytes());
            site.setImage2(image2.getBytes());
            siteRepository.save(site);
            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded the images !");
        }catch(Exception e)
        {
            redirectAttributes.addFlashAttribute("message",
                    "Image upload failed: check the site name again !!!");
        }
        return "redirect:/";
    }

    @PostMapping("/upload/site")
    public String addSite(@RequestParam("lat") Double longitude,@RequestParam("lng") Double latitude,
                          @RequestParam("name") String siteName, RedirectAttributes redirectAttributes)
    {
        try {
            Site site = Site.builder().build();
            site.setSiteName(siteName);
            Location siteLocation = Location.builder().latitude(latitude).longitude(longitude).build();
            site.setSiteLocation(siteLocation);
            siteRepository.save(site);
            redirectAttributes.addFlashAttribute("message",
                    "You successfully created the site !");
        }catch(Exception e)
        {
            redirectAttributes.addFlashAttribute("message",
                    "Site Creation Failed !!!");
        }
        return "redirect:/";
    }




}
