package dev.practice.inventory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
@Configuration public class DataSeeder {
 @Bean CommandLineRunner seedProducts(ProductRepository repo) {return args -> {
  if(!repo.existsById("LAPTOP-01"))repo.save(new Product("LAPTOP-01","Developer Laptop",new BigDecimal("850.00"),12));
  if(!repo.existsById("MOUSE-01"))repo.save(new Product("MOUSE-01","Wireless Mouse",new BigDecimal("25.00"),30));
  if(!repo.existsById("KB-01"))repo.save(new Product("KB-01","Mechanical Keyboard",new BigDecimal("90.00"),15));
 };}
}
