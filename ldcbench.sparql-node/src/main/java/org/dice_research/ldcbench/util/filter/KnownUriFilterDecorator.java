package org.dice_research.ldcbench.util.filter;


import java.io.Closeable;

public interface KnownUriFilterDecorator extends KnownUriFilter, Closeable {

    public KnownUriFilter getDecorated();
}
