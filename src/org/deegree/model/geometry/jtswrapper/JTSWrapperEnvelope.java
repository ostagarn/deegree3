//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
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

package org.deegree.model.geometry.jtswrapper;

import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.geometry.primitive.Envelope;
import org.deegree.model.geometry.primitive.Point;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

/**
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 * 
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
class JTSWrapperEnvelope extends JTSWrapperGeometry implements Envelope {

    private Point min;

    private Point max;

    /**
     * 
     * @param precision
     * @param crs
     * @param coordinateDimension
     * @param min
     * @param max
     */
    JTSWrapperEnvelope( double precision, CoordinateSystem crs, int coordinateDimension, Point min, Point max ) {
        super( precision, crs, coordinateDimension );
        this.min = min;
        this.max = max;

        // create polygon representation of the envelope to use JTS topological
        // methods; a JTS envelope always has dimension == 2
        Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate( min.getX(), min.getY() );
        coords[1] = new Coordinate( min.getX(), max.getY() );
        coords[2] = new Coordinate( max.getX(), max.getY() );
        coords[3] = new Coordinate( max.getX(), min.getY() );
        coords[4] = new Coordinate( min.getX(), min.getY() );
        CoordinateSequenceFactory fac = CoordinateArraySequenceFactory.instance();
        CoordinateSequence cs = fac.create( coords );
        LinearRing lr = new LinearRing( cs, jtsFactory );
        geometry = jtsFactory.createPolygon( lr, new LinearRing[0] );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Envelope#getMax()
     */
    public Point getMax() {
        return max;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Envelope#getMin()
     */
    public Point getMin() {
        return min;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.geometry.primitive.Envelope#merger(org.deegree.model.geometry.primitive.Envelope)
     */
    public Envelope merger( Envelope other ) {
        double[] min = new double[coordinateDimension];
        double[] max = new double[coordinateDimension];
        for ( int i = 0; i < coordinateDimension; i++ ) {
            if ( this.min.getAsArray()[i] < other.getMin().getAsArray()[i] ) {
                min[i] = this.min.getAsArray()[i];
            } else {
                min[i] = other.getMin().getAsArray()[i];
            }
            if ( this.max.getAsArray()[i] > other.getMax().getAsArray()[i] ) {
                max[i] = this.max.getAsArray()[i];
            } else {
                max[i] = other.getMax().getAsArray()[i];
            }
        }
        Point newMin = new JTSWrapperPoint( getPrecision(), getCoordinateSystem(), min );
        Point newMax = new JTSWrapperPoint( getPrecision(), getCoordinateSystem(), max );

        return new JTSWrapperEnvelope( getPrecision(), getCoordinateSystem(), coordinateDimension, newMin, newMax );
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.primitive.Envelope#getHeight()
     */
    public double getHeight() {
        return max.getY() - min.getY();
    }

    /* (non-Javadoc)
     * @see org.deegree.model.geometry.primitive.Envelope#getWidth()
     */
    public double getWidth() {
        return max.getX() - min.getX();
    }
    
    

}
