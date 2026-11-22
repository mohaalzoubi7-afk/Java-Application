package dev.practice.inventory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@RestController @RequestMapping("/api/products")
public class InventoryController {
 private final ProductRepository repo;
 public InventoryController(ProductRepository repo) { this.repo=repo; }
 public record CreateProduct(@NotBlank String sku,@NotBlank String name,@NotNull @DecimalMin("0.01") BigDecimal price,@Min(0) int quantity) {}
 public record QuantityChange(@Min(1) int quantity) {}
 public record ProductView(String sku,String name,BigDecimal price,int quantity) {
  static ProductView from(Product p) { return new ProductView(p.sku,p.name,p.price,p.quantity); }
 }
 @GetMapping public List<ProductView> all() {return repo.findAll().stream().map(ProductView::from).toList();}
 @GetMapping("/{sku}") public ProductView one(@PathVariable String sku) {return ProductView.from(repo.findById(sku).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"SKU not found")));}
 @PostMapping public ResponseEntity<ProductView> create(@Valid @RequestBody CreateProduct in) {
  if(repo.existsById(in.sku())) throw new ResponseStatusException(HttpStatus.CONFLICT,"SKU already exists");
  Product product=repo.save(new Product(in.sku(),in.name(),in.price(),in.quantity()));
  return ResponseEntity.status(HttpStatus.CREATED).body(ProductView.from(product));
 }
 @PostMapping("/{sku}/restock") @Transactional public ProductView restock(@PathVariable String sku,@Valid @RequestBody QuantityChange in) {
  Product p=locked(sku);p.quantity=Math.addExact(p.quantity,in.quantity());return ProductView.from(p);
 }
 @PostMapping("/{sku}/reserve") @Transactional public ProductView reserve(@PathVariable String sku,@Valid @RequestBody QuantityChange in) {
  Product p=locked(sku);
  if(p.quantity<in.quantity()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient stock");
  p.quantity-=in.quantity();return ProductView.from(p);
 }
 @PostMapping("/{sku}/release") @Transactional public ProductView release(@PathVariable String sku,@Valid @RequestBody QuantityChange in) {
  Product p=locked(sku);p.quantity=Math.addExact(p.quantity,in.quantity());return ProductView.from(p);
 }
 private Product locked(String sku) {return repo.findLocked(sku).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"SKU not found"));}
}
