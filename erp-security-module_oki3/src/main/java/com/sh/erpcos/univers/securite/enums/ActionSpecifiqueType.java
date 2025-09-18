package com.sh.erpcos.univers.securite.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//Enum pour les actions spécifiques par module
public enum ActionSpecifiqueType {
 // Sécurité
 ASSIGNER_ROLES("Assigner des rôles", ModuleType.SECURITE, 5),
 GESTION_PERMISSIONS("Gérer les permissions", ModuleType.SECURITE, 6),
 
 // RH
 VALIDER_CONGE("Valider les congés", ModuleType.RH, 5),
 GESTION_SALAIRE("Gérer les salaires", ModuleType.RH, 6),
 
 // Vente
 VALIDER("Valider les ventes", ModuleType.VENTE, 5),
 FACTURER("Facturer", ModuleType.VENTE, 6),
 REMBOURSER("Rembourser", ModuleType.VENTE, 7);
 
 private final String libelle;
 private final ModuleType module;
 private final int niveauPriorite;
 
 ActionSpecifiqueType(String libelle, ModuleType module, int niveauPriorite) {
     this.libelle = libelle;
     this.module = module;
     this.niveauPriorite = niveauPriorite;
 }
 
 // Getters
 public String getLibelle() { return libelle; }
 public ModuleType getModule() { return module; }
 public int getNiveauPriorite() { return niveauPriorite; }
 
 public static List<ActionSpecifiqueType> getActionsForModule(ModuleType module) {
     return Arrays.stream(values())
             .filter(action -> action.getModule() == module)
             .collect(Collectors.toList());
 }
}