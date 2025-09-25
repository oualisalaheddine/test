package com.sh.erpcos.module.pontbascule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "type_partenaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypePartenaire {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String libelle; // CLIENT | FOURNISSEUR.......AUTRES
}


