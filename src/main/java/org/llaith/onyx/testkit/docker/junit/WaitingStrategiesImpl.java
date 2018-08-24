package org.llaith.onyx.testkit.docker.junit;

import org.rnorth.ducttape.ratelimits.RateLimiter;
import org.rnorth.ducttape.ratelimits.RateLimiterBuilder;
import org.rnorth.ducttape.timeouts.Timeouts;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
public class WaitingStrategiesImpl<C extends DockerConfig<C,R>, R extends DockerResource<C,R>> implements WaitingStrategies<C,R>, WaitingStrategy<C,R> {

    private static final Logger logger = getLogger(GenericWaitingStrategies.class);

    private final int maxWaitSecs;

    private final int callRatePerMin;

    private final int timeoutSeconds;

    private final List<WaitingStrategy<C,R>> waitingStrategies = new ArrayList<>();

    public WaitingStrategiesImpl() {

        this(60, 6, 10);

    }

    public WaitingStrategiesImpl(final int maxWaitSecs, final int callRatePerMin, final int timeoutSeconds) {
        this.maxWaitSecs = maxWaitSecs;
        this.callRatePerMin = callRatePerMin;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public WaitingStrategies<C,R> addStrategy(final WaitingStrategy<C,R> strategy) {

        this.waitingStrategies.add(strategy);

        return this;

    }


    @Override
    public void waitForContainer(final R resource) {

        // abort if no strategies added
        if (this.waitingStrategies.isEmpty()) return;

        // get a rate limiter
        final RateLimiter rateLimiter = RateLimiterBuilder
                .newBuilder()
                .withRate(callRatePerMin, TimeUnit.MINUTES)
                .withConstantThroughput()
                .build();

        // Retry the call for up to 2s if an exception is thrown
        Unreliables.retryUntilTrue(maxWaitSecs, TimeUnit.SECONDS, () -> {

            logger.debug(String.format("retrying for max of %s seconds", maxWaitSecs));

            // Limit calls to a max rate of x per minute
            rateLimiter.doWhenReady(() -> {

                logger.debug(String.format("rate-limited try of %s per minute", callRatePerMin));

                // Limit each call to 100ms
                Timeouts.doWithTimeout(
                        timeoutSeconds,
                        TimeUnit.SECONDS,
                        () -> {

                            logger.debug(String.format("trying with timeout of %s seconds", timeoutSeconds));

                            this.waitingStrategies.forEach((strategy) -> strategy.waitForContainer(resource));

                        });

            });

            return true;

        });

    }

}