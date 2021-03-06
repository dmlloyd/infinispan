package org.infinispan.loaders.jdbc.configuration;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.loaders.jdbc.AbstractJdbcCacheStoreConfig;
import org.infinispan.loaders.jdbc.connectionfactory.ConnectionFactory;
import org.infinispan.loaders.jdbc.connectionfactory.SimpleConnectionFactory;

/**
 * SimpleConnectionFactoryConfiguration.
 *
 * @author Tristan Tarrant
 * @since 5.2
 */
@BuiltBy(SimpleConnectionFactoryConfigurationBuilder.class)
public class SimpleConnectionFactoryConfiguration implements ConnectionFactoryConfiguration, LegacyConnectionFactoryAdaptor {
   private final String connectionUrl;
   private final String driverClass;
   private final String username;
   private final String password;

   SimpleConnectionFactoryConfiguration(String connectionUrl, String driverClass, String username, String password) {
      this.connectionUrl = connectionUrl;
      this.driverClass = driverClass;
      this.username = username;
      this.password = password;
   }

   public String connectionUrl() {
      return connectionUrl;
   }

   public String driverClass() {
      return driverClass;
   }

   public String username() {
      return username;
   }

   public String password() {
      return password;
   }

   @Override
   public Class<? extends ConnectionFactory> connectionFactoryClass() {
      return SimpleConnectionFactory.class;
   }

   @Override
   public void adapt(AbstractJdbcCacheStoreConfig config) {
      config.setConnectionFactoryClass(connectionFactoryClass().getName());
      config.setConnectionUrl(connectionUrl);
      config.setDriverClass(driverClass);
      config.setUserName(username);
      config.setPassword(password);
   }

   @Override
   public String toString() {
      return "SimpleConnectionFactoryConfiguration [connectionUrl=" + connectionUrl + ", driverClass=" + driverClass + ", username=" + username + ", password=" + password + "]";
   }
}
