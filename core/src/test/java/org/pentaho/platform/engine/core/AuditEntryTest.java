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


package org.pentaho.platform.engine.core;

import java.math.BigDecimal;

import junit.framework.TestCase;

import org.pentaho.platform.api.engine.IAuditEntry;
import org.pentaho.platform.engine.core.audit.AuditEntry;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.audit.NullAuditEntry;
import org.pentaho.platform.engine.core.output.SimpleContentItem;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;

@SuppressWarnings( { "all" } )
public class AuditEntryTest extends TestCase {

  public void testAuditEntry() throws Exception {

    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    PentahoSystem.registerObjectFactory( factory );

    String jobId = "testjobid";
    String instId = "testinstid";
    String objId = "testobjid";
    String objType = "testobjtype";
    String actor = "testactor";
    String messageType = "testtype";
    String messageName = "testname";
    String messageTxtValue = MessageTypes.INSTANCE_END;
    BigDecimal messageNumValue = new BigDecimal( 99 );
    float duration = (float) 1.23;

    // this should not complain
    AuditEntry.auditJobDuration( jobId, instId, objId, objType, actor, messageType, messageName, messageTxtValue,
        duration );

    TestOutputHandler.contentItem = new SimpleContentItem();
    factory.defineObject( IAuditEntry.class.getSimpleName(), TestAuditEntry.class.getName(),
        StandaloneObjectFactory.Scope.GLOBAL );

    // this should not complain
    AuditEntry.auditJobDuration( jobId, instId, objId, objType, actor, messageType, messageName, messageTxtValue,
        duration );

    TestAuditEntry entry = (TestAuditEntry) factory.get( IAuditEntry.class, null );
    assertEquals( jobId, entry.jobId );
    assertEquals( "1.23", Double.toString( entry.duration ).substring( 0, 4 ) );
    assertEquals( null, entry.messageNumValue );
    assertEquals( messageType, entry.messageType );
    assertEquals( messageName, entry.messageName );
    assertEquals( messageTxtValue, entry.messageTxtValue );

    AuditEntry.auditJobNumValue( jobId, instId, objId, objType, actor, messageType, messageName, messageNumValue );
    assertEquals( "0", Double.toString( entry.duration ).substring( 0, 1 ) );
    assertEquals( messageNumValue, entry.messageNumValue );
    assertEquals( null, entry.messageTxtValue );

    AuditEntry.auditJobTxtValue( jobId, instId, objId, objType, actor, messageType, messageName, messageTxtValue );
    assertEquals( "0", Double.toString( entry.duration ).substring( 0, 1 ) );
    assertEquals( null, entry.messageNumValue );
    assertEquals( messageTxtValue, entry.messageTxtValue );

    new MessageTypes();
    new AuditHelper();
    new AuditEntry();
  }

  public void testNullAuditEntry() {
    IAuditEntry auditEntry = new NullAuditEntry();
    // this should not fail, even with all nulls as inputs
    auditEntry.auditAll( null, null, null, null, null, null, null, null, null, 0.0 );
  }

  public void testClearCounts() {
    AuditEntry auditEntry = new AuditEntry();
    long counterResetDateTime = auditEntry.getCounterResetDateTime().getTime();
    auditEntry.auditJobTxtValue( null, null, null, null, null, null, null, "messageTxtValue" );
    assertTrue( !auditEntry.getCounts().isEmpty() );
    auditEntry.clearCounts();
    assertTrue( auditEntry.getCounts().isEmpty() );
    assertTrue( counterResetDateTime < auditEntry.getCounterResetDateTime().getTime() );
  }
}
