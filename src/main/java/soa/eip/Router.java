package soa.eip;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Router extends RouteBuilder {

  public static final String DIRECT_URI = "direct:twitter";

  @Override
  public void configure() {

    Processor processor = new Processor() {
      @Override
      public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        // Comprobamos si existe el comando limitador de mensajes
        if (body.matches("^.+ max:[0-9]+$")) {
          String[] aux = body.split(":");
          int count = Integer.parseInt(aux[aux.length - 1]);
          body = body.replace("max:" + count, "");
          // Se añade al final el limitador de cantidad
          exchange.getIn().setBody(body + "?count=" + count);
        }
      }
    };

    from(DIRECT_URI)
            .log("Body contains \"${body}\"")
            .process(processor)
            .log("Searching twitter for \"${body}\"!")
            .toD("twitter-search:${body}")
            .log("Body now contains the response from twitter:\n${body}");
  }
}
