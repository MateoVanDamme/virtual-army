package be.ugent.devops.services.logic.utils;

import be.ugent.devops.commons.model.BaseMoveInput;
import be.ugent.devops.commons.model.UnitMoveInput;
import be.ugent.devops.services.logic.FactionLogicImpl;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpBinding extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(HttpBinding.class);

    @Override
    public Completable rxStart() {
        return ConfigRetriever.create(vertx)
                .rxGetConfig()
                .flatMapCompletable(config -> {
                    var router = Router.router(vertx);

                    var serviceStats = new ServiceStats();
                    var factionLogic = new FactionLogicImpl(config);

                    if (config.getBoolean(Constants.SECURE_ENDPOINTS_PROPERTY, Constants.DEFAULT_SECURE_ENDPOINTS) && config.containsKey(Constants.SECURE_KEY_PROPERTY)) {
                        var secureKey = config.getString(Constants.SECURE_KEY_PROPERTY);
                        router.route("/*").handler(ctx -> {
                            if (secureKey.equals(ctx.request().getHeader(Constants.SECURE_KEY_HEADER))) {
                                ctx.next();
                            } else {
                                ctx.response().setStatusCode(401).end();
                            }
                        });
                    }

                    router.route(Constants.BASEMOVE_ENDPOINT).handler(BodyHandler.create());
                    router.post(Constants.BASEMOVE_ENDPOINT).handler(ctx -> parseInput(ctx, BaseMoveInput.class)
                            .map(input -> serviceStats.wrapBaseMove(input, factionLogic::nextBaseMove))
                            .subscribe(httpSuccessWithBody(ctx), httpError(ctx))
                    );

                    router.route(Constants.UNITMOVE_ENDPOINT).handler(BodyHandler.create());
                    router.post(Constants.UNITMOVE_ENDPOINT).handler(ctx -> parseInput(ctx, UnitMoveInput.class)
                            .map(input -> serviceStats.wrapUnitMove(input, factionLogic::nextUnitMove))
                            .subscribe(httpSuccessWithBody(ctx), httpError(ctx))
                    );

                    router.get(Constants.STATS_ENDPOINT).handler(ctx -> ctx.json(serviceStats));

                    // Register log endpoint
                    RemoteLogAppender.registerHttpEndpoint(router);

                    //Receive POI (Points Of Interest)
                    router.route(Constants.HINTS_POIS_ENDPOINT).handler(BodyHandler.create());
                    router.post(Constants.HINTS_POIS_ENDPOINT).handler(ctx -> parseInput(ctx, POIsHint.class)
                            .map(input -> factionLogic.registerPOIs(input))
                            .subscribe(httpSuccessWithBody(ctx), httpError(ctx))
                    );
                    //Receive Bonuscodes
                    router.route(Constants.HINTS_BONUSCODES_ENDPOINT).handler(BodyHandler.create());
                    router.post(Constants.HINTS_BONUSCODES_ENDPOINT).handler(ctx -> parseInput(ctx, BonusCode.class)
                            .map(input -> factionLogic.registerBonusCodes(input))
                            .subscribe(httpSuccessWithBody(ctx), httpError(ctx))
                    );

                    int httpPort = config.getInteger(Constants.HTTP_PORT_PROPERTY, Constants.DEFAULT_HTTP_PORT);
                    return vertx.createHttpServer()
                            .requestHandler(router)
                            .rxListen(httpPort).ignoreElement();
                });
    }

    private <T> Single<T> parseInput(RoutingContext ctx, Class<T> expectedInput) {
        try {
            T input = ctx.getBodyAsJson().mapTo(expectedInput);
            return Single.just(input);
        } catch (Throwable t) {
            return Single.error(new IllegalArgumentException("Could not parse the input object to class " + expectedInput.getSimpleName(), t));
        }
    }

    private <T> Consumer<T> httpSuccessWithBody(RoutingContext ctx) {
        return result -> ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(Json.encode(result));
    }

    private Consumer<Throwable> httpError(RoutingContext ctx) {
        return err -> {
            logger.warn("An error occurred while handling an HTTP call!", err);
            if (err instanceof IllegalArgumentException) {
                ctx.response().setStatusCode(400).end(err.getMessage());
            } else {
                ctx.response().setStatusCode(500).end("An unexpected error occurred and has been logged!");
            }
        };
    }
}

