package sn.ism.gestion_dettes.repositories;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import sn.ism.gestion_dettes.entities.Client;
import sn.ism.gestion_dettes.entities.Dette;

@Repository
public interface DetteRepository extends JpaRepository<Dette, Long> {
    
    List<Dette> findByClient(Client client);
    
    Page<Dette> findByClient(Client client, Pageable pageable);
    
    @Query("SELECT d FROM Dette d WHERE d.client.telephone LIKE %:telephone%")
    Page<Dette> findByClientTelephoneContaining(@Param("telephone") String telephone, 
                                               Pageable pageable);
    
    @Query("SELECT d FROM Dette d WHERE d.client = :client AND d.client.telephone LIKE %:telephone%")
    Page<Dette> findByClientAndTelephoneFilter(@Param("client") Client client, 
                                             @Param("telephone") String telephone, 
                                             Pageable pageable);
    
    @Query("SELECT d FROM Dette d WHERE " +
           "(:clientId IS NULL OR d.client.id = :clientId) AND " +
           "(:telephone IS NULL OR d.client.telephone LIKE %:telephone%) AND " +
           "(:montantMin IS NULL OR d.montantDette >= :montantMin) AND " +
           "(:montantMax IS NULL OR d.montantDette <= :montantMax)")
    Page<Dette> findDettesWithFilters(@Param("clientId") Long clientId,
                                     @Param("telephone") String telephone,
                                     @Param("montantMin") BigDecimal montantMin,
                                     @Param("montantMax") BigDecimal montantMax,
                                     Pageable pageable);
    
    @Query("SELECT d FROM Dette d WHERE d.montantRestant > 0")
    Page<Dette> findDettesNonPayees(Pageable pageable);
    
    @Query("SELECT d FROM Dette d WHERE d.montantRestant = 0")
    Page<Dette> findDettesPayees(Pageable pageable);
    
    @Query("SELECT SUM(d.montantDette) FROM Dette d WHERE d.client = :client")
    BigDecimal getTotalDetteByClient(@Param("client") Client client);
    
    @Query("SELECT SUM(d.montantRestant) FROM Dette d WHERE d.client = :client")
    BigDecimal getMontantRestantByClient(@Param("client") Client client);
}
