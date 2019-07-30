import annotation.WebSocketController;
import annotation.WebSocketHandler;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@WebSocketController(url = "/bank", idKey = "id")
public class BankWsHandler {


    @WebSocketHandler(idValue = "bill")
    public void hanldeBill(){
        System.out.println("billing");
    }

}
