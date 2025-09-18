package com.sh.erpcos.module.pontbascule.controller;

import com.sh.erpcos.module.pontbascule.entity.Operation;
import com.sh.erpcos.module.pontbascule.entity.OperationEnCoursExterne;
import com.sh.erpcos.module.pontbascule.entity.OperationEnCoursInterne;
import com.sh.erpcos.module.pontbascule.entity.Partenaire;
import com.sh.erpcos.module.pontbascule.entity.Produit;
import com.sh.erpcos.module.pontbascule.entity.TypeOperation;
import com.sh.erpcos.module.pontbascule.entity.VehiculeTarer;
import com.sh.erpcos.module.pontbascule.service.OperationEncoursExterneService;
import com.sh.erpcos.module.pontbascule.service.OperationEncoursInterneService;
import com.sh.erpcos.module.pontbascule.service.OperationService;
import com.sh.erpcos.module.pontbascule.service.PartenaireService;
import com.sh.erpcos.module.pontbascule.service.ProduitService;
import com.sh.erpcos.module.pontbascule.service.TypeOperationService;
import com.sh.erpcos.module.pontbascule.service.VehiculeExterneService;
import com.sh.erpcos.module.pontbascule.service.VehiculeTarerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/pontbascule")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PONTBASCULE_LIRE')")
public class PontBasculeController {

	private final ProduitService produitService;
	private final PartenaireService partenaireService;
	private final TypeOperationService typeOperationService;
	private final OperationService operationService;
	private final OperationEncoursExterneService operationEncoursExterneService;
	private final OperationEncoursInterneService operationEncoursInterneService;
	private final VehiculeTarerService vehiculeTarerService;
	private final VehiculeExterneService vehiculeExterneService;
	
	@GetMapping
	public String index(Model model) {
		model.addAttribute("produits", produitService.findAll());
		model.addAttribute("partenaires", partenaireService.findAll());
		model.addAttribute("operations", operationService.findAll());
		return "pontbascule/index";
	}

	// PRODUITS CRUD
	@GetMapping("/produits"
			+ "")
	@PreAuthorize("hasAuthority('PONTBASCULE_LIRE')")
	public String listProduits(Model model) {
	    model.addAttribute("produits", produitService.findAll());
	    return "pontbascule/produits/produits";
	}

	@GetMapping("/produits/nouveau")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String newProduit(Model model) {
		model.addAttribute("produit", new Produit());
		return "pontbascule/produits/form";
	}

	@PostMapping("/produits")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String createProduit(@Valid @ModelAttribute Produit produit, BindingResult br) {
		if (br.hasErrors()) return "pontbascule/produits/form";
		produitService.save(produit);
		return "redirect:/pontbascule";
	}

	@GetMapping("/produits/{id}/modifier")
	@PreAuthorize("hasAuthority('PONTBASCULE_MODIFIER')")
	public String editProduit(@PathVariable Long id, Model model) {
		Produit produit = produitService.
				findById(id).orElseThrow();
		model.addAttribute("produit", produit);
		return "pontbascule/produits/form";
	}

	@PostMapping("/produits/{id}")
	@PreAuthorize("hasAuthority('PONTBASCULE_MODIFIER')")
	public String updateProduit(@PathVariable Long id, @Valid @ModelAttribute Produit produit, BindingResult br) {
		if (br.hasErrors()) return "pontbascule/produits/form";
		produit.setIdProduit(id);
		produitService.save(produit);
		return "redirect:/pontbascule";
	}

	@PostMapping("/produits/{id}/supprimer")
	@PreAuthorize("hasAuthority('PONTBASCULE_SUPPRIMER')")
	public String deleteProduit(@PathVariable Long id) {
		produitService.deleteById(id);
		return "redirect:/pontbascule";
	}

	// PARTENAIRES CRUD
	@GetMapping("/partenaires/nouveau")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String newPartenaire(Model model) {
		model.addAttribute("partenaire", new Partenaire());
		return "pontbascule/partenaires/form";
	}

