package com.sh.erpcos.univers.securite.enums;

//Enum pour les actions standard
public enum ActionType {
 LIRE("Lecture", 1),
 CREER("Création", 2),
 MODIFIER("Modification", 3),
 SUPPRIMER("Suppression", 4),
 EXPORTER("Exportation", 5),
 IMPORTER("Importation", 6);
 
 private final String libelle;
 private final int niveauPriorite;
 
 ActionType(String libelle, int niveauPriorite) {
     this.libelle = libelle;
     this.niveauPriorite = niveauPriorite;
 }
 
 // Getters
 public String getLibelle() { return libelle; }
 public int getNiveauPriorite() { return niveauPriorite; }
}