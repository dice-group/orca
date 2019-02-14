package org.dice_research.ldcbench.nodes.http.spring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RobotsController {

    private String robotsContent;

    @RequestMapping("/robots")
    public String config() {
        return robotsContent;
    }

}
