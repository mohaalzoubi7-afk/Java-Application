package dev.practice.orders;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class OrderControllerTest {
 @Test void placingOrderReservesStockAndCreatesOrder(){
  OrderRepository repo=mock(OrderRepository.class);
  InventoryClient inventory=mock(InventoryClient.class);
  when(inventory.reserve("SKU-1",2)).thenReturn(new InventoryClient.StockResponse("SKU-1","Item",new BigDecimal("10.00"),8));
  when(repo.save(any(CustomerOrder.class))).thenAnswer(call->{var order=call.getArgument(0,CustomerOrder.class);order.id=1L;return order;});
  var response=new OrderController(repo,inventory).create(new OrderController.NewOrder("student@example.com","SKU-1",2));
  assertEquals(201,response.getStatusCode().value());
  assertEquals(new BigDecimal("20.00"),response.getBody().total());
  verify(inventory).reserve("SKU-1",2);
 }
 @Test void cancelTwiceReturnsConflict(){
  OrderRepository repo=mock(OrderRepository.class);
  InventoryClient inventory=mock(InventoryClient.class);
  var order=new CustomerOrder("student@example.com","SKU-1",1,new BigDecimal("10"));
  order.id=1L;order.status=CustomerOrder.Status.CANCELLED;
  when(repo.findById(1L)).thenReturn(Optional.of(order));
  var error=assertThrows(ResponseStatusException.class,()->new OrderController(repo,inventory).cancel(1L));
  assertEquals(409,error.getStatusCode().value());
  verifyNoInteractions(inventory);
 }
}
