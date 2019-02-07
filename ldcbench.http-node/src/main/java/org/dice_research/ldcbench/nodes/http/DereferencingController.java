package org.dice_research.ldcbench.nodes.http;

import java.io.OutputStream;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.dice_research.ldcbench.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import toools.DataMapping;

@Controller
@RequestMapping("/{datasetId}")
public class DereferencingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DereferencingController.class);

    @Autowired
    private Map<String, Graph> datasets;

    @RequestMapping("/{resourceId}")
    @ResponseBody
    public StreamRDF resource(@PathVariable String datasetId, @PathVariable String resourceId) {
        // TODO Select dataset and resource to stream
        // TODO Get a language and the triple iterator
//        return new SimpleTripleStream(lang, iterator);
        return null;
    }

}