	@PostMapping("/partenaires")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String createPartenaire(@Valid @ModelAttribute Partenaire partenaire, BindingResult br) {
		if (br.hasErrors()) return "pontbascule/partenaires/form";
		partenaireService.save(partenaire);
		return "redirect:/pontbascule";
	}

	@GetMapping("/partenaires/{id}/modifier")
	@PreAuthorize("hasAuthority('PONTBASCULE_MODIFIER')")
	public String editPartenaire(@PathVariable Long id, Model model) {
		Partenaire partenaire = partenaireService.findById(id).orElseThrow();
		model.addAttribute("partenaire", partenaire);
		return "pontbascule/partenaires/form";
	}

	@PostMapping("/partenaires/{id}")
	@PreAuthorize("hasAuthority('PONTBASCULE_MODIFIER')")
	public String updatePartenaire(@PathVariable Long id, @Valid @ModelAttribute Partenaire partenaire, BindingResult br) {
		if (br.hasErrors()) return "pontbascule/partenaires/form";
		partenaire.setId(id);
		partenaireService.save(partenaire);
		return "redirect:/pontbascule";
	}

	@PostMapping("/partenaires/{id}/supprimer")
	@PreAuthorize("hasAuthority('PONTBASCULE_SUPPRIMER')")
	public String deletePartenaire(@PathVariable Long id) {
		partenaireService.deleteById(id);
		return "redirect:/pontbascule";
	}

	// OPERATIONS CRUD
	@GetMapping("/operations/nouveau")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String newOperation(@RequestParam(required = false) Integer operationInterneId,Model model) {
		/**
		Operation operation = new Operation();
		 if (operationInterneId != null) {
		        OperationEnCoursInterne operationInterne = operationEncoursInterneService.findById(operationInterneId)
		                .orElseThrow(() -> new IllegalArgumentException("OperationEnCoursInterne introuvable"));
		        operation.setImmatriculation(operationInterne.getVehiculeTarer().getImmatriculation());
		        operation.setPeser1(operationInterne.getVehiculeTarer().getPeser1());
		        System.out.println("operationInterneId = " + operationInterneId);
		    }
		 model.addAttribute("operation", operation);
		model.addAttribute("produits", produitService.findAll());
		model.addAttribute("partenaires", partenaireService.findAll());
		model.addAttribute("types", typeOperationService.findAll());
		 System.out.println("operationInterneId = " + operationInterneId);
		return "pontbascule/operations/form";
	**/
		Operation operation = operationService.initFromInterne(operationInterneId);

	    model.addAttribute("operation", operation);
	    model.addAttribute("produits", produitService.findAll());
	    model.addAttribute("partenaires", partenaireService.findAll());
	    model.addAttribute("types", typeOperationService.findAll());

	    return "pontbascule/operations/form";
	}
	/**
	@PostMapping("/operations")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String createOperation(@RequestParam Long produitId,
	                             @RequestParam Long partenaireId,
	                             @RequestParam Integer typeOperationId,
	                             @ModelAttribute Operation operation,
	                             Authentication auth) {

	    String currentUser = auth != null ? auth.getName() : "system";
	    
	    operationService.createOperation(produitId, partenaireId, typeOperationId, 
	                                    operation, currentUser);

	    return "redirect:/erp/pontbascule";
	}
	**/
	@PostMapping("/operations")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String createOperation(@ModelAttribute Operation operation,@RequestParam Long produitId,
            @RequestParam Long partenaireId, @RequestParam Integer typeOperationId,Authentication auth) {
	    operationService.saveOperation(operation,produitId,partenaireId,typeOperationId, auth);
	    return "redirect:/pontbascule";
	}
	/**

	@PostMapping("/operations")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String createOperation(@RequestParam Long produitId,
	                             @RequestParam Long partenaireId,
	                             @RequestParam Integer typeOperationId,
	                             @ModelAttribute Operation operation,
	                             Authentication auth) {
		// Attacher les entités associées par ID
		operation.setProduit(produitService.findById(produitId).orElseThrow());
		operation.setPartenaire(partenaireService.findById(partenaireId).orElseThrow());
		operation.setTypeOperation(typeOperationService.findAll().stream()
				.filter(t -> t.getId().equals(typeOperationId))
				.findFirst().orElseThrow());
		// dates système
		operation.setDateOperation(LocalDate.now());
		operation.setHeureOperation(LocalTime.now());
		// calculs
		if (operation.getPeser1() != null && operation.getPeser2() != null) {
			operation.setPoidsNet(operation.getPeser2() - operation.getPeser1());
		}
		if (operation.getPoidsNet() != null && operation.getQteDeclare() != null) {
			operation.setEcart(operation.getPoidsNet() - operation.getQteDeclare());
		}
		// audit
		operation.setCreeParId(auth != null ? auth.getName() : null);
		operationService.save(operation);
		return "redirect:/erp/pontbascule";
	}
**/
	@GetMapping("/operations/{id}/modifier")
	@PreAuthorize("hasAuthority('PONTBASCULE_MODIFIER')")
	public String editOperation(@PathVariable Long id, Model model) {
		Operation op = operationService.findById(id).orElseThrow();
		model.addAttribute("operation", op);
		model.addAttribute("produits", produitService.findAll());
		model.addAttribute("partenaires", partenaireService.findAll());
		model.addAttribute("types", typeOperationService.findAll());
		return "pontbascule/operations/form";
	}

