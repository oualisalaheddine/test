package com.sh.erpcos.module.pontbascule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "vehicules_tarer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculeTarer {

	
	@Id
	@Column(nullable = false, length = 30)
	private String immatriculation;

	@Column(nullable = false)
	private Integer peser1; // > 0

	@Column(name = "tare_date")
	private LocalDate tareDate;
}


