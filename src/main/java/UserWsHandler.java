import annotation.WebSocketController;
import annotation.WebSocketHandler;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@WebSocketController(url = "/user", idKey = "id")
public class UserWsHandler extends AbstractWebSocketHandler {

    @WebSocketHandler(idValue = "login")
    public void handleLogin(){
        System.out.println("lol login");
    }

}
