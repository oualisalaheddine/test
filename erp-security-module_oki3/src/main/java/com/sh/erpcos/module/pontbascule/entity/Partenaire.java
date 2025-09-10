package com.sh.erpcos.module.pontbascule.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "partenaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Partenaire {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 150)
	@Column(name = "raison_sociale", nullable = false, unique = true, length = 150)
	private String raisonSociale;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "type_partenaire_id")
	private TypePartenaire typePartenaire;
}


