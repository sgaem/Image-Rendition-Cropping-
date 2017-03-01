package com.aem.sgaem.project.services;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.Collections;

@Component(label = "SGAEM - Resource Resolver Utility Service", enabled = true, immediate = true)
@Service(ResourceResolverUtil.class)
public class ResourceResolverUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceResolverUtil.class);

  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  public ResourceResolver getResourceResolver() {

    LOGGER.debug("START OF getResourceResolver METHOD");
    ResourceResolver resourceResolver = null;
    try {
      resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
    } catch (LoginException le) {
      LOGGER.error("Login Exception" + le.getMessage());
    }
    LOGGER.debug("END OF getResourceResolver METHOD");
    return resourceResolver;
  }

  public void closeResourceResolver(ResourceResolver resourceResolver) {

    LOGGER.debug("START OF closeResourceResolver METHOD");
    if (resourceResolver != null) {
      resourceResolver.close();
    }
    LOGGER.debug("END OF closeResourceResolver METHOD");
  }

  /* This method is used to get resource resolver object from Workflow session object. */
  public ResourceResolver getResourceResolver(Session session) throws LoginException {
    return resourceResolverFactory.getResourceResolver(Collections.<String, Object>singletonMap(
        JcrResourceConstants.AUTHENTICATION_INFO_SESSION, session));
  }
}
