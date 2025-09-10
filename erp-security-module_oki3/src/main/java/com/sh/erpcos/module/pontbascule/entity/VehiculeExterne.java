package com.sh.erpcos.module.pontbascule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicules_externe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeExterne {

	@Id
	@Column(length = 30)
	private String immatriculation;
}


