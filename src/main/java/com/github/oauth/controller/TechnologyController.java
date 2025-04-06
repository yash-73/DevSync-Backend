package com.github.oauth.controller;

import com.github.oauth.service.TechService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/tech")
public class TechnologyController {
    
    private TechService techService;

    public TechnologyController (TechService techService){
        this.techService = techService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin/addTech")
    public ResponseEntity<Set<String>> addTech(@RequestBody Set<String> technologies){
               Set<String> addedTechnology = techService.addTech(technologies);
                return ResponseEntity.ok(addedTechnology);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/seeTech")
    public ResponseEntity<Set<String>> seeTech(@RequestBody Set<String> technologies){
        return ResponseEntity.ok(technologies);
    }


}