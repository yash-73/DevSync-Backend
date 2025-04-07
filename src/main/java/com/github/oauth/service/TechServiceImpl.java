package com.github.oauth.service;



import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.github.oauth.model.Tech;
import com.github.oauth.repository.TechRepository;

@Service
public class TechServiceImpl implements  TechService{

    private TechRepository techRepository;


    public TechServiceImpl(TechRepository techRepository){
        this.techRepository = techRepository;
    }

    @Override
    public Set<String> addTech(Set<String> technologies) {
                    Set<Tech> techSet = technologies.stream().map(
                            technology -> {
                                Tech tech = techRepository.findByTechName(technology);
                                if(tech == null){
                                    Tech newTech = new Tech(technology);
                                    techRepository.save(newTech);
                                    tech = techRepository.findByTechName(technology);
                                }
                                return tech;
                            }
                    ).collect(Collectors.toSet());

                    return techSet.stream().map(Tech::getTechName).collect(Collectors.toSet());

    }
}