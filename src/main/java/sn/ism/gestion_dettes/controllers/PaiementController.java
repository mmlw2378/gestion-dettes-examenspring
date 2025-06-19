package sn.ism.gestion_dettes.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import sn.ism.gestion_dettes.dto.PaiementDto;
import sn.ism.gestion_dettes.services.PaiementService;

@RestController
@RequestMapping("/paiements")
@CrossOrigin(origins = "*")
public class PaiementController {
    
    @Autowired
    private PaiementService paiementService;
    
    /**
     *  Ajouter un paiement à une dette
     */
    @PostMapping
    public ResponseEntity<?> ajouterPaiement(@Valid @RequestBody PaiementDto paiementDto) {
        try {
            PaiementDto nouveauPaiement = paiementService.ajouterPaiement(paiementDto);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "message", "Paiement ajouté avec succès",
                    "data", nouveauPaiement
            ), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Payer une dette complètement
     */
    @PostMapping("/payer-completement/{detteId}")
    public ResponseEntity<?> payerDetteCompletement(
            @PathVariable Long detteId,
            @RequestBody Map<String, String> requestBody) {
        try {
            String datePaiement = requestBody.get("datePaiement");
            if (datePaiement == null || datePaiement.trim().isEmpty()) {
                return new ResponseEntity<>(Map.of(
                        "success", false,
                        "message", "La date de paiement est obligatoire"
                ), HttpStatus.BAD_REQUEST);
            }
            
            PaiementDto paiement = paiementService.payerDetteCompletement(detteId, datePaiement);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "message", "Dette payée complètement avec succès",
                    "data", paiement
            ), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Obtenir tous les paiements avec pagination et filtres
     */
    @GetMapping
    public ResponseEntity<?> obtenirTousLesPaiements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateCreation") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) Long detteId,
            @RequestParam(required = false) BigDecimal montantMin,
            @RequestParam(required = false) BigDecimal montantMax) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<PaiementDto> paiements;
            
            // Appliquer les filtres si fournis
            if (telephone != null || detteId != null || montantMin != null || montantMax != null) {
                paiements = paiementService.rechercherPaiementsAvecFiltres(
                        telephone, detteId, montantMin, montantMax, pageable);
            } else {
                paiements = paiementService.obtenirTousLesPaiements(pageable);
            }
            
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", paiements.getContent(),
                    "pagination", Map.of(
                            "currentPage", paiements.getNumber(),
                            "totalPages", paiements.getTotalPages(),
                            "totalElements", paiements.getTotalElements(),
                            "size", paiements.getSize(),
                            "hasNext", paiements.hasNext(),
                            "hasPrevious", paiements.hasPrevious()
                    )
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     *  Obtenir les paiements d'une dette avec pagination
     */
    @GetMapping("/dette/{detteId}")
    public ResponseEntity<?> obtenirPaiementsDette(
            @PathVariable Long detteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateCreation") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<PaiementDto> paiements = paiementService.obtenirPaiementsDette(detteId, pageable);
            
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", paiements.getContent(),
                    "pagination", Map.of(
                            "currentPage", paiements.getNumber(),
                            "totalPages", paiements.getTotalPages(),
                            "totalElements", paiements.getTotalElements(),
                            "size", paiements.getSize(),
                            "hasNext", paiements.hasNext(),
                            "hasPrevious", paiements.hasPrevious()
                    )
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Obtenir les paiements d'une dette (sans pagination)
     */
    @GetMapping("/dette/{detteId}/simple")
    public ResponseEntity<?> obtenirPaiementsDetteSimple(@PathVariable Long detteId) {
        try {
            List<PaiementDto> paiements = paiementService.obtenirPaiementsDetteOrdonnes(detteId);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", paiements
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Rechercher des paiements par téléphone du client
     */
    @GetMapping("/search-by-phone")
    public ResponseEntity<?> rechercherPaiementsParTelephone(
            @RequestParam String telephone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateCreation") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<PaiementDto> paiements = paiementService.rechercherPaiementsParTelephone(telephone, pageable);
            
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", paiements.getContent(),
                    "pagination", Map.of(
                            "currentPage", paiements.getNumber(),
                            "totalPages", paiements.getTotalPages(),
                            "totalElements", paiements.getTotalElements(),
                            "size", paiements.getSize(),
                            "hasNext", paiements.hasNext(),
                            "hasPrevious", paiements.hasPrevious()
                    )
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Obtenir un paiement par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirPaiementParId(@PathVariable Long id) {
        try {
            Optional<PaiementDto> paiement = paiementService.obtenirPaiementParId(id);
            if (paiement.isPresent()) {
                return new ResponseEntity<>(Map.of(
                        "success", true,
                        "data", paiement.get()
                ), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Map.of(
                        "success", false,
                        "message", "Paiement non trouvé"
                ), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     *  Mettre à jour un paiement
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> mettreAJourPaiement(@PathVariable Long id, 
                                               @Valid @RequestBody PaiementDto paiementDto) {
        try {
            PaiementDto paiementMisAJour = paiementService.mettreAJourPaiement(id, paiementDto);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "message", "Paiement mis à jour avec succès",
                    "data", paiementMisAJour
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Supprimer un paiement
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerPaiement(@PathVariable Long id) {
        try {
            paiementService.supprimerPaiement(id);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "message", "Paiement supprimé avec succès"
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Obtenir le montant total des paiements d'une dette
     */
    @GetMapping("/dette/{detteId}/total")
    public ResponseEntity<?> obtenirMontantTotalPaiementsDette(@PathVariable Long detteId) {
        try {
            BigDecimal montantTotal = paiementService.obtenirMontantTotalPaiementsDette(detteId);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "montantTotal", montantTotal
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Obtenir les statistiques des paiements d'une dette
     */
    @GetMapping("/dette/{detteId}/statistiques")
    public ResponseEntity<?> obtenirStatistiquesPaiementsDette(@PathVariable Long detteId) {
        try {
            PaiementService.PaiementStatistiquesDto statistiques = 
                    paiementService.obtenirStatistiquesPaiementsDette(detteId);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", statistiques
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Vérifier si un paiement existe
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<?> verifierExistencePaiement(@PathVariable Long id) {
        try {
            boolean existe = paiementService.paiementExiste(id);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "exists", existe
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}