package sn.ism.gestion_dettes.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DetteDto {
    
    private Long id;
    
    @NotBlank(message = "La date est obligatoire")
    private String date;
    
    @NotNull(message = "Le montant de la dette est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant de la dette doit être positif")
    private BigDecimal montantDette;
    
    private BigDecimal montantPaye = BigDecimal.ZERO;
    private BigDecimal montantRestant;
    
    @NotNull(message = "L'ID du client est obligatoire")
    private Long clientId;
    
    private String clientNom;
    private String clientTelephone;
    
    // Constructeurs
    public DetteDto() {}
    
    public DetteDto(String date, BigDecimal montantDette, Long clientId) {
        this.date = date;
        this.montantDette = montantDette;
        this.clientId = clientId;
        this.montantPaye = BigDecimal.ZERO;
        calculerMontantRestant();
    }
    
    public DetteDto(Long id, String date, BigDecimal montantDette, BigDecimal montantPaye, 
                   Long clientId, String clientNom, String clientTelephone) {
        this.id = id;
        this.date = date;
        this.montantDette = montantDette;
        this.montantPaye = montantPaye;
        this.clientId = clientId;
        this.clientNom = clientNom;
        this.clientTelephone = clientTelephone;
        calculerMontantRestant();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public BigDecimal getMontantDette() { return montantDette; }
    public void setMontantDette(BigDecimal montantDette) { 
        this.montantDette = montantDette;
        calculerMontantRestant();
    }
    
    public BigDecimal getMontantPaye() { return montantPaye; }
    public void setMontantPaye(BigDecimal montantPaye) { 
        this.montantPaye = montantPaye;
        calculerMontantRestant();
    }
    
    public BigDecimal getMontantRestant() { return montantRestant; }
    public void setMontantRestant(BigDecimal montantRestant) { this.montantRestant = montantRestant; }
    
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    
    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }
    
    public String getClientTelephone() { return clientTelephone; }
    public void setClientTelephone(String clientTelephone) { this.clientTelephone = clientTelephone; }
    
    // Méthode utilitaire
    private void calculerMontantRestant() {
        if (montantDette != null && montantPaye != null) {
            this.montantRestant = montantDette.subtract(montantPaye);
        }
    }
    
    @Override
    public String toString() {
        return "DetteDto{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", montantDette=" + montantDette +
                ", montantPaye=" + montantPaye +
                ", montantRestant=" + montantRestant +
                ", clientId=" + clientId +
                '}';
    }
}