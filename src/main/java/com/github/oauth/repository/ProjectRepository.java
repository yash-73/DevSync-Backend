package com.github.oauth.repository;

import com.github.oauth.model.Project;
import com.github.oauth.model.Tech;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Project findByProjectName(String name);

    @Query("SELECT p FROM Project p " +
           "JOIN p.techStack pt " +
           "WHERE pt IN :techStack " +
           "GROUP BY p " +
           "ORDER BY COUNT(pt) DESC")
    List<Project> findByTechStackOrderByMatchCount(@Param("techStack") Set<Tech> techStack);
}
