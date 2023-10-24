package be.ugent.devops.services.logic.utils;

import be.ugent.devops.commons.model.BaseMove;
import be.ugent.devops.commons.model.BaseMoveInput;
import be.ugent.devops.commons.model.UnitMove;
import be.ugent.devops.commons.model.UnitMoveInput;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class ServiceStats {

    private final Long onlineSinceTimestamp = System.currentTimeMillis();
    private final AtomicLong totalRequests = new AtomicLong(0);
    @JsonIgnore
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private Long lastRequestTimestamp = -1L;
    private String currentGameId = null;

    private <I, O> O baseWrapExecution(I input, Function<I, O> processor) {
        long start = System.currentTimeMillis();
        try {
            return processor.apply(input);
        } catch (Throwable t) {
            failedRequests.incrementAndGet();
            throw t;
        } finally {
            lastRequestTimestamp = start;
            totalRequests.incrementAndGet();
            totalProcessingTime.addAndGet(System.currentTimeMillis() - start);
        }
    }

    public BaseMove wrapBaseMove(BaseMoveInput baseMoveInput, Function<BaseMoveInput, BaseMove> processor) {
        currentGameId = baseMoveInput.context().gameId();
        return baseWrapExecution(baseMoveInput, processor);
    }

    public UnitMove wrapUnitMove(UnitMoveInput unitMoveInput, Function<UnitMoveInput, UnitMove> processor) {
        return baseWrapExecution(unitMoveInput, processor);
    }

    public String getOnlineSince() {
        return Instant.ofEpochMilli(onlineSinceTimestamp).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    public Long getTotalRequests() {
        return totalRequests.get();
    }

    public Long getFailedRequests() {
        return failedRequests.get();
    }

    public Double getMeanProcessingTime() {
        return Long.valueOf(totalProcessingTime.get()).doubleValue() / totalRequests.get();
    }

    public String getLastRequestAt() {
        return Instant.ofEpochMilli(lastRequestTimestamp).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    public String getCurrentGameId() {
        return currentGameId;
    }
}
