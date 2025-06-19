package sn.ism.gestion_dettes.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import sn.ism.gestion_dettes.entities.Dette;
import sn.ism.gestion_dettes.entities.Paiement;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    
    List<Paiement> findByDette(Dette dette);
    
    Page<Paiement> findByDette(Dette dette, Pageable pageable);
    
    @Query("SELECT p FROM Paiement p WHERE p.dette.client.telephone LIKE %:telephone%")
    Page<Paiement> findByClientTelephoneContaining(@Param("telephone") String telephone, 
                                                  Pageable pageable);
    
    @Query("SELECT p FROM Paiement p WHERE p.dette.id = :detteId")
    Page<Paiement> findByDetteId(@Param("detteId") Long detteId, Pageable pageable);
    
    @Query("SELECT p FROM Paiement p WHERE " +
           "(:telephone IS NULL OR p.dette.client.telephone LIKE %:telephone%) AND " +
           "(:detteId IS NULL OR p.dette.id = :detteId) AND " +
           "(:montantMin IS NULL OR p.montant >= :montantMin) AND " +
           "(:montantMax IS NULL OR p.montant <= :montantMax)")
    Page<Paiement> findPaiementsWithFilters(@Param("telephone") String telephone,
                                           @Param("detteId") Long detteId,
                                           @Param("montantMin") BigDecimal montantMin,
                                           @Param("montantMax") BigDecimal montantMax,
                                           Pageable pageable);
    
    @Query("SELECT SUM(p.montant) FROM Paiement p WHERE p.dette = :dette")
    BigDecimal getTotalPaiementsByDette(@Param("dette") Dette dette);
    
    @Query("SELECT p FROM Paiement p WHERE p.dette = :dette ORDER BY p.dateCreation DESC")
    List<Paiement> findByDetteOrderByDateCreationDesc(@Param("dette") Dette dette);
}