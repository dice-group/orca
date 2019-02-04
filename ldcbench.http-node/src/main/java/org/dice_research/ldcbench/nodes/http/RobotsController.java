package org.dice_research.ldcbench.nodes.http;

import java.io.OutputStream;
import java.util.Map;

import org.dice_research.ldcbench.graph.Graph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RobotsController {

    @Autowired
    @Qualifier("robotsContent")
    private String robotsContent;

    @RequestMapping("/robots")
    public String config() {
        return robotsContent;
    }

}
