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

import sn.ism.gestion_dettes.dto.PaiementDto;
import sn.ism.gestion_dettes.entities.Dette;
import sn.ism.gestion_dettes.entities.Paiement;
import sn.ism.gestion_dettes.repositories.PaiementRepository;

@Service
@Transactional
public class PaiementService {
    
    @Autowired
    private PaiementRepository paiementRepository;
    
    @Autowired
    private DetteService detteService;
    
    /**
     * Ajouter un paiement à une dette
     */
    public PaiementDto ajouterPaiement(PaiementDto paiementDto) {
        // Vérifier que la dette existe
        Dette dette = detteService.obtenirDetteEntity(paiementDto.getDetteId());
        
        // Vérifier que le montant du paiement ne dépasse pas le montant restant
        if (paiementDto.getMontant().compareTo(dette.getMontantRestant()) > 0) {
            throw new RuntimeException("Le montant du paiement (" + paiementDto.getMontant() + 
                    ") dépasse le montant restant de la dette (" + dette.getMontantRestant() + ")");
        }
        
        Paiement paiement = new Paiement();
        paiement.setMontant(paiementDto.getMontant());
        paiement.setDatePaiement(paiementDto.getDatePaiement());
        paiement.setDette(dette);
        
        Paiement savedPaiement = paiementRepository.save(paiement);
        
        // Mettre à jour automatiquement les montants de la dette
        dette.addPaiement(savedPaiement);
        
        return convertToDto(savedPaiement);
    }
    
    /**
     * Obtenir les paiements d'une dette avec pagination
     */
    @Transactional(readOnly = true)
    public Page<PaiementDto> obtenirPaiementsDette(Long detteId, Pageable pageable) {
        // Vérifier que la dette existe
        detteService.obtenirDetteEntity(detteId);
        
        Page<Paiement> paiements = paiementRepository.findByDetteId(detteId, pageable);
        return paiements.map(this::convertToDto);
    }
    
    /**
     * Rechercher des paiements avec filtre sur le téléphone du client
     */
    @Transactional(readOnly = true)
    public Page<PaiementDto> rechercherPaiementsParTelephone(String telephone, Pageable pageable) {
        Page<Paiement> paiements = paiementRepository.findByClientTelephoneContaining(telephone, pageable);
        return paiements.map(this::convertToDto);
    }
    
    /**
     * Rechercher des paiements avec filtres multiples
     */
    @Transactional(readOnly = true)
    public Page<PaiementDto> rechercherPaiementsAvecFiltres(String telephone, Long detteId, 
                                                           BigDecimal montantMin, BigDecimal montantMax, 
                                                           Pageable pageable) {
        Page<Paiement> paiements = paiementRepository.findPaiementsWithFilters(
                telephone, detteId, montantMin, montantMax, pageable);
        return paiements.map(this::convertToDto);
    }
    
    /**
     * Obtenir tous les paiements avec pagination
     */
    @Transactional(readOnly = true)
    public Page<PaiementDto> obtenirTousLesPaiements(Pageable pageable) {
        Page<Paiement> paiements = paiementRepository.findAll(pageable);
        return paiements.map(this::convertToDto);
    }
    
    /**
     * Obtenir un paiement par ID
     */
    @Transactional(readOnly = true)
    public Optional<PaiementDto> obtenirPaiementParId(Long id) {
        Optional<Paiement> paiement = paiementRepository.findById(id);
        return paiement.map(this::convertToDto);
    }
    
    /**
     * Obtenir les paiements d'une dette (sans pagination) - triés par date de création desc
     */
    @Transactional(readOnly = true)
    public List<PaiementDto> obtenirPaiementsDetteOrdonnes(Long detteId) {
        Dette dette = detteService.obtenirDetteEntity(detteId);
        List<Paiement> paiements = paiementRepository.findByDetteOrderByDateCreationDesc(dette);
        return paiements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Mettre à jour un paiement
     */
    public PaiementDto mettreAJourPaiement(Long id, PaiementDto paiementDto) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec ID: " + id));
        
        Dette dette = paiement.getDette();
        
        // Calculer le nouveau montant restant de la dette en tenant compte du changement
        BigDecimal ancienMontant = paiement.getMontant();
        BigDecimal nouveauMontant = paiementDto.getMontant();
        BigDecimal differenceRestante = dette.getMontantRestant().add(ancienMontant).subtract(nouveauMontant);
        
        // Vérifier que le nouveau montant ne rend pas le paiement négatif
        if (differenceRestante.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Le nouveau montant du paiement est trop élevé pour cette dette");
        }
        
        paiement.setMontant(nouveauMontant);
        paiement.setDatePaiement(paiementDto.getDatePaiement());
        
        Paiement updatedPaiement = paiementRepository.save(paiement);
        
        // Recalculer les montants de la dette
        dette.calculerMontantPaye();
        
        return convertToDto(updatedPaiement);
    }
    
