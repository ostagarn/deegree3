//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.client.mdeditor.controller;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.List;

import javax.faces.component.html.HtmlCommandButton;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.deegree.client.mdeditor.FormElementManager;
import org.deegree.client.mdeditor.config.Configuration;
import org.deegree.client.mdeditor.gui.FormFieldBean;
import org.deegree.client.mdeditor.model.FormField;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormGroupWriter extends FormElementWriter {

    private static final Logger LOG = getLogger( FormGroupWriter.class );

    public static void writeFormGroup( AjaxBehaviorEvent arg0 ) {
        HtmlCommandButton comp = (HtmlCommandButton) arg0.getComponent();
        LOG.debug( "Write FormGroup with id " + comp.getId() );

        FacesContext fc = FacesContext.getCurrentInstance();
        fc.getELContext();
        FormFieldBean formFieldBean = (FormFieldBean) fc.getApplication().getELResolver().getValue( fc.getELContext(),
                                                                                                    null,
                                                                                                    "formFieldBean" );
        String fgId = comp.getId();
        List<String> subFgIds = FormElementManager.getSubFormGroupIds( fgId );
        subFgIds.add( fgId );
        List<FormField> ff = formFieldBean.getElements( subFgIds );

        String dirUrl = Configuration.getFilesDirURL();
        File dir = new File( dirUrl, fgId );
        if ( !dir.exists() ) {
            dir.mkdir();
        }
        String instance = Utils.getInstanceId();
        File file = new File( dir, instance + ".xml" );

        writeElements( ff, file );

    }
}
