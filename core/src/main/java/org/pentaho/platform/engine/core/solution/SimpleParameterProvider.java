/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.engine.core.solution;

import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.util.web.HttpUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleParameterProvider extends BaseParameterProvider implements IParameterProvider {

  private Map<String, Object> parameters;

  protected static final String ADDITIONAL_PARAMS = "_PENTAHO_ADDITIONAL_PARAMS_"; //$NON-NLS-1$

  public SimpleParameterProvider() {
    this( null );
  }

  @Override
  protected String getValue( final String name ) {
    Object obj = parameters.get( name );
    if ( obj instanceof Object[][] ) {
      Object[][] array2d = (Object[][]) obj;
      if ( array2d.length > 0 && array2d[0].length > 0 ) {
        // obj = array2d[array2d.length-1][array2d[array2d.length-1].length-1];
        obj = array2d[0][0];
      } else {
        // the string of this is meaningless
        obj = null;
      }
    } else if ( obj instanceof Object[] ) {
      Object[] array = (Object[]) obj;
      if ( array.length > 0 ) {
        obj = array[0];
      } else {
        // the string of this is meaningless
        obj = null;
      }
    }
    return ( obj == null ) ? null : String.valueOf( obj );
  }

  @Override
  public Object getParameter( final String name ) {
    return parameters.get( name );
  }

  // TODO sbarkdull, may want to tweak this ctor to make a copy of the map,
  // otherwise the caller will have a reference to this class's internal
  // data, destroying encapsulation/data hiding
  @SuppressWarnings( { "unchecked" } )
  public SimpleParameterProvider( Map parameters ) {
    if ( parameters == null ) {
      parameters = new HashMap<String, Object>();
    }
    this.parameters = parameters;
  }

  public void setParameter( final String name, final String value ) {
    parameters.put( name, value );
  }

  public void setParameter( final String name, final long value ) {
    parameters.put( name, new Long( value ) );
  }

  public void setParameter( final String name, final Date value ) {
    parameters.put( name, value );
  }

  public void setParameter( final String name, final Object value ) {
    parameters.put( name, value );
  }

  @SuppressWarnings( { "unchecked" } )
  public void setParameters( final Map newParameters ) {
    if ( newParameters != null ) {
      parameters.putAll( newParameters );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.newcode.IParameterProvider#getParameterNames()
   */
  public Iterator getParameterNames() {
    // TODO turn the key set iterator into an enumeration
    return parameters.keySet().iterator();
  }

  /**
   * Converts single value arrays to String parameters
   * 
   */
  public void copyAndConvertParameters( final Map paramMap ) {
    for ( Iterator it = paramMap.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry entry = (Map.Entry) it.next();
      Object value = entry.getValue();
      if ( value != null ) {
        if ( ( value instanceof Object[] ) && ( ( (Object[]) value ).length == 1 ) ) {
          setParameter( (String) entry.getKey(), String.valueOf( ( (Object[]) value )[0] ) );
        } else {
          setParameter( (String) entry.getKey(), value );
        }
      }
    }
  }

  /**
   * Looks for ADDITIONAL_PARAMS in the paramMap, if it finds a parameter by that name, it assumes that it is a
   * query string, it parses the query string, and adds the components of the query string to this class's
   * parameter map.
   * 
   * @param paramMap
   */
  public void copyAndConvertAdditionalParameters( final Map paramMap ) {
    String strAdditionalParams = (String) paramMap.get( SimpleParameterProvider.ADDITIONAL_PARAMS );
    if ( strAdditionalParams != null ) {
      int idx = strAdditionalParams.indexOf( "?" ); //$NON-NLS-1$
      if ( idx > 0 ) {
        strAdditionalParams = strAdditionalParams.substring( idx + 1 );
      }
      Map additionalParms = HttpUtil.parseQueryString( strAdditionalParams );
      copyAndConvertParameters( additionalParms );
    }
  }
}
