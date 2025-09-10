package com.sh.erpcos.module.pontbascule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "operations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_produit")
	private Produit produit;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "partenaire_id")
	private Partenaire partenaire;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "type_operations_id")
	private TypeOperation typeOperation;

	@Column(name = "date_operation")
	private LocalDate dateOperation;

	@Column(name = "heure_operation")
	private LocalTime heureOperation;

	@Column(length = 30)
	private String immatriculation;

	@Column(nullable = false)
	private Integer peser1;

	@Column
	private Integer peser2;

	@Column(name = "poids_net")
	private Integer poidsNet;

	@Column(name = "bl_numero")
	private String blNumero;

	@Column(name = "date_bl")
	private LocalDate dateBl;

	@Column(name = "facture_numero")
	private String factureNumero;

	@Column(name = "date_facture")
	private LocalDate dateFacture;

	@Column(name = "qte_declare")
	private Integer qteDeclare;

	@Column(name = "ecart")
	private Integer ecart;

	@Column
	private String commentaire;

	@Column(name = "cree_par_id")
	private String creeParId;

	@Column(name = "annuler_par")
	private String annulerPar;

	@Column(name = "date_annulation")
	private LocalDateTime dateAnnulation;
	
	@Transient
    private Integer operationEnCoursId;
}