    /**
     * Supprimer un paiement
     */
    public void supprimerPaiement(Long id) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé avec ID: " + id));
        
        Dette dette = paiement.getDette();
        dette.removePaiement(paiement);
        
        paiementRepository.delete(paiement);
        
        // Recalculer automatiquement les montants de la dette
        dette.calculerMontantPaye();
    }
    
    /**
     * Obtenir le montant total des paiements d'une dette
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenirMontantTotalPaiementsDette(Long detteId) {
        Dette dette = detteService.obtenirDetteEntity(detteId);
        BigDecimal total = paiementRepository.getTotalPaiementsByDette(dette);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Vérifier si un paiement existe
     */
    @Transactional(readOnly = true)
    public boolean paiementExiste(Long id) {
        return paiementRepository.existsById(id);
    }
    
    /**
     * Effectuer un paiement complet d'une dette
     */
    public PaiementDto payerDetteCompletement(Long detteId, String datePaiement) {
        Dette dette = detteService.obtenirDetteEntity(detteId);
        
        if (dette.getMontantRestant().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Cette dette est déjà entièrement payée");
        }
        
        PaiementDto paiementDto = new PaiementDto();
        paiementDto.setMontant(dette.getMontantRestant());
        paiementDto.setDatePaiement(datePaiement);
        paiementDto.setDetteId(detteId);
        
        return ajouterPaiement(paiementDto);
    }
    
    /**
     * Obtenir les statistiques des paiements d'une dette
     */
    @Transactional(readOnly = true)
    public PaiementStatistiquesDto obtenirStatistiquesPaiementsDette(Long detteId) {
        Dette dette = detteService.obtenirDetteEntity(detteId);
        
        List<Paiement> paiements = dette.getPaiements();
        
        BigDecimal montantTotal = paiements.stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal montantMoyen = paiements.isEmpty() ? BigDecimal.ZERO : 
                montantTotal.divide(BigDecimal.valueOf(paiements.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        Optional<BigDecimal> montantMin = paiements.stream()
                .map(Paiement::getMontant)
                .min(BigDecimal::compareTo);
        
        Optional<BigDecimal> montantMax = paiements.stream()
                .map(Paiement::getMontant)
                .max(BigDecimal::compareTo);
        
        return new PaiementStatistiquesDto(
                montantTotal,
                montantMoyen,
                montantMin.orElse(BigDecimal.ZERO),
                montantMax.orElse(BigDecimal.ZERO),
                paiements.size(),
                dette.getMontantDette(),
                dette.getMontantRestant()
        );
    }
    
    /**
     * Convertir Paiement vers PaiementDto
     */
    private PaiementDto convertToDto(Paiement paiement) {
        PaiementDto dto = new PaiementDto();
        dto.setId(paiement.getId());
        dto.setMontant(paiement.getMontant());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setDateCreation(paiement.getDateCreation());
        dto.setDetteId(paiement.getDette().getId());
        dto.setClientTelephone(paiement.getDette().getClient().getTelephone());
        dto.setClientNom(paiement.getDette().getClient().getNom());
        dto.setMontantDetteTotal(paiement.getDette().getMontantDette());
        return dto;
    }
    
    // Classe interne pour les statistiques des paiements
    public static class PaiementStatistiquesDto {
        private BigDecimal montantTotalPaiements;
        private BigDecimal montantMoyenPaiement;
        private BigDecimal montantMinPaiement;
        private BigDecimal montantMaxPaiement;
        private int nombrePaiements;
        private BigDecimal montantTotalDette;
        private BigDecimal montantRestantDette;
        
        public PaiementStatistiquesDto(BigDecimal montantTotalPaiements, BigDecimal montantMoyenPaiement,
                                     BigDecimal montantMinPaiement, BigDecimal montantMaxPaiement,
                                     int nombrePaiements, BigDecimal montantTotalDette,
                                     BigDecimal montantRestantDette) {
            this.montantTotalPaiements = montantTotalPaiements;
            this.montantMoyenPaiement = montantMoyenPaiement;
            this.montantMinPaiement = montantMinPaiement;
            this.montantMaxPaiement = montantMaxPaiement;
            this.nombrePaiements = nombrePaiements;
            this.montantTotalDette = montantTotalDette;
            this.montantRestantDette = montantRestantDette;
        }
        
        // Getters
        public BigDecimal getMontantTotalPaiements() { return montantTotalPaiements; }
        public BigDecimal getMontantMoyenPaiement() { return montantMoyenPaiement; }
        public BigDecimal getMontantMinPaiement() { return montantMinPaiement; }
        public BigDecimal getMontantMaxPaiement() { return montantMaxPaiement; }
        public int getNombrePaiements() { return nombrePaiements; }
        public BigDecimal getMontantTotalDette() { return montantTotalDette; }
        public BigDecimal getMontantRestantDette() { return montantRestantDette; }
        public BigDecimal getPourcentagePaye() {
            if (montantTotalDette.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
            return montantTotalPaiements.divide(montantTotalDette, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
    }
}