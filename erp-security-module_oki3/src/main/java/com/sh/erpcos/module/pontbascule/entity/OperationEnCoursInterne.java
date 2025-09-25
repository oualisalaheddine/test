package com.sh.erpcos.module.pontbascule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "operations_en_cours_interne")
@Data
@NoArgsConstructor
@AllArgsConstructor



public class OperationEnCoursInterne {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	//private String immatriculation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vehicules_tarer_immatriculation")
	private VehiculeTarer vehiculeTarer;
}


