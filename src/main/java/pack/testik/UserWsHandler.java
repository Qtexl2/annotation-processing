package pack.testik;

import processing.annotation.WebSocketController;
import processing.annotation.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@WebSocketController(url = "/user", idKey = "id")
public class UserWsHandler {

    private String gleb;

    public UserWsHandler(String lol, int m, Object ivan, List<Object> objects) {
        gleb = lol;
    }

    @WebSocketHandler(idValue = "login")
    public void handleLogin(WebSocketSession ws, DToshka dto) {
        System.out.println("lol login");
    }


    @WebSocketHandler(idValue = "zacini")
    public void handleZacini() {
        System.out.println("lol login");
    }
}
