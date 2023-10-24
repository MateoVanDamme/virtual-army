package be.ugent.devops.services.logic.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.google.common.collect.EvictingQueue;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.ext.web.Router;

import java.util.List;

public class RemoteLogAppender extends AppenderBase<ILoggingEvent> {

    private static final int BUFFER_SIZE = 500;
    private static final String LOG_FETCHER_ENDPOINT = "/logs";
    private static final EvictingQueue<String> buffer = EvictingQueue.create(BUFFER_SIZE);

    @Override
    protected void append(ILoggingEvent e) {
        synchronized (buffer) {
            buffer.add(e.toString());
        }
    }

    public static void registerHttpEndpoint(Router router) {
        router.get(LOG_FETCHER_ENDPOINT).handler(ctx -> {
            ctx.vertx().rxExecuteBlocking(promise -> {
                synchronized (buffer) {
                    var output = new JsonArray(List.copyOf(buffer));
                    buffer.clear();
                    promise.complete(output);
                }
            }).subscribe(
                    ctx::json,
                    Throwable::printStackTrace
            );
        });
    }

}
