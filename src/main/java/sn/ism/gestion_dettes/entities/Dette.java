package sn.ism.gestion_dettes.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "dettes")
public class Dette {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @NotBlank(message = "La date est obligatoire")
    private String date;
    
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Le montant de la dette est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant de la dette doit être positif")
    private BigDecimal montantDette;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantPaye = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montantRestant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "Le client est obligatoire")
    private Client client;
    
    @OneToMany(mappedBy = "dette", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Paiement> paiements = new ArrayList<>();
    
    // Constructeurs
    public Dette() {
        calculerMontantRestant();
    }
    
    public Dette(String date, BigDecimal montantDette, Client client) {
        this.date = date;
        this.montantDette = montantDette;
        this.client = client;
        this.montantPaye = BigDecimal.ZERO;
        calculerMontantRestant();
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public BigDecimal getMontantDette() {
        return montantDette;
    }
    
    public void setMontantDette(BigDecimal montantDette) {
        this.montantDette = montantDette;
        calculerMontantRestant();
    }
    
    public BigDecimal getMontantPaye() {
        return montantPaye;
    }
    
    public void setMontantPaye(BigDecimal montantPaye) {
        this.montantPaye = montantPaye;
        calculerMontantRestant();
    }
    
    public BigDecimal getMontantRestant() {
        return montantRestant;
    }
    
    public void setMontantRestant(BigDecimal montantRestant) {
        this.montantRestant = montantRestant;
    }
    
    public Client getClient() {
        return client;
    }
    
    public void setClient(Client client) {
        this.client = client;
    }
    
    public List<Paiement> getPaiements() {
        return paiements;
    }
    
    public void setPaiements(List<Paiement> paiements) {
        this.paiements = paiements;
        calculerMontantPaye();
    }
    
    // Méthodes utilitaires
    public void addPaiement(Paiement paiement) {
        paiements.add(paiement);
        paiement.setDette(this);
        calculerMontantPaye();
    }
    
    public void removePaiement(Paiement paiement) {
        paiements.remove(paiement);
        paiement.setDette(null);
        calculerMontantPaye();
    }
    
    /**
     * Calcule automatiquement le montant payé = somme des paiements
     */
    public void calculerMontantPaye() {
        this.montantPaye = paiements.stream()
                .map(Paiement::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        calculerMontantRestant();
    }
    
    /**
     * Calcule automatiquement le montant restant = montantDette - montantPaye
     */
    private void calculerMontantRestant() {
        if (montantDette != null && montantPaye != null) {
            this.montantRestant = montantDette.subtract(montantPaye);
        }
    }
    
    /**
     * Vérifie si la dette est complètement payée
     */
    public boolean isCompletelyPaid() {
        return montantRestant != null && montantRestant.compareTo(BigDecimal.ZERO) <= 0;
    }
    
    @PrePersist
    @PreUpdate
    private void beforeSave() {
        calculerMontantPaye();
    }
    
    @Override
    public String toString() {
        return "Dette{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", montantDette=" + montantDette +
                ", montantPaye=" + montantPaye +
                ", montantRestant=" + montantRestant +
                '}';
    }
}