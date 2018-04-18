package com.n3m.garbagecollection.controller;

import com.n3m.garbagecollection.model.Site;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

@Controller
public class DataController {

    @GetMapping("/data")
    public @ResponseBody Iterable<Site> greeting() {
        return new ArrayList<>();
    }

}
