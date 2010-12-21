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

import static java.util.Collections.singletonList;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.OUTER;
import static org.deegree.coverage.raster.interpolation.InterpolationType.BILINEAR;
import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataFromImage;
import static org.deegree.coverage.raster.utils.RasterFactory.rasterDataToImage;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.deegree.commons.utils.Pair;
import org.deegree.coverage.raster.RasterTransformer;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wms.Utils;
import org.deegree.protocol.wms.client.WMSClient111;
import org.deegree.remoteows.RemoteOWSStore;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RemoteWMSStore implements RemoteOWSStore {

    private static final Logger LOG = getLogger( RemoteWMSStore.class );

    private WMSClient111 client;

    private final Map<String, LayerOptions> layers;

    private List<String> layerOrder;

    private TreeSet<String> commonSRS;

    private boolean homogenousGroup = true, alwaysUseDefaultCRS = false;

    private String imageFormat, requestCRS;

    private boolean transparent;

    /**
     * @param client
     * @param layers
     */
    public RemoteWMSStore( WMSClient111 client, Map<String, LayerOptions> layers, List<String> layerOrder ) {
        this.client = client;
        this.layers = layers;
        this.layerOrder = layerOrder;

        Boolean transparent = null;
        String format = null;
        String crs = null;
        String defCrs = null;
        for ( LayerOptions opts : layers.values() ) {
            if ( transparent == null ) {
                transparent = opts.transparent;
            } else {
                if ( transparent != opts.transparent ) {
                    homogenousGroup = false;
                    LOG.debug( "Layer group can not be requested in a single request as transparency settings are not homogenous." );
                    break;
                }
            }
            if ( opts.imageFormat != null ) {
                if ( format == null ) {
                    format = opts.imageFormat;
                } else {
                    if ( !opts.imageFormat.equals( format ) ) {
                        homogenousGroup = false;
                        LOG.debug( "Layer group can not be requested in a single request as image format settings are not homogenous." );
                        break;
                    }
                }
            }
            if ( opts.defaultCRS != null ) {
                if ( defCrs == null ) {
                    defCrs = opts.defaultCRS;
                }
            }
            if ( opts.alwaysUseDefaultCRS && opts.defaultCRS != null ) {
                if ( crs == null ) {
                    crs = opts.defaultCRS;
                } else {
                    if ( !crs.equals( opts.defaultCRS ) ) {
                        homogenousGroup = false;
                        LOG.debug( "Layer group can not be requested in a single request as crs settings are not homogenous." );
                        break;
                    }
                }
            }
        }
        commonSRS = null;
        for ( String l : layers.keySet() ) {
            if ( commonSRS == null ) {
                commonSRS = new TreeSet<String>( client.getCoordinateSystems( l ) );
            } else {
                commonSRS.retainAll( client.getCoordinateSystems( l ) );
            }
        }
        LOG.debug( "Requestable srs common to all cascaded layers: " + commonSRS );

        if ( homogenousGroup ) {
            imageFormat = format == null ? "image/png" : format;
            requestCRS = crs;
            if ( requestCRS == null ) {
                requestCRS = defCrs == null ? commonSRS.first() : defCrs;
            } else {
                alwaysUseDefaultCRS = true;
            }
            this.transparent = transparent;
        }
    }

    /**
     * @return the pre-configured client
     */
    public WMSClient111 getClient() {
        return client;
    }

    /**
     * @return the list of configured layers
     */
    public List<String> getLayers() {
        return null;
    }

    public List<BufferedImage> getMap( final Envelope envelope, final int width, final int height ) {
        if ( homogenousGroup ) {
            CRS origCrs = envelope.getCoordinateSystem();
            String origCrsName = origCrs.getName();
            try {

                if ( ( !alwaysUseDefaultCRS && commonSRS.contains( origCrsName ) ) || origCrsName.equals( requestCRS ) ) {
                    LOG.trace( "Will request remote layer(s) in " + origCrsName );
                    LinkedList<String> errors = new LinkedList<String>();
                    Pair<BufferedImage, String> pair = client.getMap( new LinkedList<String>( layers.keySet() ), width,
                                                                      height, envelope, origCrs, imageFormat,
                                                                      transparent, false, -1, true, errors );
                    LOG.debug( "Parameters that have been replaced for this request: " + errors );
                    if ( pair.first == null ) {
                        LOG.debug( "Error from remote WMS: " + pair.second );
                    }
                    return singletonList( pair.first );
                }

                // case: transform the bbox and image
                LOG.trace( "Will request remote layer(s) in {} and transform to {}", requestCRS, origCrsName );

                GeometryTransformer trans = new GeometryTransformer( requestCRS );
                Envelope bbox = trans.transform( envelope, origCrs.getWrappedCRS() );

                RasterTransformer rtrans = new RasterTransformer( origCrs.getWrappedCRS() );

                double scale = Utils.calcScaleWMS111( width, height, envelope,
                                                      envelope.getCoordinateSystem().getWrappedCRS() );
                double newScale = Utils.calcScaleWMS111( width, height, bbox, new CRS( requestCRS ).getWrappedCRS() );
                double ratio = scale / newScale;

                int newWidth = round( ratio * width );
                int newHeight = round( ratio * height );

                LinkedList<String> errors = new LinkedList<String>();
                Pair<BufferedImage, String> pair = client.getMap( layerOrder, newWidth, newHeight, bbox,
                                                                  new CRS( requestCRS ), imageFormat, transparent,
                                                                  false, -1, true, errors );

                LOG.debug( "Parameters that have been replaced for this request: {}", errors );
                if ( pair.first == null ) {
                    LOG.debug( "Error from remote WMS: {}", pair.second );
                    return null;
                }

                RasterGeoReference env = RasterGeoReference.create( OUTER, bbox, newWidth, newHeight );
                RasterData data = rasterDataFromImage( pair.first );
                SimpleRaster raster = new SimpleRaster( data, bbox, env );

                SimpleRaster transformed = rtrans.transform( raster, envelope, width, height, BILINEAR ).getAsSimpleRaster();

                return Collections.singletonList( rasterDataToImage( transformed.getRasterData() ) );

            } catch ( IOException e ) {
                LOG.info( "Error when loading image from remote WMS: {}", e.getLocalizedMessage() );
                LOG.trace( "Stack trace", e );
            } catch ( UnknownCRSException e ) {
                LOG.warn( "Unable to find crs, this is not supposed to happen." );
                LOG.trace( "Stack trace", e );
            } catch ( TransformationException e ) {
                LOG.warn( "Unable to transform bbox from {} to {}", origCrsName, requestCRS );
                LOG.trace( "Stack trace", e );
            }
        } else {
            LOG.warn( "Sophisticated per layer settings are not supported yet." );
        }

        return null;
    }

    public static class LayerOptions {
        public boolean transparent = true, alwaysUseDefaultCRS = false;

        public String imageFormat = "image/png", defaultCRS = "EPSG:4326";
    }

}