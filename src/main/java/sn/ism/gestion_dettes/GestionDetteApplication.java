package sn.ism.gestion_dettes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class GestionDetteApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionDetteApplication.class, args);
        System.out.println("=================================");
        System.out.println("Application de Gestion des Dettes");
        System.out.println("=================================");
        System.out.println("API disponible sur: http://localhost:8080/api");
        System.out.println("Endpoints principaux:");
        System.out.println("- POST /api/clients - Ajouter un client");
        System.out.println("- GET  /api/clients - Lister les clients");
        System.out.println("- POST /api/dettes - Ajouter une dette");
        System.out.println("- GET  /api/dettes - Lister les dettes");
        System.out.println("- POST /api/paiements - Ajouter un paiement");
        System.out.println("- GET  /api/paiements - Lister les paiements");
        System.out.println("=================================");
    }
}