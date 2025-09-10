package com.sh.erpcos.module.pontbascule.service;

import com.sh.erpcos.module.pontbascule.entity.Operation;
import com.sh.erpcos.module.pontbascule.entity.OperationEnCoursExterne;
import com.sh.erpcos.module.pontbascule.entity.OperationEnCoursInterne;
import com.sh.erpcos.module.pontbascule.entity.Partenaire;
import com.sh.erpcos.module.pontbascule.entity.Produit;
import com.sh.erpcos.module.pontbascule.entity.TypeOperation;
import com.sh.erpcos.module.pontbascule.repository.OperationRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OperationService {

	private final OperationRepository repository;
	
	private final OperationEncoursInterneService operationEnCoursInterneService;
	private final OperationEncoursExterneService operationEncoursExterneService;
	private final ProduitService produitService;
	private final PartenaireService partenaireService;
	private final TypeOperationService typeOperationService;
	
	@Transactional(readOnly = true)
	public List<Operation> findAll() { return repository.findAll(); }

	@Transactional(readOnly = true)
	public Optional<Operation> findById(Long id) { return repository.findById(id); }

	public Operation save(Operation op) { return repository.save(op); }

	public void deleteById(Long id) { repository.deleteById(id); }
	
	/**
     * Retourne une instance d'Operation pré-remplie à partir d'une
     * opération interne existante. Si l'ID est null, on retourne simplement
     * un nouvel objet vide.
     */
	///private Integer interId;
    public Operation initFromInterne(Integer interId) {
        /**
    	if (interId == null) {
        	
            return new Operation();
        }

    	OperationEnCoursInterne inter =
        operationEnCoursInterneService.findById(interId)
                .orElse(((null)));
        
        System.out.println("object interne -------------" + inter);
        	///System.out.println("object intern -------------" + inter.getClass().getSimpleName());
        OperationEnCoursExterne exter =
                operationEncoursExterneService.findById(interId)
                        .orElse(((null)));
               
        System.out.println("object ixter -------------" + exter);
        Operation op = new Operation();
        
        if (inter!= null ){
            op.setImmatriculation(inter.getVehiculeTarer().getImmatriculation());
            op.setPeser1(inter.getVehiculeTarer().getPeser1());
            op.setOperationEnCoursId(interId);
            
            System.out.println("object interne -------------" + inter);
        }else  {
        	op.setImmatriculation(exter.getVehiculeExterne().getImmatriculation());
            op.setPeser2(exter.getPeser1());
            op.setOperationEnCoursId(interId);
            System.out.println("object exter -------------" + exter);
        	
        }
            return op;
            
        **/    
            
          
                // Récupération sous forme d'Optional
                Optional<OperationEnCoursInterne> optInter = operationEnCoursInterneService.findById(interId);
                Optional<OperationEnCoursExterne> optExter = operationEncoursExterneService.findById(interId);

                Operation op = new Operation();
                op. setOperationEnCoursId(interId);

                // Utilisation d'ifPresentOrElse pour éviter le if/else classique
                optInter.ifPresentOrElse(
                    inter -> {
                        op.setImmatriculation(inter.getVehiculeTarer().getImmatriculation());
                        op.setPeser1(inter.getVehiculeTarer().getPeser1());
                        System.out.println("Object interne ------------- " + inter);
                    },
                    () -> optExter.ifPresent(exter -> {
                        op.setImmatriculation(exter.getVehiculeExterne().getImmatriculation());
                        op.setPeser2(exter.getPeser1());
                        System.out.println("Object exter ------------- " + exter);
                    })
                );

                return op;
            }
            
            
        
        
    
   
    public void saveOperation(Operation operation,Long produitId,Long partenaireId,
    		Integer typeOperationId,Authentication auth) {

        /* 1️⃣  Récupérer les entités liées */
        Produit       prod   = produitService.findById(produitId)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable"));
        Partenaire    part   = partenaireService.findById(partenaireId)
                .orElseThrow(() -> new IllegalArgumentException("Partenaire introuvable"));
        TypeOperation typeOp = typeOperationService.findById(typeOperationId)
                .orElseThrow(() -> new IllegalArgumentException("Type d’opération introuvable"));

        /* 2️⃣  Lier les entités */
        operation.setProduit(prod);
        operation.setPartenaire(part);
        operation.setTypeOperation(typeOp);

        /* 3️⃣  Dates système */
        operation.setDateOperation(LocalDate.now());
        operation.setHeureOperation(LocalTime.now());

        /* 4️⃣  Calculs métier – *ne touchons pas à peser1* */
        if (operation.getPeser1() != null && operation.getPeser2() != null) {
            operation.setPoidsNet(operation.getPeser2() - operation.getPeser1());
        }

        if (operation.getPoidsNet() != null && operation.getQteDeclare() != null) {
            operation.setEcart(operation.getPoidsNet() - operation.getQteDeclare());
        }

        /* 5️⃣  Audit */
        operation.setCreeParId(auth != null ? auth.getName() : null);

        /* 6️⃣  Persist */
        repository.save(operation);
    ///    System.out.println("interid =---------------" + interId);
        System.out.println("oppp =---------------saved-----------------");
        
        // a revoire dans le cas de supression concurente entre interne et externe 
      //  operationEnCoursInterneService.deleteById(operation.getOperationEnCoursId());
      //  operationEncoursExterneService.deleteById(operation.getOperationEnCoursId());
        System.out.println("interid =---------------" + operation.getOperationEnCoursId());
        
        Optional<OperationEnCoursInterne> optInterne = operationEnCoursInterneService.findById(operation.getOperationEnCoursId());
        Optional<OperationEnCoursExterne> optExterne = operationEncoursExterneService.findById(operation.getOperationEnCoursId());

        if (optInterne.isPresent() && !optExterne.isPresent()) {
            operationEnCoursInterneService.deleteById(operation.getOperationEnCoursId());
        } else if (optExterne.isPresent() && !optInterne.isPresent()) {
            operationEncoursExterneService.deleteById(operation.getOperationEnCoursId());
        } else if (optInterne.isPresent() && optExterne.isPresent()) {
            // Cas inattendu : les deux existent pour le même ID, prendre une décision,
            // par exemple, supprimer l'un ou lever une exception ou loguer un avertissement.
            // Ici, on peut choisir de supprimer les deux (ou seulement l'un, selon la logique métier)
            //operationEnCoursInterneService.deleteById(operation.getOperationEnCoursId());
           // operationEncoursExterneService.deleteById(operation.getOperationEnCoursId());
        } else {
            // Aucune entité trouvée, éventuellement loguer une erreur ou effectuer un traitement par défaut.
            System.out.println("Aucune donnée à supprimer pour l'id " + operation.getOperationEnCoursId());
        }
        
        
        System.out.println("externeid =---------------" + operation.getOperationEnCoursId());
 ///       interId = null;
        System.out.println("oppp =---------------" + operation.getOperationEnCoursId());
        System.out.println("oppp =---------------deleted" );
        
    }
    
}


