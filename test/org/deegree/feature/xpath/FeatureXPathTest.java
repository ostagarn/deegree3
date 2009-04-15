//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.feature.xpath;

import java.net.URL;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Property;
import org.deegree.feature.gml.GMLFeatureParser;
import org.deegree.feature.gml.GMLFeatureParserTest;
import org.deegree.feature.gml.GMLIdContext;
import org.deegree.feature.gml.schema.GMLApplicationSchemaXSDAdapter;
import org.deegree.feature.gml.schema.GMLVersion;
import org.deegree.feature.types.ApplicationSchema;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class FeatureXPathTest {

    private static final String BASE_DIR = "../gml/testdata/features/";

    private FeatureCollection fc;

    private SimpleNamespaceContext nsContext;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {

        String schemaURL = this.getClass().getResource( "../gml/schema/Philosopher_typesafe.xsd" ).toString();
        GMLApplicationSchemaXSDAdapter xsdAdapter = new GMLApplicationSchemaXSDAdapter( schemaURL,
                                                                                        GMLVersion.VERSION_31 );
        ApplicationSchema schema = xsdAdapter.extractFeatureTypeSchema();
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureParser gmlAdapter = new GMLFeatureParser( schema, idContext );

        URL docURL = GMLFeatureParserTest.class.getResource( BASE_DIR + "Philosopher_FeatureCollection.xml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        fc = (FeatureCollection) gmlAdapter.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ),
                                                          null);
        idContext.resolveXLinks( schema );

        for ( Feature member : fc ) {
            System.out.println( member.getId() );
        }

        nsContext = new SimpleNamespaceContext();
        nsContext.addNamespace( "gml", "http://www.opengis.net/gml" );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath1()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "*" );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertNotNull( selectedNodes );
        Assert.assertEquals( 7, selectedNodes.size() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath2()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "featureMember" );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 7, selectedNodes.size() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath3()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember/app:Philosopher" );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 7, selectedNodes.size() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath4()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember[1]/app:Philosopher" );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        FeatureNode featureNode = (FeatureNode) selectedNodes.get( 0 );
        Feature feature = featureNode.getFeature();
        Assert.assertEquals( "PHILOSOPHER_1", feature.getId() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath5()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember[1]/app:Philosopher/app:name" );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        PropertyNode propNode = (PropertyNode) selectedNodes.get( 0 );
        Property prop = propNode.getProperty();
        Assert.assertEquals( "Karl Marx", prop.getValue() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath6()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember[1]/app:Philosopher/app:name/text()" );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        TextNode textNode = (TextNode) selectedNodes.get( 0 );
        Assert.assertEquals( "Karl Marx", textNode.getValue() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath7()
                            throws JaxenException {
        XPath xpath = new FeatureXPath(
                                        "gml:featureMember/app:Philosopher[app:name='Albert Camus' and app:placeOfBirth/*/app:name='Mondovi']/app:placeOfBirth/app:Place/app:name" );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        PropertyNode propNode = (PropertyNode) selectedNodes.get( 0 );
        Property prop = propNode.getProperty();
        System.out.println( prop.getValue() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath8()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember[1]/app:Philosopher/app:placeOfBirth/app:Place" );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        FeatureNode featureNode = (FeatureNode) selectedNodes.get( 0 );
        Feature feature = featureNode.getFeature();
        Assert.assertEquals( "PLACE_2", feature.getId() );

        xpath = new FeatureXPath( "../.." );
        xpath.setNamespaceContext( nsContext );
        selectedNodes = xpath.selectNodes( featureNode );
        Assert.assertEquals( 1, selectedNodes.size() );
        featureNode = (FeatureNode) selectedNodes.get( 0 );
        feature = featureNode.getFeature();
        Assert.assertEquals( "PHILOSOPHER_1", feature.getId() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath9()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember/app:Philosopher[app:id < 3]/app:name" );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        for ( Node node : selectedNodes ) {
            System.out.println( ( (PropertyNode) node ).getProperty().getValue() );
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath10()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember/app:Philosopher/app:friend/app:Philosopher//app:name" );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        for ( Node node : selectedNodes ) {
            System.out.println( ( (PropertyNode) node ).getProperty().getValue() );
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXPath11()
                            throws JaxenException {
        XPath xpath = new FeatureXPath( "gml:featureMember/app:Philosopher[@gml:id='PHILOSOPHER_1']" );
        xpath.setNamespaceContext( nsContext );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, fc ) );
        Assert.assertEquals( 1, selectedNodes.size() );
        FeatureNode featureNode = (FeatureNode) selectedNodes.get( 0 );
        Feature feature = featureNode.getFeature();
        Assert.assertEquals( "PHILOSOPHER_1", feature.getId() );
    }
}
