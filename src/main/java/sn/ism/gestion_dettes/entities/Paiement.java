package sn.ism.gestion_dettes.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "paiements")
public class Paiement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Le montant du paiement est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant du paiement doit être positif")
    private BigDecimal montant;
    
    @Column(nullable = false)
    @NotBlank(message = "La date du paiement est obligatoire")
    private String datePaiement;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dette_id", nullable = false)
    @NotNull(message = "La dette est obligatoire")
    private Dette dette;
    
    // Constructeurs
    public Paiement() {
        this.dateCreation = LocalDateTime.now();
    }
    
    public Paiement(BigDecimal montant, String datePaiement, Dette dette) {
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.dette = dette;
        this.dateCreation = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public BigDecimal getMontant() {
        return montant;
    }
    
    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }
    
    public String getDatePaiement() {
        return datePaiement;
    }
    
    public void setDatePaiement(String datePaiement) {
        this.datePaiement = datePaiement;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public Dette getDette() {
        return dette;
    }
    
    public void setDette(Dette dette) {
        this.dette = dette;
    }
    
    @PrePersist
    private void beforeSave() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
    
    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", montant=" + montant +
                ", datePaiement='" + datePaiement + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }
}