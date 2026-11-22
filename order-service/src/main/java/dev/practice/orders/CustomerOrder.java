package dev.practice.orders;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="customer_orders") public class CustomerOrder {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String customerEmail;
 @Column(nullable=false) public String sku;
 @Column(nullable=false) public int quantity;
 @Column(nullable=false) public BigDecimal unitPrice;
 @Enumerated(EnumType.STRING) @Column(nullable=false) public Status status;
 @Column(nullable=false) public Instant createdAt;
 @Version public long version;
 protected CustomerOrder() {}
 public CustomerOrder(String email,String sku,int quantity,BigDecimal unitPrice) {
  this.customerEmail=email;this.sku=sku;this.quantity=quantity;this.unitPrice=unitPrice;this.status=Status.CONFIRMED;this.createdAt=Instant.now();
 }
 public enum Status {CONFIRMED,CANCELLED}
}