	@PostMapping("/operations/{id}")
	@PreAuthorize("hasAuthority('PONTBASCULE_MODIFIER')")
	public String updateOperation(@PathVariable Long id,
	                             @RequestParam Long produitId,
	                             @RequestParam Long partenaireId,
	                             @RequestParam Integer typeOperationId,
	                             @ModelAttribute Operation operation) {
		operation.setId(id);
		operation.setProduit(produitService.findById(produitId).orElseThrow());
		operation.setPartenaire(partenaireService.findById(partenaireId).orElseThrow());
		operation.setTypeOperation(typeOperationService.findAll().stream()
				.filter(t -> t.getId().equals(typeOperationId))
				.findFirst().orElseThrow());
		if (operation.getPeser1() != null && operation.getPeser2() != null) {
			operation.setPoidsNet(operation.getPeser2() - operation.getPeser1());
		}
		if (operation.getPoidsNet() != null && operation.getQteDeclare() != null) {
			operation.setEcart(operation.getPoidsNet() - operation.getQteDeclare());
		}
		operationService.save(operation);
		return "redirect:/pontbascule";
	}

	@PostMapping("/operations/{id}/supprimer")
	@PreAuthorize("hasAuthority('PONTBASCULE_SUPPRIMER')")
	public String deleteOperation(@PathVariable Long id) {
		operationService.deleteById(id);
		return "redirect:/pontbascule";
	}
	
	
	// OPERATIONS ENCOURS EXTERNE CRUD
	@GetMapping("/operations-externes")
	@PreAuthorize("hasAuthority('PONTBASCULE_LIRE')")
	public String listOperationsExternes(Model model) {
		model.addAttribute("operationsInternes", operationEncoursInterneService.findAll());
	    model.addAttribute("operationsExternes", operationEncoursExterneService.findAll());
	    return "pontbascule/operations-internes/index";
	}

	@GetMapping("/operations-externes/nouveau")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String newOperationExterne(Model model) {
	    model.addAttribute("operationExterne", new OperationEnCoursExterne());
	    model.addAttribute("vehiculeExterne", vehiculeExterneService.findAll());
	    return "pontbascule/operations-externes/form";
	    //return "pontbascule/operations-externes/formExterneFragment :: formExterne";
	}

