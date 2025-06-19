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
import sn.ism.gestion_dettes.dto.DetteDto;
import sn.ism.gestion_dettes.services.DetteService;

@RestController
@RequestMapping("/dettes")
@CrossOrigin(origins = "*")
public class DetteController {
    
    @Autowired
    private DetteService detteService;
    
    /**
     *  Ajouter une nouvelle dette
     */
    @PostMapping
    public ResponseEntity<?> ajouterDette(@Valid @RequestBody DetteDto detteDto) {
        try {
            DetteDto nouvelleDette = detteService.ajouterDette(detteDto);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "message", "Dette ajoutée avec succès",
                    "data", nouvelleDette
            ), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Ajouter plusieurs dettes à un client
     */
    @PostMapping("/batch")
    public ResponseEntity<?> ajouterPluseursDettes(@Valid @RequestBody List<DetteDto> dettesDto) {
        try {
            List<DetteDto> nouvellesDettes = detteService.ajouterPluseursDettes(dettesDto);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "message", nouvellesDettes.size() + " dettes ajoutées avec succès",
                    "data", nouvellesDettes
            ), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Obtenir toutes les dettes avec pagination et filtres
     */
    @GetMapping
    public ResponseEntity<?> obtenirToutesLesDettes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) BigDecimal montantMin,
            @RequestParam(required = false) BigDecimal montantMax) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<DetteDto> dettes;
            
            // Appliquer les filtres si fournis
            if (clientId != null || telephone != null || montantMin != null || montantMax != null) {
                dettes = detteService.rechercherDettesAvecFiltres(clientId, telephone, 
                        montantMin, montantMax, pageable);
            } else {
                dettes = detteService.obtenirToutesLesDettes(pageable);
            }
            
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", dettes.getContent(),
                    "pagination", Map.of(
                            "currentPage", dettes.getNumber(),
                            "totalPages", dettes.getTotalPages(),
                            "totalElements", dettes.getTotalElements(),
                            "size", dettes.getSize(),
                            "hasNext", dettes.hasNext(),
                            "hasPrevious", dettes.hasPrevious()
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
     *  Lister les dettes d'un client avec pagination
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> listerDettesClient(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<DetteDto> dettes = detteService.listerDettesClient(clientId, pageable);
            
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", dettes.getContent(),
                    "pagination", Map.of(
                            "currentPage", dettes.getNumber(),
                            "totalPages", dettes.getTotalPages(),
                            "totalElements", dettes.getTotalElements(),
                            "size", dettes.getSize(),
                            "hasNext", dettes.hasNext(),
                            "hasPrevious", dettes.hasPrevious()
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
     *  Rechercher des dettes par téléphone du client
     */
    @GetMapping("/search-by-phone")
    public ResponseEntity<?> rechercherDettesParTelephone(
            @RequestParam String telephone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<DetteDto> dettes = detteService.listerDettesAvecFiltreTelephone(telephone, pageable);
            
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", dettes.getContent(),
                    "pagination", Map.of(
                            "currentPage", dettes.getNumber(),
                            "totalPages", dettes.getTotalPages(),
                            "totalElements", dettes.getTotalElements(),
                            "size", dettes.getSize(),
                            "hasNext", dettes.hasNext(),
                            "hasPrevious", dettes.hasPrevious()
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
     *  Obtenir une dette par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirDetteParId(@PathVariable Long id) {
        try {
            Optional<DetteDto> dette = detteService.obtenirDetteParId(id);
            if (dette.isPresent()) {
                return new ResponseEntity<>(Map.of(
                        "success", true,
                        "data", dette.get()
                ), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Map.of(
                        "success", false,
                        "message", "Dette non trouvée"
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
     *  Obtenir les dettes non payées
     */
    @GetMapping("/non-payees")
    public ResponseEntity<?> obtenirDettesNonPayees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<DetteDto> dettes = detteService.obtenirDettesNonPayees(pageable);
            
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", dettes.getContent(),
                    "pagination", Map.of(
                            "currentPage", dettes.getNumber(),
                            "totalPages", dettes.getTotalPages(),
                            "totalElements", dettes.getTotalElements(),
                            "size", dettes.getSize(),
                            "hasNext", dettes.hasNext(),
                            "hasPrevious", dettes.hasPrevious()
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
     *  Obtenir les dettes payées
     */
    @GetMapping("/payees")
    public ResponseEntity<?> obtenirDettesPayees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<DetteDto> dettes = detteService.obtenirDettesPayees(pageable);
            
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", dettes.getContent(),
                    "pagination", Map.of(
                            "currentPage", dettes.getNumber(),
                            "totalPages", dettes.getTotalPages(),
                            "totalElements", dettes.getTotalElements(),
                            "size", dettes.getSize(),
                            "hasNext", dettes.hasNext(),
                            "hasPrevious", dettes.hasPrevious()
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
     * Mettre à jour une dette
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> mettreAJourDette(@PathVariable Long id, 
                                             @Valid @RequestBody DetteDto detteDto) {
        try {
            DetteDto detteMiseAJour = detteService.mettreAJourDette(id, detteDto);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "message", "Dette mise à jour avec succès",
                    "data", detteMiseAJour
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Supprimer une dette
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerDette(@PathVariable Long id) {
        try {
            detteService.supprimerDette(id);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "message", "Dette supprimée avec succès"
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Obtenir le montant total des dettes d'un client
     */
    @GetMapping("/client/{clientId}/total")
    public ResponseEntity<?> obtenirMontantTotalDetteClient(@PathVariable Long clientId) {
        try {
            BigDecimal montantTotal = detteService.obtenirMontantTotalDetteClient(clientId);
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
     *  Obtenir le montant restant à payer d'un client
     */
    @GetMapping("/client/{clientId}/restant")
    public ResponseEntity<?> obtenirMontantRestantClient(@PathVariable Long clientId) {
        try {
            BigDecimal montantRestant = detteService.obtenirMontantRestantClient(clientId);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "montantRestant", montantRestant
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Obtenir les statistiques des dettes d'un client
     */
    @GetMapping("/client/{clientId}/statistiques")
    public ResponseEntity<?> obtenirStatistiquesClient(@PathVariable Long clientId) {
        try {
            DetteService.DetteStatistiquesDto statistiques = detteService.obtenirStatistiquesClient(clientId);
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
     *  Vérifier si une dette existe
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<?> verifierExistenceDette(@PathVariable Long id) {
        try {
            boolean existe = detteService.detteExiste(id);
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