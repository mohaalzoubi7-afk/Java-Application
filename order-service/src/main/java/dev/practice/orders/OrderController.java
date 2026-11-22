package dev.practice.orders;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController @RequestMapping("/api/orders") public class OrderController {
 private final OrderRepository orders;
 private final InventoryClient inventory;
 public OrderController(OrderRepository orders,InventoryClient inventory){this.orders=orders;this.inventory=inventory;}
 public record NewOrder(@NotBlank @Email String customerEmail,@NotBlank String sku,@Min(1) int quantity){}
 public record OrderView(Long id,String customerEmail,String sku,int quantity,BigDecimal unitPrice,BigDecimal total,CustomerOrder.Status status,Instant createdAt) {
  static OrderView from(CustomerOrder o){return new OrderView(o.id,o.customerEmail,o.sku,o.quantity,o.unitPrice,o.unitPrice.multiply(BigDecimal.valueOf(o.quantity)),o.status,o.createdAt);}
 }
 @PostMapping public ResponseEntity<OrderView> create(@Valid @RequestBody NewOrder request){
  var stock=inventory.reserve(request.sku(),request.quantity());
  try {
   var saved=orders.save(new CustomerOrder(request.customerEmail(),request.sku(),request.quantity(),stock.price()));
   return ResponseEntity.status(HttpStatus.CREATED).body(OrderView.from(saved));
  }catch(RuntimeException exception){
   try{inventory.release(request.sku(),request.quantity());}catch(RuntimeException releaseFailed){exception.addSuppressed(releaseFailed);}
   throw exception;
  }
 }
 @GetMapping public List<OrderView> all(){return orders.findAll().stream().map(OrderView::from).toList();}
 @GetMapping("/{id}") public OrderView one(@PathVariable Long id){return OrderView.from(orders.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found")));}
 @PostMapping("/{id}/cancel") public OrderView cancel(@PathVariable Long id){
  var order=orders.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found"));
  if(order.status==CustomerOrder.Status.CANCELLED)throw new ResponseStatusException(HttpStatus.CONFLICT,"Order already cancelled");
  inventory.release(order.sku,order.quantity);
  order.status=CustomerOrder.Status.CANCELLED;
  return OrderView.from(orders.save(order));
 }
}