	@PostMapping("/operations-externes")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String createOperationExterne(@Valid @ModelAttribute OperationEnCoursExterne operationExterne, BindingResult br) {
	    if (br.hasErrors()) return "pontbascule/operations-externes/form";
	    operationEncoursExterneService.save(operationExterne);
	    return "redirect:/pontbascule/operations-externes";
	}

	@GetMapping("/operations-externes/{id}/modifier")
	@PreAuthorize("hasAuthority('PONTBASCULE_MODIFIER')")
	public String editOperationExterne(@PathVariable Integer id, Model model) {
	    OperationEnCoursExterne operationExterne = operationEncoursExterneService.findById(id).orElseThrow();
	    model.addAttribute("operationExterne", operationExterne);
	    model.addAttribute("vehiculeExterne", vehiculeExterneService.findAll());
	    return "pontbascule/operations-externes/form";
	    //return "pontbascule/operations-externes/formExterneFragment :: formExterne";
	}

	@PostMapping("/operations-externes/{id}")
	@PreAuthorize("hasAuthority('PONTBASCULE_MODIFIER')")
	public String updateOperationExterne(@PathVariable Integer id, @Valid @ModelAttribute OperationEnCoursExterne operationExterne, BindingResult br) {
	    if (br.hasErrors()) return "pontbascule/operations-externes/form";
	    operationExterne.setId(id);
	    operationEncoursExterneService.save(operationExterne);
	    System.out.println("ope save-------------------" + operationExterne.getId());
	    return "redirect:/pontbascule/operations-externes";
	}

	@PostMapping("/operations-externes/{id}/supprimer")
	@PreAuthorize("hasAuthority('PONTBASCULE_SUPPRIMER')")
	public String deleteOperationExterne(@PathVariable Integer id) {
	    operationEncoursExterneService.deleteById(id);
	    return "redirect:/pontbascule/operations-internes";
	}
	

	// OPERATIONS ENCOURS INTERNE CRUD
	@GetMapping("/operations-internes")
	@PreAuthorize("hasAuthority('PONTBASCULE_LIRE')")
	public String listOperationsInternes(Model model) {
	    model.addAttribute("operationsInternes", operationEncoursInterneService.findAll());
	    model.addAttribute("operationsExternes", operationEncoursExterneService.findAll());
	    return "pontbascule/operations-internes/index";
	}

	@GetMapping("/operations-internes/nouveau")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String newOperationInterne(Model model) {
	    model.addAttribute("operationInterne", new OperationEnCoursInterne());
	    model.addAttribute("vehiculesTarer", vehiculeTarerService.findAll());
	    System.out.println("ope-attribute------------------" + model.getAttribute("operationInterne"));
	    return "pontbascule/operations-internes/form";
	    //return "pontbascule/operations-internes/formInterneFragment :: formInterne";
	}

	@PostMapping("/operations-internes")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String createOperationInterne(@Valid @ModelAttribute OperationEnCoursInterne operationInterne, BindingResult br) {
	    if (br.hasErrors()) return "pontbascule/operations-internes/form";
	    operationEncoursInterneService.save(operationInterne);
	    System.out.println("ope-creer------------------" + operationInterne.getId());
	    return "redirect:/pontbascule/operations-internes";
	}

	@GetMapping("/operations-internes/{id}/modifier")
	@PreAuthorize("hasAuthority('PONTBASCULE_MODIFIER')")
	public String editOperationInterne(@PathVariable Integer id, Model model) {
	    OperationEnCoursInterne operationInterne = operationEncoursInterneService.findById(id).orElseThrow();
	    System.out.println("ope-modifier------------------" + operationInterne.getId());
	    model.addAttribute("operationInterne", operationInterne);
	    model.addAttribute("vehiculesTarer", vehiculeTarerService.findAll());
	    return "pontbascule/operations-internes/form";
	 //   return "pontbascule/operations-internes/formInterneFragment :: formInterne";
	}

