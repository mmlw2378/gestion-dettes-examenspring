package sn.ism.gestion_dettes.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.ism.gestion_dettes.dto.DetteDto;
import sn.ism.gestion_dettes.entities.Client;
import sn.ism.gestion_dettes.entities.Dette;
import sn.ism.gestion_dettes.repositories.DetteRepository;

@Service
@Transactional
public class DetteService {
    
    @Autowired
    private DetteRepository detteRepository;
    
    @Autowired
    private ClientService clientService;
    
    /**
     * Ajouter une nouvelle dette à un client
     */
    public DetteDto ajouterDette(DetteDto detteDto) {
        // Vérifier que le client existe
        Client client = clientService.obtenirClientEntity(detteDto.getClientId());
        
        Dette dette = new Dette();
        dette.setDate(detteDto.getDate());
        dette.setMontantDette(detteDto.getMontantDette());
        dette.setClient(client);
        
        Dette savedDette = detteRepository.save(dette);
        return convertToDto(savedDette);
    }
    
    /**
     * Ajouter plusieurs dettes à un client
     */
    public List<DetteDto> ajouterPluseursDettes(List<DetteDto> dettesDto) {
        return dettesDto.stream()
                .map(this::ajouterDette)
                .collect(Collectors.toList());
    }
    
    /**
     * Lister les dettes d'un client avec pagination
     */
    @Transactional(readOnly = true)
    public Page<DetteDto> listerDettesClient(Long clientId, Pageable pageable) {
        Client client = clientService.obtenirClientEntity(clientId);
        Page<Dette> dettes = detteRepository.findByClient(client, pageable);
        return dettes.map(this::convertToDto);
    }
    
    /**
     * Lister les dettes d'un client avec filtre sur le téléphone
     */
    @Transactional(readOnly = true)
    public Page<DetteDto> listerDettesAvecFiltreTelephone(String telephone, Pageable pageable) {
        Page<Dette> dettes = detteRepository.findByClientTelephoneContaining(telephone, pageable);
        return dettes.map(this::convertToDto);
    }
    
    /**
     * Rechercher des dettes avec filtres multiples
     */
    @Transactional(readOnly = true)
    public Page<DetteDto> rechercherDettesAvecFiltres(Long clientId, String telephone, 
                                                     BigDecimal montantMin, BigDecimal montantMax, 
                                                     Pageable pageable) {
        Page<Dette> dettes = detteRepository.findDettesWithFilters(
                clientId, telephone, montantMin, montantMax, pageable);
        return dettes.map(this::convertToDto);
    }
    
    /**
     * Obtenir toutes les dettes avec pagination
     */
    @Transactional(readOnly = true)
    public Page<DetteDto> obtenirToutesLesDettes(Pageable pageable) {
        Page<Dette> dettes = detteRepository.findAll(pageable);
        return dettes.map(this::convertToDto);
    }
    
    /**
     * Obtenir une dette par ID
     */
    @Transactional(readOnly = true)
    public Optional<DetteDto> obtenirDetteParId(Long id) {
        Optional<Dette> dette = detteRepository.findById(id);
        return dette.map(this::convertToDto);
    }
    
    /**
     * Obtenir les dettes non payées
     */
    @Transactional(readOnly = true)
    public Page<DetteDto> obtenirDettesNonPayees(Pageable pageable) {
        Page<Dette> dettes = detteRepository.findDettesNonPayees(pageable);
        return dettes.map(this::convertToDto);
    }
    
    /**
     * Obtenir les dettes payées
     */
    @Transactional(readOnly = true)
    public Page<DetteDto> obtenirDettesPayees(Pageable pageable) {
        Page<Dette> dettes = detteRepository.findDettesPayees(pageable);
        return dettes.map(this::convertToDto);
    }
    
    /**
     * Mettre à jour une dette
     */
    public DetteDto mettreAJourDette(Long id, DetteDto detteDto) {
        Dette dette = detteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dette non trouvée avec ID: " + id));
        
        dette.setDate(detteDto.getDate());
        dette.setMontantDette(detteDto.getMontantDette());
        
        // Recalculer automatiquement le montant restant
        dette.calculerMontantPaye();
        
        Dette updatedDette = detteRepository.save(dette);
        return convertToDto(updatedDette);
    }
    
