📚 Vue d’ensemble du modèle de sécurité
EntitéTable (PostgreSQL)Relations clésUtilisateurutilisateurs• @ManyToMany roles (utilisateur_roles – table de jointure) • Chaque utilisateur possède un login (username) et un mot de passe (encodé).Roleroles• @ManyToMany permissions (role_permissions – table de jointure) • Un rôle possède un nom (ex. ADMIN, MANAGER, USER, READER).Permissionpermissions• Définie par module, action (LIRE, CREER, MODIFIER, SUPPRIMER) et nomAction. • Les permissions sont généralement créées par module (ex. VENTE, ACHAT, CLIENT).
Schéma relationnel (simplifié)
utilisateurs
   └─ id (PK)
   └─ username
   └─ ...

roles
   └─ id (PK)
   └─ nom
   └─ ...

permissions
   └─ id (PK)
   └─ module
   └─ nomAction
   └─ ...

-- Table de jointure Utilisateur‑Role
utilisateur_roles
   └─ utilisateur_id   (FK → utilisateurs.id)
   └─ role_id          (FK → roles.id)

-- Table de jointure Role‑Permission
role_permissions
   └─ role_id          (FK → roles.id)
   └─ permission_id    (FK → permissions.id)


Remarque : toutes ces relations sont déclarées dans les entités avec @ManyToMany (ou @ManyToOne / @OneToMany selon le besoin).
Spring JPA crée automatiquement les tables de jointure grâce à @JoinTable.


🛠️ Comment remplir la base de données ?
Le TestDataController que vous avez partagé permet d’initialiser pas à pas :
Méthode (URL)ActionRésultat attenduGET /securite/testAffiche le tableau de bord avec le nombre d’enregistrements actuelsVous voyez nbPermissions, nbRoles, nbUtilisateurs.POST /securite/test/init-permissionsCrée toutes les permissions de test (un pour chaque module/action)permissions = nombre total de permissions (ex. 4 actions × N modules).POST /securite/test/init-rolesCrée 4 rôles (ADMIN, MANAGER, USER, READER)roles = 4.POST /securite/test/assign-permissionsAssigne les permissions aux rôles : • ADMIN → toutes • MANAGER → tout sauf SUPPRIMER • USER → LIRE + CREER • READER → LIRE uniquementrole_permissions remplie selon la règle.POST /securite/test/init-usersCrée 4 comptes utilisateurs : admin / admin123 manager / manager123 user / user123 reader / reader123 et leur assigne le rôle correspondantutilisateurs = 4 et les jointures utilisateur_roles.POST /securite/test/init-allEnchaîne tout (permissions → rôles → assignation → utilisateurs)Vous avez un jeu de données complet et fonctionnel en un seul appel.
1️⃣ Utiliser le contrôleur via le navigateur

1. Lancez l’application (mvn spring-boot:run ou depuis votre IDE).
2. Ouvrez : http://localhost:8080/securite/test (ou /erp/securite/test si le pré‑préfixe erp est configuré).
3. Cliquez sur les boutons Init Permissions, Init Roles, Assign Permissions, Init Users ou Init All.
4. Les messages flash (successMessage / errorMessage) apparaissent en haut de la page – vous avez la confirmation que chaque étape a fonctionné.

2️⃣ Utiliser curl / Postman (sans UI)
bashDownloadCopy code Wrap# 1️⃣ Crée les permissions
curl -X POST http://localhost:8080/securite/test/init-permissions -i

# 2️⃣ Crée les rôles
curl -X POST http://localhost:8080/securite/test/init-roles -i

# 3️⃣ Assigne les permissions aux rôles
curl -X POST http://localhost:8080/securite/test/assign-permissions -i

# 4️⃣ Crée les utilisateurs
curl -X POST http://localhost:8080/securite/test/init-users -i

# OU tout en une fois
curl -X POST http://localhost:8080/securite/test/init-all -i

Important : les appels POST n’ont pas de corps (les données sont générées en dur dans le contrôleur).
Si vous avez besoin d’un payload personnalisé, vous pouvez créer une version REST (JSON) du même service – voir la section Améliorations possibles plus bas.


