package com.sh.erpcos.univers.securite.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String nom;
    
    @Column(length = 200)
    private String description;
    
    @Column(name = "niveau_hierarchie")
    private Integer niveauHierarchie = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_parent_id")
    private Role roleParent;
    
    @OneToMany(mappedBy = "roleParent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Role> rolesEnfants = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
    
    @Column(name = "role_actif")
    private boolean roleActif = true;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}
