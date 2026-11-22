package dev.practice.inventory;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="products")
public class Product {
 @Id public String sku;
 @Column(nullable=false) public String name;
 @Column(nullable=false) public BigDecimal price;
 @Column(nullable=false) public int quantity;
 @Version public long version;
 protected Product() {}
 public Product(String sku,String name,BigDecimal price,int quantity) {this.sku=sku;this.name=name;this.price=price;this.quantity=quantity;}
}
