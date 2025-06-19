package sn.ism.gestion_dettes.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import sn.ism.gestion_dettes.entities.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    Optional<Client> findByTelephone(String telephone);
    
    boolean existsByTelephone(String telephone);
    
    @Query("SELECT c FROM Client c WHERE c.telephone LIKE %:telephone%")
    Page<Client> findByTelephoneContaining(@Param("telephone") String telephone, Pageable pageable);
    
    @Query("SELECT c FROM Client c WHERE " +
           "(:nom IS NULL OR LOWER(c.nom) LIKE LOWER(CONCAT('%', :nom, '%'))) AND " +
           "(:telephone IS NULL OR c.telephone LIKE %:telephone%)")
    Page<Client> findClientsWithFilters(@Param("nom") String nom, 
                                       @Param("telephone") String telephone, 
                                       Pageable pageable);
}

