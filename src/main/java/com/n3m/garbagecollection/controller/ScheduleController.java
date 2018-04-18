package com.n3m.garbagecollection.controller;

import com.n3m.garbagecollection.model.Schedule;
import com.n3m.garbagecollection.model.Site;
import com.n3m.garbagecollection.repository.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ScheduleController {

    @Autowired
    private SiteRepository siteRepository;

    @GetMapping("/schedule")
    public @ResponseBody List<Schedule> scheduleCollection()
    {
        //TODO: add code for creating vrp
        //TODO: add code for calling VRP service
        return new ArrayList<Schedule>();
    }

    private void updatSiteData() {
        for (Site site : siteRepository.findAll())
        {
            //TODO: add volume estimation API consumption code;
            siteRepository.save(site);
        }
    }
}
