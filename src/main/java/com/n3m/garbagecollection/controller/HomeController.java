package com.n3m.garbagecollection.controller;

import com.n3m.garbagecollection.model.Site;
import com.n3m.garbagecollection.repository.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @Autowired
    private SiteRepository siteRepository;

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    private void updatSiteData() {
        for (Site site : siteRepository.findAll())
        {
            //TODO: add volume estimation API consumption code;
            siteRepository.save(site);
        }
    }
    
}
