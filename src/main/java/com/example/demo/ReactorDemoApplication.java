package com.example.demo;

import javafx.application.Application;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @author IMI-Ron
 */
@RequiredArgsConstructor
@SpringBootApplication
public class ReactorDemoApplication  {

    private final Logger LOG = LoggerFactory.getLogger("Application");

//    private final EventBus eventBus;
//    private final EventHandler eventHandler;

//    @Autowired
//    public Application(EventBus eventBus, EventHandler eventHandler) {
//        this.eventBus = eventBus;
//        this.eventHandler = eventHandler;
//    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//    @Override
//    public void run(String... strings) throws Exception {
//        eventBus.on($("eventHandler"), eventHandler);
//
//        //Publish messages here
//        for (int i = 0; i < 10; i++) {
//            Shipment shipment = new Shipment();
//            shipment.setShipmentId(String.valueOf(i));
//            eventBus.notify("eventHandler", Event.wrap(shipment));
//            LOG.info("Published shipment number {}.", i);
//        }
//    }

}
