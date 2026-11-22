package dev.practice.inventory;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class InventoryControllerTest {
 @Test void reserveRejectsInsufficientStock(){
  ProductRepository repo=mock(ProductRepository.class);
  when(repo.findLocked("SKU-1")).thenReturn(Optional.of(new Product("SKU-1","Sample",new BigDecimal("10.00"),2)));
  var controller=new InventoryController(repo);
  var error=assertThrows(ResponseStatusException.class,()->controller.reserve("SKU-1",new InventoryController.QuantityChange(3)));
  assertEquals(409,error.getStatusCode().value());
 }
 @Test void reserveDecrementsStock(){
  ProductRepository repo=mock(ProductRepository.class);
  var product=new Product("SKU-1","Sample",new BigDecimal("10.00"),5);
  when(repo.findLocked("SKU-1")).thenReturn(Optional.of(product));
  var view=new InventoryController(repo).reserve("SKU-1",new InventoryController.QuantityChange(2));
  assertEquals(3,view.quantity());
 }
}
