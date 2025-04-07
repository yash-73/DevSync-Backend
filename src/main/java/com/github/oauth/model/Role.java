package com.github.oauth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(value = EnumType.STRING)
    @Column(unique = true, nullable = false)
    private AppRole roleName;

    public Role(AppRole roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return roleName.name();
    }
}
