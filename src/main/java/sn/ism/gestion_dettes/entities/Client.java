package sn.ism.gestion_dettes.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "clients")
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Format de téléphone invalide")
    private String telephone;
    
    @Column(nullable = false)
    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Dette> dettes = new ArrayList<>();
    
    // Constructeurs
    public Client() {}
    
    public Client(String nom, String telephone, String adresse) {
        this.nom = nom;
        this.telephone = telephone;
        this.adresse = adresse;
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public List<Dette> getDettes() {
        return dettes;
    }
    
    public void setDettes(List<Dette> dettes) {
        this.dettes = dettes;
    }
    
    // Méthodes utilitaires
    public void addDette(Dette dette) {
        dettes.add(dette);
        dette.setClient(this);
    }
    
    public void removeDette(Dette dette) {
        dettes.remove(dette);
        dette.setClient(null);
    }
    
    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", adresse='" + adresse + '\'' +
                '}';
    }
}