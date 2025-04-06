package com.github.oauth.repository;

import com.github.oauth.model.Tech;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TechRepository extends JpaRepository<Tech, Integer> {

    Tech findByTechName(String technology);
}