	@PostMapping("/operations-internes/{id}")
	@PreAuthorize("hasAuthority('PONTBASCULE_MODIFIER')")
	public String updateOperationInterne(@PathVariable Integer id, @Valid @ModelAttribute OperationEnCoursInterne operationInterne, BindingResult br) {
	    if (br.hasErrors()) return "pontbascule/operations-internes/form";
	    operationInterne.setId(id);
	    operationEncoursInterneService.save(operationInterne);
	    System.out.println("ope save-------------------" + operationInterne.getId());
	    return "redirect:/pontbascule/operations-internes";
	}

	@PostMapping("/operations-internes/{id}/supprimer")
	@PreAuthorize("hasAuthority('PONTBASCULE_SUPPRIMER')")
	public String deleteOperationInterne(@PathVariable int id) {
	    operationEncoursInterneService.deleteById(id);
	    return "redirect:/pontbascule/operations-internes";
	}


	//======================================================================
	// VEHICULES TARER - CRUD
	//======================================================================

	@GetMapping("/vehicules-tarer")
	@PreAuthorize("hasAuthority('PONTBASCULE_LIRE')")
	public String listVehiculesTarer(Model model) {
	    model.addAttribute("vehicules", vehiculeTarerService.findAll());
	    return "pontbascule/vehicules-tarer/index"; // Page listant les véhicules
	}

	@GetMapping("/vehicules-tarer/nouveau")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String newVehiculeTarer(Model model) {
	    model.addAttribute("vehiculeTarer", new VehiculeTarer());
	    return "pontbascule/vehicules-tarer/form"; // Formulaire de création
	}

	@PostMapping("/vehicules-tarer")
	@PreAuthorize("hasAuthority('PONTBASCULE_CREER')")
	public String createVehiculeTarer(@Valid @ModelAttribute VehiculeTarer vehiculeTarer, BindingResult br) {
	    if (br.hasErrors()) {
	        return "pontbascule/vehicules-tarer/form";
	    }
	    vehiculeTarerService.save(vehiculeTarer);
	    return "redirect:/pontbascule/operations-internes/nouveau"; // Redirige vers le formulaire d'opération pour rafraîchir la liste
	}

	@GetMapping("/vehicules-tarer/{id}/modifier")
	@PreAuthorize("hasAuthority('PONTBASCULE_MODIFIER')")
	public String editVehiculeTarer(@PathVariable String id, Model model) {
	    VehiculeTarer vehicule = vehiculeTarerService.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable pour l'id: " + id));
	    model.addAttribute("vehiculeTarer", vehicule);
	    return "pontbascule/vehicules-tarer/form"; // Formulaire de modification
	}

	@PostMapping("/vehicules-tarer/{id}/modifier")
	@PreAuthorize("hasAuthority('PONTBASCULE_MODIFIER')")
	public String updateVehiculeTarer(@PathVariable String id, @Valid @ModelAttribute VehiculeTarer vehiculeTarer, BindingResult br) {
	    if (br.hasErrors()) {
	        return "pontbascule/vehicules-tarer/form";
	    }
	    vehiculeTarer.setImmatriculation(id); // Assure que l'ID n'est pas modifié
	    vehiculeTarerService.save(vehiculeTarer);
	    return "redirect:/pontbascule/vehicules-tarer"; // Redirige vers la liste des véhicules
	}
/**
 * en cas ou ,mais normallement pour les véhicule il n as pas lieu de supression violation de contrainte
 * avec opération en cours interne ou externe
	@PostMapping("/vehicules-tarer/{id}/supprimer")
	@PreAuthorize("hasAuthority('PONTBASCULE_SUPPRIMER')")
	public String deleteVehiculeTarer(@PathVariable String id) {
	    vehiculeTarerService.deleteById(id);
	    return "redirect:/pontbascule/vehicules-tarer"; // Redirige vers la liste des véhicules
	}
	
	**/
}