🧑‍💻 Exploration du code (extraits clés)
### Création d’un Permission (Service)
javaDownloadCopy code Wrap@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;

    /** Crée toutes les permissions pour chaque module (ex. VENTE, ACHAT, CLIENT…) */
    public void creerToutesPermissionsModules() {
        List<String> modules = List.of("VENTE", "ACHAT", "CLIENT", "FACTURE");
        List<String> actions = List.of("LIRE", "CREER", "MODIFIER", "SUPPRIMER");
        modules.forEach(m -> actions.forEach(a -> {
            Permission p = new Permission();
            p.setNomModule(m);
            p.setNomAction(a);
            p.setPermissionActif(true);
            permissionRepository.save(p);
        }));
    }
}
### Création d’un Role et affectation des permissions
javaDownloadCopy code Wrap@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /** Assigne un ensemble d’IDs de permission à un rôle */
    public void assignerPermissions(Long roleId, Set<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        Set<Permission> perms = permissionRepository.findAllById(permissionIds)
                .stream().collect(Collectors.toSet());
        role.setPermissions(perms);
        roleRepository.save(role);
    }
}
### Création d’un Utilisateur et liaison aux rôles
javaDownloadCopy code Wrap@Service
@RequiredArgsConstructor
public class UtilisateurService {
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;   // Spring‑Security

    public Utilisateur createUtilisateur(Utilisateur u) {
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        return utilisateurRepository.save(u);
    }

    /** Ajoute un ou plusieurs rôles à l’utilisateur */
    public void assignerRoles(Long utilisateurId, Set<Long> roleIds) {
        Utilisateur user = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Set<Role> roles = roleRepository.findAllById(roleIds).stream()
                .collect(Collectors.toSet());
        user.getRoles().addAll(roles);
        utilisateurRepository.save(user);
    }
}

Le contrôleur n’a pas besoin de connaître les détails du PasswordEncoder ; tout est encapsulé dans le service.


🔧 Améliorations / Bonnes pratiques
ProblèmeSolution proposéeEndpoint uniquement accessible via UIExposer une API REST (/api/security/init-all) qui accepte du JSON (facultatif).Duplication de logique (init‑permissions → init‑roles → …)Créez un CommandLineRunner ou @PostConstruct qui s’exécute une fois au démarrage (utile en dev). Exemple : java @Component public class DataInitializer implements CommandLineRunner { private final TestDataController testCtrl; @Override public void run(String... args) { testCtrl.initAll(new RedirectAttributesModelMap()); } }Mot de passe en clair dans le codeUtilisez un PasswordEncoder (BCrypt) pour encoder lors de la création du compte, comme déjà fait dans le service.Hard‑coded noms de rôlesCentralisez les constantes dans une enum (enum RoleName { ADMIN, MANAGER, USER, READER }).Gestion d’erreursRetourner un ResponseEntity avec code HTTP (ex. 201 Created, 400 Bad Request) plutôt que de faire des redirections dans un contrôleur @Controller.Tests automatisésÉcrivez des tests d’intégration (@SpringBootTest) qui invoquent initAll() et vérifient que les tables contiennent le nombre attendu d’enregistrements.

📄 README.md généré
markdownDownloadCopy code Wrap# ERP Security Module – Jeu de données de test

