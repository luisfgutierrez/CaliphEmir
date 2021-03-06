/*
 * This file is part of Caliph & Emir.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://caliph-emir.sourceforge.net
 */
package at.lux.fotoretrieval.retrievalengines;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JProgressBar;

import org.jdom.Element;

import at.lux.fotoretrieval.ResultListEntry;

/**
 * Implements rtwo methods, exactly those with no JProgressBar. They just give null as parameter.
 * Date: 13.10.2004
 * Time: 20:56:05
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public abstract class AbstractRetrievalEngine implements RetrievalEngine {
	abstract public List<ResultListEntry> getSimilarImages_fromSet(Set <Element> VisualDescriptorSet, String whereToSearch, boolean recursive, JProgressBar progress);

	public List<ResultListEntry> getSimilarImages_fromSet(Set<Element> VisualDescriptor, String whereToSearch, boolean recursive) {
        return getSimilarImages_fromSet(VisualDescriptor, whereToSearch, recursive, null);
    }

	public List<ResultListEntry> getSimilarImages(Element VisualDescriptor, String whereToSearch, boolean recursive) {
        return getSimilarImages(VisualDescriptor, whereToSearch, recursive, null);
    }

    public List<ResultListEntry> getImagesByXPathSearch(String xPath, String whereToSearch, boolean recursive) {
        return getImagesByXPathSearch(xPath, whereToSearch, recursive, null);
    }

    /**
     * This method has to be able to cope with null as parameter for the JProgressBar.
     * @param xPath
     * @param objects
     * @param whereToSearch
     * @param recursive
     * @param progress can be null.
     */
    abstract public List<ResultListEntry> getImagesBySemantics(String xPath, Vector objects, String whereToSearch, boolean recursive, JProgressBar progress);

    /**
     * This method has to be able to cope with null as parameter for the JProgressBar.
     * @param VisualDescriptor
     * @param whereToSearch
     * @param recursive
     * @param progress can be null.
     */
    abstract public List<ResultListEntry> getSimilarImages(Element VisualDescriptor, String whereToSearch, boolean recursive, JProgressBar progress);

    /**
     * This method has to be able to cope with null as parameter for the JProgressBar.
     * @param xPath
     * @param whereToSearch
     * @param recursive
     * @param progress can be null.
     */
    abstract public List<ResultListEntry> getImagesByXPathSearch(String xPath, String whereToSearch, boolean recursive, JProgressBar progress);

}
