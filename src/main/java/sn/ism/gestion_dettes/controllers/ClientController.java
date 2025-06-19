package sn.ism.gestion_dettes.controllers;

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
import sn.ism.gestion_dettes.dto.ClientDto;
import sn.ism.gestion_dettes.services.ClientService;

@RestController
@RequestMapping("/clients")
@CrossOrigin(origins = "*")
public class ClientController {
    
    @Autowired
    private ClientService clientService;
    
    /**
     * Ajouter un nouveau client
     */
    @PostMapping
    public ResponseEntity<?> ajouterClient(@Valid @RequestBody ClientDto clientDto) {
        try {
            ClientDto nouveauClient = clientService.ajouterClient(clientDto);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "message", "Client ajouté avec succès",
                    "data", nouveauClient
            ), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Obtenir tous les clients avec pagination
     */
    @GetMapping
    public ResponseEntity<?> obtenirTousLesClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String telephone) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ClientDto> clients;
            
            // Appliquer les filtres si fournis
            if (nom != null || telephone != null) {
                clients = clientService.rechercherClientsAvecFiltres(nom, telephone, pageable);
            } else {
                clients = clientService.obtenirTousLesClients(pageable);
            }
            
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", clients.getContent(),
                    "pagination", Map.of(
                            "currentPage", clients.getNumber(),
                            "totalPages", clients.getTotalPages(),
                            "totalElements", clients.getTotalElements(),
                            "size", clients.getSize(),
                            "hasNext", clients.hasNext(),
                            "hasPrevious", clients.hasPrevious()
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
     *  Obtenir tous les clients sans pagination (pour les listes déroulantes)
     */
    @GetMapping("/simple")
    public ResponseEntity<?> obtenirTousLesClientsSimplement() {
        try {
            List<ClientDto> clients = clientService.obtenirTousLesClientsSimplement();
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", clients
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     *  Obtenir un client par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenirClientParId(@PathVariable Long id) {
        try {
            Optional<ClientDto> client = clientService.obtenirClientParId(id);
            if (client.isPresent()) {
                return new ResponseEntity<>(Map.of(
                        "success", true,
                        "data", client.get()
                ), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Map.of(
                        "success", false,
                        "message", "Client non trouvé"
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
     * Obtenir un client par téléphone
     */
    @GetMapping("/telephone/{telephone}")
    public ResponseEntity<?> obtenirClientParTelephone(@PathVariable String telephone) {
        try {
            Optional<ClientDto> client = clientService.obtenirClientParTelephone(telephone);
            if (client.isPresent()) {
                return new ResponseEntity<>(Map.of(
                        "success", true,
                        "data", client.get()
                ), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Map.of(
                        "success", false,
                        "message", "Client non trouvé avec ce numéro de téléphone"
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
     *  Rechercher des clients par téléphone
     */
    @GetMapping("/search")
    public ResponseEntity<?> rechercherClientsParTelephone(
            @RequestParam String telephone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ClientDto> clients = clientService.rechercherClientsParTelephone(telephone, pageable);
            
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "data", clients.getContent(),
                    "pagination", Map.of(
                            "currentPage", clients.getNumber(),
                            "totalPages", clients.getTotalPages(),
                            "totalElements", clients.getTotalElements(),
                            "size", clients.getSize(),
                            "hasNext", clients.hasNext(),
                            "hasPrevious", clients.hasPrevious()
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
     * Mettre à jour un client
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> mettreAJourClient(@PathVariable Long id, 
                                              @Valid @RequestBody ClientDto clientDto) {
        try {
            ClientDto clientMisAJour = clientService.mettreAJourClient(id, clientDto);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "message", "Client mis à jour avec succès",
                    "data", clientMisAJour
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     *  Supprimer un client
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerClient(@PathVariable Long id) {
        try {
            clientService.supprimerClient(id);
            return new ResponseEntity<>(Map.of(
                    "success", true,
                    "message", "Client supprimé avec succès"
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Vérifier si un client existe
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<?> verifierExistenceClient(@PathVariable Long id) {
        try {
            boolean existe = clientService.clientExiste(id);
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