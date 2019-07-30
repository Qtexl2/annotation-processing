import annotation.WebSocketController;
import annotation.WebSocketHandler;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.List;

@WebSocketController(url = "/user", idKey = "id")
public class UserWsHandler{

    private String gleb;

    UserWsHandler(String lol, int m, Object ivan, List<Object> objects){
        gleb = lol;
    }

    @WebSocketHandler(idValue = "login")
    public void handleLogin(){
        System.out.println("lol login");
    }

}
