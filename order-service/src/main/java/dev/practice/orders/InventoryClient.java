package dev.practice.orders;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component public class InventoryClient {
 private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
 private final ObjectMapper json;
 private final String baseUrl;
 public InventoryClient(ObjectMapper json,@Value("${inventory.base-url}") String baseUrl){this.json=json;this.baseUrl=baseUrl.replaceAll("/$","");}
 public record StockResponse(String sku,String name,BigDecimal price,int quantity) {}
 public StockResponse reserve(String sku,int quantity){return change(sku,quantity,"reserve");}
 public StockResponse release(String sku,int quantity){return change(sku,quantity,"release");}
 private StockResponse change(String sku,int quantity,String action) {
  try {
   String path="/api/products/"+URLEncoder.encode(sku,StandardCharsets.UTF_8)+"/"+action;
   HttpRequest request=HttpRequest.newBuilder(URI.create(baseUrl+path))
    .timeout(Duration.ofSeconds(5)).header("Content-Type","application/json")
    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(new Quantity(quantity)))).build();
   HttpResponse<String> result=http.send(request,HttpResponse.BodyHandlers.ofString());
   if(result.statusCode()==404)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found in inventory");
   if(result.statusCode()==409)throw new ResponseStatusException(HttpStatus.CONFLICT,"Inventory rejected request: insufficient stock or conflict");
   if(result.statusCode()<200||result.statusCode()>=300)throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,"Inventory returned HTTP "+result.statusCode());
   return json.readValue(result.body(),StockResponse.class);
  } catch(ResponseStatusException e){throw e;}
    catch(InterruptedException e){Thread.currentThread().interrupt();throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Inventory request interrupted",e);}
    catch(Exception e){throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Inventory service unavailable",e);}
 }
 private record Quantity(int quantity) {}
}
