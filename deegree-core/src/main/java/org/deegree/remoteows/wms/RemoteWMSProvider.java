//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.remoteows.wms;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wms.client.WMSClient111;
import org.deegree.remoteows.RemoteOWSProvider;
import org.deegree.remoteows.RemoteOWSStore;
import org.deegree.remoteows.wms.RemoteWMSStore.LayerOptions;
import org.deegree.remoteows.wms.jaxb.RemoteWMSStore;
import org.deegree.remoteows.wms.jaxb.RequestOptionsType;
import org.deegree.remoteows.wms.jaxb.RequestedLayerType;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RemoteWMSProvider implements RemoteOWSProvider {

    private static final Logger LOG = getLogger( RemoteWMSProvider.class );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.remoteows.wms.jaxb";

    private static final String CONFIG_SCHEMA = "/META-INF/schemas/datasource/remoteows/wms/3.1.0/remotewms.xsd";

    /**
     * 
     */
    public RemoteWMSProvider() {
        // for spi
    }

    public List<String> getCapabilitiesNamespaces() {
        return null;
    }

    public String getConfigurationNamespace() {
        return "http://www.deegree.org/datasource/remoteows/wms";
    }

    public String getServiceType() {
        return "WMS";
    }

    private static void fillLayerOptions( LayerOptions opts, RequestOptionsType ropts ) {
        if ( ropts != null ) {
            if ( ropts.getImageFormat() != null ) {
                opts.imageFormat = ropts.getImageFormat().getValue();
                opts.transparent = ropts.getImageFormat().isTransparent();
            }
            if ( ropts.getDefaultCRS() != null ) {
                opts.defaultCRS = ropts.getDefaultCRS().getValue();
                opts.alwaysUseDefaultCRS = ropts.getDefaultCRS().isUseAlways();
            }
        }
    }

    public RemoteOWSStore create( URL config ) {
        try {
            RemoteWMSStore cfg = (RemoteWMSStore) unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA, config );
            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( config.toString() );
            URL capas = resolver.resolve( cfg.getCapabilitiesDocumentLocation().getLocation() );
            WMSClient111 client = new WMSClient111( capas );
            Map<String, LayerOptions> layers = new HashMap<String, LayerOptions>();
            List<String> layerOrder = new LinkedList<String>();
            RequestOptionsType def = cfg.getDefaultRequestOptions();
            for ( RequestedLayerType rlt : cfg.getRequestedLayer() ) {
                layerOrder.add( rlt.getName() );
                LayerOptions opts = new LayerOptions();
                fillLayerOptions( opts, def );
                fillLayerOptions( opts, rlt.getRequestOptions() );
                layers.put( rlt.getName(), opts );
            }
            return new org.deegree.remoteows.wms.RemoteWMSStore( client, layers, layerOrder );
        } catch ( JAXBException e ) {
            e.printStackTrace();
            LOG.warn( "Remote WMS store config at '{}' could not be parsed: {}", config, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( ClassCastException e ) {
            e.printStackTrace();
            LOG.warn( "Remote WMS store config at '{}' could not be parsed: {}", config, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( MalformedURLException e ) {
            e.printStackTrace();
            LOG.warn( "Remote WMS store config at '{}' could not be parsed: {}", config, e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return null;
    }

}