package org.boris;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BorisApplication {
    public static void main(String[] args) {
        SpringApplication.run(BorisApplication.class, args);
    }

    /* TODO :
        testy,
        jeżeli z api nie przyjdzie kraj powinniśmy go rozpoznać po współrzędnych,
        dev profile,
        czas z api nie zgadza się z czasem postgresa,
        podział na moduły jak już wszystko będzie działać,
     */

}
