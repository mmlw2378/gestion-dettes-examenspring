package sn.ism.gestion_dettes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaiementDto {
    
    private Long id;
    
    @NotNull(message = "Le montant du paiement est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant du paiement doit être positif")
    private BigDecimal montant;
    
    @NotBlank(message = "La date du paiement est obligatoire")
    private String datePaiement;
    
    private LocalDateTime dateCreation;
    
    @NotNull(message = "L'ID de la dette est obligatoire")
    private Long detteId;
    
    private String clientTelephone;
    private String clientNom;
    private BigDecimal montantDetteTotal;
    
    // Constructeurs
    public PaiementDto() {}
    
    public PaiementDto(BigDecimal montant, String datePaiement, Long detteId) {
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.detteId = detteId;
    }
    
    public PaiementDto(Long id, BigDecimal montant, String datePaiement, 
                      LocalDateTime dateCreation, Long detteId, 
                      String clientTelephone, String clientNom) {
        this.id = id;
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.dateCreation = dateCreation;
        this.detteId = detteId;
        this.clientTelephone = clientTelephone;
        this.clientNom = clientNom;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }
    
    public String getDatePaiement() { return datePaiement; }
    public void setDatePaiement(String datePaiement) { this.datePaiement = datePaiement; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public Long getDetteId() { return detteId; }
    public void setDetteId(Long detteId) { this.detteId = detteId; }
    
    public String getClientTelephone() { return clientTelephone; }
    public void setClientTelephone(String clientTelephone) { this.clientTelephone = clientTelephone; }
    
    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }
    
    public BigDecimal getMontantDetteTotal() { return montantDetteTotal; }
    public void setMontantDetteTotal(BigDecimal montantDetteTotal) { this.montantDetteTotal = montantDetteTotal; }
    
    @Override
    public String toString() {
        return "PaiementDto{" +
                "id=" + id +
                ", montant=" + montant +
                ", datePaiement='" + datePaiement + '\'' +
                ", dateCreation=" + dateCreation +
                ", detteId=" + detteId +
                '}';
    }
}