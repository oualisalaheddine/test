package com.sh.erpcos.univers.securite.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "utilisateurs",
uniqueConstraints = {
    @UniqueConstraint(name = "uk_utilisateur_username", columnNames = "username"),
    @UniqueConstraint(name = "uk_utilisateur_email", columnNames = "email")
}
)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, length = 100)
    private String nom;
    
    @Column(nullable = false, length = 100)
    private String prenom;
    
    @Column(nullable = false, length = 100)
    private String email;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;
    
    @Column(name = "compte_actif")
    private boolean compteActif = true;
    
    @Column(name = "compte_non_expire")
    private boolean compteNonExpire = true;
    
    @Column(name = "compte_non_verrouille")
    private boolean compteNonVerrouille = true;
    
    @Column(name = "credentials_non_expire")
    private boolean credentialsNonExpire = true;
    
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "utilisateur_roles",
        joinColumns = @JoinColumn(name = "utilisateur_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}
