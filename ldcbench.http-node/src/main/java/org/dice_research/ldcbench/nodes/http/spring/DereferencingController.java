package org.dice_research.ldcbench.nodes.http.spring;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dice_research.ldcbench.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{datasetId}")
public class DereferencingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DereferencingController.class);

    private Map<String, Graph> datasets;

    @RequestMapping("/{resourceId}")
    @ResponseBody
    public SimpleTripleStream resource(@PathVariable String datasetId, @PathVariable String resourceId) {
        // TODO Select dataset and resource to stream
        // TODO Get a language and the triple iterator

        return new SimpleTripleStream(Lang.TTL,
                Arrays.asList(Triple.create(
                        ResourceFactory.createResource("http://example.org/" + datasetId + "/" + resourceId).asNode(),
                        RDF.type.asNode(), RDFS.Resource.asNode())).iterator());
    }

}
