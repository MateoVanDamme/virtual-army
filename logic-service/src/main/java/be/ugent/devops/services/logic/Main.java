package be.ugent.devops.services.logic;

import be.ugent.devops.services.logic.utils.HttpBinding;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.DatabindCodec;

public class Main {

    public static void main(String[] args) {
        DatabindCodec.prettyMapper().registerModule(new Jdk8Module());
        DatabindCodec.mapper().registerModule(new Jdk8Module());

        var vertx = Vertx.vertx();

        vertx.deployVerticle(new HttpBinding());
    }

}
