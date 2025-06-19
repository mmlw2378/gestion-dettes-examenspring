package sn.ism.gestion_dettes.services;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.ism.gestion_dettes.dto.ClientDto;
import sn.ism.gestion_dettes.entities.Client;
import sn.ism.gestion_dettes.repositories.ClientRepository;

@Service
@Transactional
public class ClientService {
    
    @Autowired
    private ClientRepository clientRepository;
    
    /**
     * Ajouter un nouveau client
     */
    public ClientDto ajouterClient(ClientDto clientDto) {
        // Vérifier si le téléphone existe déjà
        if (clientRepository.existsByTelephone(clientDto.getTelephone())) {
            throw new RuntimeException("Un client avec ce numéro de téléphone existe déjà");
        }
        
        Client client = new Client();
        client.setNom(clientDto.getNom());
        client.setTelephone(clientDto.getTelephone());
        client.setAdresse(clientDto.getAdresse());
        
        Client savedClient = clientRepository.save(client);
        return convertToDto(savedClient);
    }
    
    /**
     * Obtenir tous les clients avec pagination
     */
    @Transactional(readOnly = true)
    public Page<ClientDto> obtenirTousLesClients(Pageable pageable) {
        Page<Client> clients = clientRepository.findAll(pageable);
        return clients.map(this::convertToDto);
    }
    
    /**
     * Rechercher des clients par téléphone avec pagination
     */
    @Transactional(readOnly = true)
    public Page<ClientDto> rechercherClientsParTelephone(String telephone, Pageable pageable) {
        Page<Client> clients = clientRepository.findByTelephoneContaining(telephone, pageable);
        return clients.map(this::convertToDto);
    }
    
    /**
     * Rechercher des clients avec filtres
     */
    @Transactional(readOnly = true)
    public Page<ClientDto> rechercherClientsAvecFiltres(String nom, String telephone, Pageable pageable) {
        Page<Client> clients = clientRepository.findClientsWithFilters(nom, telephone, pageable);
        return clients.map(this::convertToDto);
    }
    
    /**
     * Obtenir un client par ID
     */
    @Transactional(readOnly = true)
    public Optional<ClientDto> obtenirClientParId(Long id) {
        Optional<Client> client = clientRepository.findById(id);
        return client.map(this::convertToDto);
    }
    
    /**
     * Obtenir un client par téléphone
     */
    @Transactional(readOnly = true)
    public Optional<ClientDto> obtenirClientParTelephone(String telephone) {
        Optional<Client> client = clientRepository.findByTelephone(telephone);
        return client.map(this::convertToDto);
    }
    
    /**
     * Mettre à jour un client
     */
    public ClientDto mettreAJourClient(Long id, ClientDto clientDto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec ID: " + id));
        
        // Vérifier si le nouveau téléphone existe déjà (sauf pour ce client)
        if (!client.getTelephone().equals(clientDto.getTelephone()) && 
            clientRepository.existsByTelephone(clientDto.getTelephone())) {
            throw new RuntimeException("Un autre client avec ce numéro de téléphone existe déjà");
        }
        
        client.setNom(clientDto.getNom());
        client.setTelephone(clientDto.getTelephone());
        client.setAdresse(clientDto.getAdresse());
        
        Client updatedClient = clientRepository.save(client);
        return convertToDto(updatedClient);
    }
    
    /**
     * Supprimer un client
     */
    public void supprimerClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec ID: " + id));
        
        // Vérifier si le client a des dettes
        if (!client.getDettes().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer le client car il a des dettes associées");
        }
        
        clientRepository.delete(client);
    }
    
    /**
     * Vérifier si un client existe
     */
    @Transactional(readOnly = true)
    public boolean clientExiste(Long id) {
        return clientRepository.existsById(id);
    }
    
    /**
     * Obtenir l'entité Client (pour usage interne)
     */
    @Transactional(readOnly = true)
    public Client obtenirClientEntity(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec ID: " + id));
    }
    
    /**
     * Convertir Client vers ClientDto
     */
    private ClientDto convertToDto(Client client) {
        return new ClientDto(
                client.getId(),
                client.getNom(),
                client.getTelephone(),
                client.getAdresse()
        );
    }
    
    /**
     * Obtenir tous les clients (sans pagination) - pour les listes déroulantes
     */
    @Transactional(readOnly = true)
    public List<ClientDto> obtenirTousLesClientsSimplement() {
        List<Client> clients = clientRepository.findAll();
        return clients.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}