## 📖  Sommaire
1. [Description du modèle](#description-du-modèle)
2. [Architecture du projet](#architecture-du-projet)
3. [Installation & démarrage](#installation--démarrage)
4. [Initialisation des données de sécurité](#initialisation-des-données-de-sécurité)
5. [Endpoints disponibles (UI & API)](#endpoints-disponibles-ui--api)
6. [Vérifier le contenu en base](#vérifier-le-contenu-en-base)
7. [Bonnes pratiques & améliorations futures](#bonnes-pratiques--améliorations-futures)

---

## 🎯 Description du modèle

| Entité | Table | Rôle |
|--------|-------|------|
| `Utilisateur` | `utilisateurs` | Représente un compte utilisateur (login, mot de passe, e‑mail). |
| `Role` | `roles` | Ensemble de permissions attribuées à un ou plusieurs utilisateurs. |
| `Permission` | `permissions` | Action autorisée sur un module (ex. `VENTE` + `LIRE`). |
| Jointures | `utilisateur_roles`, `role_permissions` | Relations **many‑to‑many** entre `Utilisateur`↔`Role` et `Role`↔`Permission`. |

*Un utilisateur peut posséder plusieurs rôles et chaque rôle regroupe plusieurs permissions.*  

![Diagramme ER (simplifié)](docs/er-diagram.png)

---

## 🏗️ Architecture du projet

src/main/java
└─ com.sh.erpcos.univers.securite
├─ entity          ← JPA @Entity (Utilisateur, Role, Permission, …)
├─ repository      ← Spring Data JPA interfaces
├─ service         ← Logique métier (CRUD, assignations, encodage pwd)
└─ controller
└─ TestDataController   ← Initialise les données de test (UI)

*Le module **security** ne dépend que de Spring‑Boot, Spring‑Security et Spring‑Data JPA.*

---

## 🚀 Installation & démarrage

```bash
# 1️⃣ cloner le dépôt
git clone https://github.com/ton-repo/erp-security-module.git
cd erp-security-module

# 2️⃣ construire le jar
./mvnw clean package

# 3️⃣ créer la base (PostgreSQL)
#    (le script schema.sql est généré par Hibernate à la première exécution)
#    assurez‑vous d’avoir un DB `erp_security` et le user adéquat
psql -U postgres -d erp_security -f src/main/resources/schema.sql   # optionnel

# 4️⃣ lancer l'application
java -jar target/erp-security-module-0.0.1-SNAPSHOT.jar
````
L’application démarre sur localhost:8080 (modifiable via application.yml).

⚙️ Initialisation des données de sécurité
1️⃣ Via l’interface web (recommandé en dev)
URLActionGET  /securite/testTableau de bord – affiche le nombre actuel d’enregistrements.POST /securite/test/init-permissionsCrée toutes les permissions (module × action).POST /securite/test/init-rolesCrée les 4 rôles de base.POST /securite/test/assign-permissionsAssigne les permissions aux rôles (logique décrite plus haut).POST /securite/test/init-usersCrée 4 utilisateurs (admin, manager, user, reader).POST /securite/test/init-allExécute les 4 étapes précédentes d’un coup.

Astuce : sur la page /securite/test vous avez des boutons qui déclenchent chaque POST.

2️⃣ Via curl / Postman (sans UI)
bashDownloadCopy code Wrapcurl -X POST http://localhost:8080/securite/test/init-all -i
3️⃣ (Option) Auto‑initialisation au démarrage
Ajoutez (en dev uniquement) un CommandLineRunner :
javaDownloadCopy code Wrap@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TestDataController testCtrl;

    @Override
    public void run(String... args) {
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        testCtrl.initAll(attrs);
    }
}

Ce runner s’exécutera une seule fois au lancement de l’application.
Supprimez‑le en production pour éviter d’écraser les données réelles.


📡 Endpoints disponibles (UI & API)
MéthodeCheminRetourUsageGET/securite/testVue Thymeleaf (securite/test-data.html)Dashboard.POST/securite/test/init-permissionsRedirection + flash messageCréation des permissions.POST/securite/test/init-rolesRedirection + flash messageCréation des rôles.POST/securite/test/assign-permissionsRedirection + flash messageAssignation des permissions aux rôles.POST/securite/test/init-usersRedirection + flash messageCréation des comptes utilisateurs.POST/securite/test/init-allRedirection + flash messageInitialise tout d’un coup.

NOTE : Tous les POST retournent une redirection (302) vers le dashboard avec un message de succès ou d’erreur dans la session (RedirectAttributes).


🔎 Vérifier le contenu en base
sqlDownloadCopy code Wrap-- Comptage
SELECT COUNT(*) FROM permissions;   -- > 4 * N modules
SELECT COUNT(*) FROM roles;         -- > 4
SELECT COUNT(*) FROM utilisateurs; -- > 4

-- Vérifier les affectations
SELECT u.username, r.nom
FROM utilisateurs u
JOIN utilisateur_roles ur ON u.id = ur.utilisateur_id
JOIN roles r ON ur.role_id = r.id;

SELECT r.nom, p.nom_module, p.nom_action
FROM roles r
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
ORDER BY r.nom;

📈 Bonnes pratiques & améliorations futures

1. Séparer le UI du service : créer un @RestController qui renvoie du JSON.
2. Gestion d’erreurs centralisée : @ControllerAdvice pour transformer toutes les Exception en réponses HTTP 4xx/5xx.
3. Configuration via application‑properties :
propertiesDownloadCopy code Wrapsecurity.testdata.enabled=true   # désactiver en prod
Puis conditionner le DataInitializer avec @ConditionalOnProperty.
4. Tests d’intégration : assurez‑vous que initAll() crée bien les 4 utilisateurs et les jointures attendues.
5. Migrations Flyway / Liquibase : versionnez le schéma de base et les scripts d’insertion de données de test.


📌  Résumé

* Vous avez maintenant un moyen clair de créer permissions → rôles → utilisateurs dans votre base.
* Le contrôleur existant fonctionne, mais vous pouvez le rendre plus réutilisable (API, CommandLineRunner).
* Le README ci‑dessus décrit le modèle, les étapes d’installation et les appels à faire.

🔧 Prêt à passer à la suite ?

* Testez la route POST /securite/test/init-all et vérifiez la base avec les requêtes SQL ci‑dessus.
* Si vous souhaitez exposer un vrai endpoint REST ou ajouter des tests automatisés, dites‑le‑moi : je vous fournirai le code exact.

🚀 Bonne continuation avec votre module de sécurité !