package com.rushtix.core.unit;

import com.rushtix.core.service.PricingService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PricingServiceTest {

    private final PricingService pricingService = new PricingService();

    @Test
    public void calculateGroupSplit_WithUnevenAmount_ShouldDistributeRemainderToFirstPerson() {
        
        // ARRANGE: $100 split among 3 friends
        BigDecimal totalAmount = new BigDecimal("100.00");
        int numberOfFriends = 3;

        // ACT: Call the method that calculates the splits
        List<BigDecimal> splits = pricingService.calculateGroupSplit(totalAmount, numberOfFriends);

        // ASSERT: 
        // We expect exactly 3 payments.
        assertEquals(3, splits.size());

        // The first person should pay $33.34 (absorbing the extra penny)
        assertEquals(new BigDecimal("33.34"), splits.get(0));

        // The other two should pay $33.33
        assertEquals(new BigDecimal("33.33"), splits.get(1));
        assertEquals(new BigDecimal("33.33"), splits.get(2));
        
        // Verify the total equals exactly $100.00
        BigDecimal sum = splits.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("100.00"), sum);
    }
}
