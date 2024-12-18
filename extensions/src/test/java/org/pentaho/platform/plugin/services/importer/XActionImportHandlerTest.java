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


package org.pentaho.platform.plugin.services.importer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.util.Assert;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.util.XmlTestConstants;
import org.xml.sax.SAXException;

/**
 * This unit test focuses on validating XActionImportHandler logic, which, according to the class' javadoc, quote:
 * <p />
 * "This import handler was created with the sole purpose to honor the hidden flag for action-sequences which is
 * controlled by the '/action-sequence/documentation/result-type' inside of the xaction definition."
 */
public class XActionImportHandlerTest {

  private final String XACTION_HIDDEN_FLAG_TRUE = "/solution/test/platform/HelloWorldHiddenFlag.xaction"; //$NON-NLS-1$
  private final String XACTION_HIDDEN_FLAG_FALSE = "/solution/test/platform/HelloWorld.xaction"; //$NON-NLS-1$

  IMimeType mimeType;
  XActionImportHandler handler;
  IPlatformImportBundle bundle;

  @Before
  public void setUp() throws Exception {

    mimeType = mock( IMimeType.class );
    when( mimeType.getName() ).thenReturn( "mock-mimeType" );
    handler = new XActionImportHandlerForTesting( Arrays.asList( mimeType ) );
    bundle = new RepositoryFileImportBundle();
  }

  @Test
  public void testImportXactionWithHiddenFlagSetToFalse() throws Exception {

    ( (RepositoryFileImportBundle) bundle ).setInputStream( getXactionAsInputStream( XACTION_HIDDEN_FLAG_FALSE ) );
    handler.importFile( bundle );

    Assert.assertNotNull( ( (XActionImportHandlerForTesting) handler ).getResultingImportBundle() );
  }

  @Test
  public void testImportXactionWithHiddenFlagSetToTrue() throws Exception {

    ( (RepositoryFileImportBundle) bundle ).setInputStream( getXactionAsInputStream( XACTION_HIDDEN_FLAG_TRUE ) );
    handler.importFile( bundle );

    Assert.assertNotNull( ( (XActionImportHandlerForTesting) handler ).getResultingImportBundle() );
    Assert.assertTrue( ( (XActionImportHandlerForTesting) handler ).getResultingImportBundle().isHidden() );
  }

  @Test( timeout = 2000, expected = SAXException.class )
  public void shouldNotFailAndReturnNullWhenMaliciousXmlIsGiven() throws IOException, ParserConfigurationException, SAXException {
    handler.getImportBundleDocument( new StringBufferInputStream( XmlTestConstants.MALICIOUS_XML ) );
    fail();
  }

  @Test
  public void shouldNotFailAndReturnNotNullWhenLegalXmlIsGiven() throws Exception {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
      + "<slave_config>"
      + "</slave_config>";

    assertNotNull( handler.getImportBundleDocument( new StringBufferInputStream( xml ) ) );
  }

  private InputStream getXactionAsInputStream( String xactionTestFilePath ) {
    return getClass().getResourceAsStream( xactionTestFilePath );
  }

  private class XActionImportHandlerForTesting extends XActionImportHandler {

    public RepositoryFileImportBundle resultingImportBundle;

    public XActionImportHandlerForTesting( List<IMimeType> mimeTypes ) {
      super( mimeTypes );
    }

    @Override
    protected void importBundle( RepositoryFileImportBundle importBundle ) throws PlatformImportException {
      this.resultingImportBundle = importBundle;
    }

    public RepositoryFileImportBundle getResultingImportBundle() {
      return resultingImportBundle;
    }
  }

  @After
  public void destroy() {
    mimeType = null;
    handler = null;
    bundle = null;
  }
}
