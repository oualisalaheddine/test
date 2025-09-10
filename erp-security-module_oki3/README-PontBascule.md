# Module Pont Bascule – Guide d’utilisation

Ce document explique comment utiliser le module Pont Bascule ajouté à l’application, comment sécuriser ses écrans et comment initialiser les permissions côté JPA (sans SQL).

## 1) Accès et navigation

- Contexte applicatif: `server.servlet.context-path=/erp`
- URL racine du module: `http://localhost:8080/erp/pontbascule`
- Écrans fournis (Thymeleaf):
  - `pontbascule/index.html`: liste Produits, Partenaires, Opérations + liens de création/modification/suppression
  - `pontbascule/produits/form.html`: création/édition d’un produit
  - `pontbascule/partenaires/form.html`: création/édition d’un partenaire
  - `pontbascule/operations/form.html`: création/édition d’une opération (calcul auto de poids net et écart)

## 2) Sécurité (backend)

La sécurité est gérée par `com.sh.erpcos.univers.securite`.

- Règle HTTP (déjà en place dans `SecurityConfig`):
  - `"/pontbascule/**"` requiert les autorités: `PONTBASCULE_LIRE`, `PONTBASCULE_CREER`, `PONTBASCULE_MODIFIER`, `PONTBASCULE_SUPPRIMER`
- Contrôleur: `PontBasculeController` utilise `@PreAuthorize` par action

Pour que cela fonctionne, il faut créer les permissions en base (via JPA) et les assigner aux rôles pertinents.

### Initialisation des permissions via JPA (sans SQL)

Exemple minimal avec un `CommandLineRunner` temporaire (à retirer après exécution):

```java
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.sh.erpcos.univers.securite.service.PermissionService;
import com.sh.erpcos.univers.securite.service.RoleService;
import com.sh.erpcos.univers.securite.entity.Permission;

@Configuration
class PontBasculePermissionsInit {
    @Bean
    CommandLineRunner initPontBasculePerms(PermissionService permissionService, RoleService roleService) {
        return args -> {
            // Créer (si absent) les permissions du module
            String module = "PONTBASCULE";
            String[] actions = {"LIRE","CREER","MODIFIER","SUPPRIMER"};
            for (String action : actions) {
                Permission p = new Permission();
                p.setNom(module + "_" + action);   // ex: PONTBASCULE_LIRE
                p.setNomModule(module);
                p.setNomAction(action);
                p.setActif(true);
                // La PermissionService devrait gérer idempotence (à adapter selon votre code)
                permissionService.createPermissionIfNotExists(p);
            }

            // Assigner toutes les permissions au rôle ADMIN (adapter selon vos rôles)
            roleService.getRoleByNom("ADMIN").ifPresent(admin -> {
                var perms = permissionService.getPermissionsActives().stream()
                    .filter(pp -> module.equals(pp.getNomModule()))
                    .map(Permission::getId)
                    .collect(java.util.stream.Collectors.toSet());
                roleService.assignerPermissions(admin.getId(), perms);
            });
        };
    }
}
```

Notes:
- Si votre `PermissionService` ne dispose pas de `createPermissionIfNotExists`, vous pouvez appeler la méthode de création existante et ignorer l’erreur si la permission existe déjà, ou enrichir le service.
- Une fois l’initialisation effectuée, supprimez le `CommandLineRunner`.

## 3) Données et validations

- Entités principales (package `com.sh.erpcos.module.pontbascule.entity`):
  - `Produit` (désignation unique, `@NotBlank`, `@Size(max=150)`)
  - `Partenaire` (raison sociale obligatoire, `@NotBlank`, `@Size(max=150)`)
  - `Operation` (références `Produit`, `Partenaire`, `TypeOperation`, dates système, calculs `poidsNet=peser2-peser1`, `ecart=poidsNet-qteDeclare`)
- Validations côté serveur activées pour Produits/Partenaires (affichage d’erreurs sur les formulaires). Vous pouvez étendre aux autres champs (ex: `peser1 > 0`).

## 4) Contrôleur et services

- Contrôleur: `com.sh.erpcos.module.pontbascule.controller.PontBasculeController`
  - Routes: `/pontbascule`, CRUD Produits/Partenaires/Opérations
- Services: CRUD simples par entité (dans `com.sh.erpcos.module.pontbascule.service`)
- Repositories: Spring Data JPA (dans `com.sh.erpcos.module.pontbascule.repository`)

## 5) Démarrage & authentification

- Lancez l’app puis connectez-vous sur `/erp/login`
- Les utilisateurs/roles/permissions proviennent du module sécurité existant; assurez-vous que l’utilisateur connecté possède les autorités `PONTBASCULE_*`.

## 6) Personnalisation

- Pour intégrer au layout global, adaptez les vues `templates/pontbascule/**` (inclusion du layout, styles, menus)
- Pour limiter l’exposition des noms d’autorisations dans les vues, utilisez des indicateurs calculés côté backend et des `th:if` sur le menu.

## 7) Dépannage

- 403 sur `/erp/pontbascule/**`:
  - Vérifier que les permissions `PONTBASCULE_*` existent et sont assignées au rôle de l’utilisateur
  - Vérifier les logs sécurité (`logging.level.org.springframework.security=DEBUG` en dev)
- Échec login:
  - Vérifier hash BCrypt du mot de passe et les flags du compte (actif, non verrouillé, etc.)

---
Ce module est conçu pour être extensible. Reprenez le même schéma pour d’autres domaines: créez les permissions, protégez `SecurityConfig`, ajoutez contrôleur/services/vues, puis assignez les droits aux rôles.
