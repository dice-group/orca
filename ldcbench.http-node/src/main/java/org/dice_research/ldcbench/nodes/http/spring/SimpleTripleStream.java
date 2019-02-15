package org.dice_research.ldcbench.nodes.http.spring;

import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;

public class SimpleTripleStream {

    protected Lang lang;
    protected Iterator<Triple> iterator;
    
    public SimpleTripleStream(Lang lang, Iterator<Triple> iterator) {
        super();
        this.lang = lang;
        this.iterator = iterator;
    }

    /**
     * @return the lang
     */
    public Lang getLang() {
        return lang;
    }

    /**
     * @param lang the lang to set
     */
    public void setLang(Lang lang) {
        this.lang = lang;
    }

    /**
     * @return the iterator
     */
    public Iterator<Triple> getIterator() {
        return iterator;
    }

    /**
     * @param iterator the iterator to set
     */
    public void setIterator(Iterator<Triple> iterator) {
        this.iterator = iterator;
    }
}
