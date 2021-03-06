package org.infinispan.loaders.remote.logging;

import org.infinispan.commons.CacheConfigurationException;
import org.infinispan.loaders.CacheLoaderException;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

import static org.jboss.logging.Logger.Level.*;

/**
 * Log abstraction for the remote cache store. For this module, message ids
 * ranging from 10001 to 11000 inclusively have been reserved.
 *
 * @author Galder Zamarreño
 * @since 5.0
 */
@MessageLogger(projectCode = "ISPN")
public interface Log extends org.infinispan.util.logging.Log {

   @LogMessage(level = ERROR)
   @Message(value = "RemoteCacheStore can only run in shared mode! This method shouldn't be called in shared mode", id = 10001)
   void sharedModeOnlyAllowed();

   @Message(value = "Wrapper cannot handle values of class %s", id = 10004)
   CacheLoaderException unsupportedValueFormat(String name);

   @Message(value = "Cannot enable HotRod wrapping if a marshaller and/or an entryWrapper have already been set", id = 10005)
   CacheConfigurationException cannotEnableHotRodWrapping();

   @Message(value = "Cannot load the HotRodEntryWrapper class (make sure the infinispan-server-hotrod classes are available)", id = 10006)
   CacheConfigurationException cannotLoadHotRodEntryWrapper(@Cause Exception e);
}
