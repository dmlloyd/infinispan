package org.infinispan.eviction;

import org.infinispan.commons.util.Util;
import org.infinispan.commons.CacheConfigurationException;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.context.impl.ImmutableContext;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.factories.annotations.Start;
import org.infinispan.factories.annotations.Stop;
import org.infinispan.loaders.CacheLoaderException;
import org.infinispan.loaders.CacheLoaderManager;
import org.infinispan.loaders.CacheStore;
import org.infinispan.notifications.cachelistener.CacheNotifier;
import org.infinispan.util.TimeService;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class PassivationManagerImpl implements PassivationManager {

   CacheLoaderManager cacheLoaderManager;
   CacheNotifier notifier;
   CacheStore cacheStore;
   Configuration cfg;

   boolean statsEnabled = false;
   boolean enabled = false;
   private static final Log log = LogFactory.getLog(PassivationManagerImpl.class);
   private final AtomicLong passivations = new AtomicLong(0);
   private DataContainer container;
   private TimeService timeService;
   private static final boolean trace = log.isTraceEnabled();

   @Inject
   public void inject(CacheLoaderManager cacheLoaderManager, CacheNotifier notifier, Configuration cfg, DataContainer container,
                      TimeService timeService) {
      this.cacheLoaderManager = cacheLoaderManager;
      this.notifier = notifier;
      this.cfg = cfg;
      this.container = container;
      this.timeService = timeService;
   }

   @Start(priority = 11)
   public void start() {
      enabled = cacheLoaderManager.isUsingPassivation();
      if (enabled) {
         cacheStore = cacheLoaderManager == null ? null : cacheLoaderManager.getCacheStore();
         if (cacheStore == null) {
            throw new CacheConfigurationException("passivation can only be used with a CacheLoader that implements CacheStore!");
         }

         enabled = cacheLoaderManager.isEnabled() && cacheLoaderManager.isUsingPassivation();
         statsEnabled = cfg.jmxStatistics().enabled();
      }
   }

   @Override
   public boolean isEnabled() {
      return enabled;
   }

   @Override
   public void passivate(InternalCacheEntry entry) {
      if (enabled && entry != null) {
         Object key = entry.getKey();
         // notify listeners that this entry is about to be passivated
         notifier.notifyCacheEntryPassivated(key, entry.getValue(), true,
               ImmutableContext.INSTANCE, null);
         if (trace) log.tracef("Passivating entry %s", key);
         try {
            cacheStore.store(entry);
            if (statsEnabled) passivations.getAndIncrement();
         } catch (CacheLoaderException e) {
            log.unableToPassivateEntry(key, e);
         }
         notifier.notifyCacheEntryPassivated(key, null, false,
               ImmutableContext.INSTANCE, null);
      }
   }

   @Override
   @Stop(priority = 9)
   public void passivateAll() throws CacheLoaderException {
      if (enabled) {
         long start = timeService.time();
         log.passivatingAllEntries();
         for (InternalCacheEntry e : container) {
            if (trace) log.tracef("Passivating %s", e.getKey());
            cacheStore.store(e);
         }
         log.passivatedEntries(container.size(),
                               Util.prettyPrintTime(timeService.timeDuration(start, TimeUnit.MILLISECONDS)));
      }
   }

   @Override
   public long getPassivationCount() {
      return passivations.get();
   }

   @Override
   public void resetPassivationCount() {
      passivations.set(0L);
   }
}