    /**
     * Supprimer une dette
     */
    public void supprimerDette(Long id) {
        Dette dette = detteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dette non trouvée avec ID: " + id));
        
        // Vérifier si la dette a des paiements
        if (!dette.getPaiements().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer la dette car elle a des paiements associés");
        }
        
        detteRepository.delete(dette);
    }
    
    /**
     * Obtenir le montant total des dettes d'un client
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenirMontantTotalDetteClient(Long clientId) {
        Client client = clientService.obtenirClientEntity(clientId);
        BigDecimal total = detteRepository.getTotalDetteByClient(client);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Obtenir le montant restant à payer d'un client
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenirMontantRestantClient(Long clientId) {
        Client client = clientService.obtenirClientEntity(clientId);
        BigDecimal montantRestant = detteRepository.getMontantRestantByClient(client);
        return montantRestant != null ? montantRestant : BigDecimal.ZERO;
    }
    
    /**
     * Vérifier si une dette existe
     */
    @Transactional(readOnly = true)
    public boolean detteExiste(Long id) {
        return detteRepository.existsById(id);
    }
    
    /**
     * Obtenir l'entité Dette (pour usage interne)
     */
    @Transactional(readOnly = true)
    public Dette obtenirDetteEntity(Long id) {
        return detteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dette non trouvée avec ID: " + id));
    }
    
    /**
     * Convertir Dette vers DetteDto
     */
    private DetteDto convertToDto(Dette dette) {
        DetteDto dto = new DetteDto();
        dto.setId(dette.getId());
        dto.setDate(dette.getDate());
        dto.setMontantDette(dette.getMontantDette());
        dto.setMontantPaye(dette.getMontantPaye());
        dto.setMontantRestant(dette.getMontantRestant());
        dto.setClientId(dette.getClient().getId());
        dto.setClientNom(dette.getClient().getNom());
        dto.setClientTelephone(dette.getClient().getTelephone());
        return dto;
    }
    
    /**
     * Obtenir les statistiques des dettes d'un client
     */
    @Transactional(readOnly = true)
    public DetteStatistiquesDto obtenirStatistiquesClient(Long clientId) {
        Client client = clientService.obtenirClientEntity(clientId);
        
        BigDecimal totalDettes = detteRepository.getTotalDetteByClient(client);
        BigDecimal montantRestant = detteRepository.getMontantRestantByClient(client);
        BigDecimal montantPaye = totalDettes != null && montantRestant != null ? 
                totalDettes.subtract(montantRestant) : BigDecimal.ZERO;
        
        long nombreDettes = client.getDettes().size();
        long dettesPayees = client.getDettes().stream()
                .mapToLong(dette -> dette.isCompletelyPaid() ? 1 : 0)
                .sum();
        
        return new DetteStatistiquesDto(
                totalDettes != null ? totalDettes : BigDecimal.ZERO,
                montantPaye,
                montantRestant != null ? montantRestant : BigDecimal.ZERO,
                nombreDettes,
                dettesPayees
        );
    }
    
    // Classe interne pour les statistiques
    public static class DetteStatistiquesDto {
        private BigDecimal montantTotalDettes;
        private BigDecimal montantTotalPaye;
        private BigDecimal montantTotalRestant;
        private long nombreTotalDettes;
        private long nombreDettesPayees;
        
        public DetteStatistiquesDto(BigDecimal montantTotalDettes, BigDecimal montantTotalPaye, 
                                   BigDecimal montantTotalRestant, long nombreTotalDettes, 
                                   long nombreDettesPayees) {
            this.montantTotalDettes = montantTotalDettes;
            this.montantTotalPaye = montantTotalPaye;
            this.montantTotalRestant = montantTotalRestant;
            this.nombreTotalDettes = nombreTotalDettes;
            this.nombreDettesPayees = nombreDettesPayees;
        }
        
        // Getters
        public BigDecimal getMontantTotalDettes() { return montantTotalDettes; }
        public BigDecimal getMontantTotalPaye() { return montantTotalPaye; }
        public BigDecimal getMontantTotalRestant() { return montantTotalRestant; }
        public long getNombreTotalDettes() { return nombreTotalDettes; }
        public long getNombreDettesPayees() { return nombreDettesPayees; }
        public long getNombreDettesNonPayees() { return nombreTotalDettes - nombreDettesPayees; }
    }
}