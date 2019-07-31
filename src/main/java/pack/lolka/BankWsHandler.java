package pack.lolka;

import processing.annotation.WebSocketController;
import processing.annotation.WebSocketHandler;

@WebSocketController(url = "/bank", idKey = "id")
public class BankWsHandler {


    @WebSocketHandler(idValue = "bill")
    public void hanldeBill(){
        System.out.println("billing");
    }

}
