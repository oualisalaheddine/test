package com.sh.erpcos.univers.securite.entity;

import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "modules")
@Getter
@Setter
@ToString(exclude = "permissions") // Exclure les permissions pour éviter les références circulaires
@NoArgsConstructor
@AllArgsConstructor
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    private String nom;
    private String description;
    private boolean actif = true;
    private String icone;
    
    @Column(name = "url_pattern")
    private String urlPattern;
    
    // Champs pour tracer les créations/modifications
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    // Champ pour indiquer si c'est un module système (défini dans l'enum)
    @Column(name = "module_systeme")
    private boolean moduleSysteme = false;
    
    // Relation avec les permissions
    @OneToMany(mappedBy = "module")
    private Set<Permission> permissions = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}