package at.tuwien.vis2.metromaps;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/test")
    public String serveWelcomePage() {
        return "index.html";
    }
}
