package sn.ism.gestion_dettes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ClientDto {
    
    private Long id;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Format de téléphone invalide")
    private String telephone;
    
    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;
    
    // Constructeurs
    public ClientDto() {}
    
    public ClientDto(String nom, String telephone, String adresse) {
        this.nom = nom;
        this.telephone = telephone;
        this.adresse = adresse;
    }
    
    public ClientDto(Long id, String nom, String telephone, String adresse) {
        this.id = id;
        this.nom = nom;
        this.telephone = telephone;
        this.adresse = adresse;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    @Override
    public String toString() {
        return "ClientDto{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", telephone='" + telephone + '\'' +
                ", adresse='" + adresse + '\'' +
                '}';
    }
}