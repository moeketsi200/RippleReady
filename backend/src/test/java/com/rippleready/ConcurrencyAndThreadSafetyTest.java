package com.rippleready;

import com.rippleready.model.IncidentRecord;
import com.rippleready.model.IncidentReportRequest;
import com.rippleready.model.MutualAidResource;
import com.rippleready.model.PledgeRequest;
import com.rippleready.service.DataStore;
import com.rippleready.service.IncidentService;
import com.rippleready.service.PledgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcurrencyAndThreadSafetyTest {

    @Autowired
    private DataStore dataStore;

    @Autowired
    private PledgeService pledgeService;

    @Autowired
    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        dataStore.reset();
    }

    @Test
    @DisplayName("Concurrency: Simultaneous pledges never over-allocate provider capacity")
    void testConcurrentPledgesNeverOverAllocate() throws Exception {
        // Hendricks Hardware has 1500L available
        int numberOfThreads = 10;
        double unitsPerPledge = 200.0; // 10 * 200 = 2000L (exceeds 1500L)

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successfulPledges = new AtomicInteger(0);
        AtomicInteger rejectedPledges = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // ensure all threads fire at the exact same instant
                    pledgeService.createPledge(new PledgeRequest(
                            "res-1",
                            "fac-1",
                            unitsPerPledge,
                            "+27 82 555 0192",
                            "+27 82 555 9901",
                            "TOKEN-CONCUR-" + index,
                            "Concurrent pledge test"
                    ));
                    successfulPledges.incrementAndGet();
                } catch (IllegalStateException e) {
                    // Expected when capacity is exhausted
                    rejectedPledges.incrementAndGet();
                } catch (Exception e) {
                    // Unexpected error
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Fire all threads simultaneously
        startLatch.countDown();
        boolean finished = doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(finished).isTrue();

        // Exactly 7 pledges can succeed (7 * 200 = 1400L <= 1500L)
        // 3 pledges must be rejected because 1500 - 1400 = 100L < 200L
        assertThat(successfulPledges.get()).isEqualTo(7);
        assertThat(rejectedPledges.get()).isEqualTo(3);

        MutualAidResource provider = dataStore.getResourceById("res-1").get();
        assertThat(provider.availableCapacity()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Concurrency: Parallel incident reports at the same coordinates are safely deduplicated")
    void testConcurrentIncidentReportsDeduplicatedSafely() throws Exception {
        int numberOfThreads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        List<IncidentRecord> results = new CopyOnWriteArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    IncidentRecord record = incidentService.reportIncident(new IncidentReportRequest(
                            "citizen-" + index,
                            "Water",
                            -33.9630,
                            22.4610,
                            "CRITICAL",
                            "Ward 18, George",
                            "Burst on York"
                    ));
                    results.add(record);
                } catch (Exception e) {
                    // Ignore
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(results).hasSize(numberOfThreads);

        // In DataStore, there should be exactly 1 unique incident ID recorded
        List<IncidentRecord> stored = dataStore.getAllIncidents();
        assertThat(stored).hasSize(1);
        assertThat(stored.get(0).id()).isEqualTo("inc-001");
        assertThat(stored.get(0).confirmationVotes()).isEqualTo(numberOfThreads);
    }
}

