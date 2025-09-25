package com.sh.erpcos.module.pontbascule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "operations_en_cours_externe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationEnCoursExterne {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicules_externe_immatriculation")
	private VehiculeExterne vehiculeExterne;

	@Column
	private Integer peser_charger; // > 0
	@Column
	private Integer peser_vide; // > 0
}


