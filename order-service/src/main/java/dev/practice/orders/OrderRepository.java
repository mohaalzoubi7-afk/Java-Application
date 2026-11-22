package dev.practice.orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface OrderRepository extends JpaRepository<CustomerOrder,Long>{
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select o from CustomerOrder o where o.id = :id")
 Optional<CustomerOrder> findLocked(@Param("id") Long id);
}
