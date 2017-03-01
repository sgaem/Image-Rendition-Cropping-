package com.aem.sgaem.project.process;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.aem.sgaem.project.services.ResourceResolverUtil;
import com.aem.sgaem.project.utils.StringUtil;
import com.day.cq.commons.ImageHelper;
import com.day.image.Layer;
import java.awt.Rectangle;
import java.util.List;
import javax.jcr.Node;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(immediate = true, enabled = true, metatype = true)
@Service(value = WorkflowProcess.class)
@Property(name = "process.label", value = "Cropping Image Rendition Process", propertyPrivate = true)
public class ImageRenditionProcess implements WorkflowProcess {

  private static final String PROCESS_ARGS = "PROCESS_ARGS";
  private static final String EMPTY = "";
  private static final String JCR_CONTENT = "/jcr:content";
  private static final String CQ5_DAM_THUMBNAIL = "cq5dam.thumbnail.";
  private static final String JPEG_EXTENSION = ".jpeg";
  private static final String JPEG_MIME_TYPE = "image/jpeg";
  private final Logger LOGGER = LoggerFactory.getLogger(ImageRenditionProcess.class);
  @Reference
  private ResourceResolverUtil resourceResolverUtil;

  private int requiredWidth;
  private int requiredHeight;
  private int originalWidth;
  private int originalHeight;
  private int cropWidth;
  private int cropHeight;

  /* This execute method crop the images as per the workflow Arguments */

  public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) {

    LOGGER.debug("START OF execute METHOD");
    ResourceResolver resourceResolver = resourceResolverUtil.getResourceResolver();
    String payload = workItem.getWorkflowData().getPayload().toString();
    String processArgument = metaDataMap.get(PROCESS_ARGS, EMPTY);
    Resource dataResource = resourceResolver.getResource(payload + JCR_CONTENT);

    if (dataResource != null) {

      Resource parentOfParentRes = getExactParent(dataResource, 2);
      if (parentOfParentRes != null) {
        Node node = parentOfParentRes.adaptTo(Node.class);
        if (node != null) {
          Double quality = 90.0;
          try {
            if (!EMPTY.equals(processArgument)) {
              List<String> args = StringUtil.splitWithNewLine(processArgument);
              for (String arg : args) {
                Layer originalLayer = ImageHelper.createLayer(dataResource);
                ImageHelper.saveLayer(getLayer(originalLayer, payload, arg), JPEG_MIME_TYPE,
                    quality, node, CQ5_DAM_THUMBNAIL + arg + JPEG_EXTENSION, true);
              }
            }
          } catch (Exception exception) {
            LOGGER.error(exception.getCause().toString());
          } finally {
            resourceResolverUtil.closeResourceResolver(resourceResolver);
          }
        }
      }
    }

    LOGGER.debug("END OF execute METHOD");
  }

  private Layer getLayer(Layer layer, String payload, String size) {

    LOGGER.debug("START OF getLayer METHOD");
    String[] splitSize = size.split("\\.");
    requiredWidth = (EMPTY).equals(splitSize[0]) ? 0 : Integer.parseInt(splitSize[0]);
    requiredHeight = (EMPTY).equals(splitSize[1]) ? 0 : Integer.parseInt(splitSize[1]);
    originalWidth = layer.getWidth();
    originalHeight = layer.getHeight();
    requiredWidth = originalWidth > requiredWidth ? requiredWidth : originalWidth;
    requiredHeight = originalHeight > requiredHeight ? requiredHeight : originalHeight;
    cropWidth = originalWidth > requiredWidth ? (originalWidth - requiredWidth) / 2 : 0;
    cropHeight = originalHeight > requiredHeight ? (originalHeight - requiredHeight) / 2 : 0;
    if (originalWidth > requiredWidth || originalHeight > requiredHeight) {
      layer = cropImage(layer, payload);
    }
    LOGGER.debug("END OF getLayer METHOD");
    return layer;
  }

  private Layer cropImage(Layer layer, String payload) {

    LOGGER.debug("START OF cropImage METHOD");
    String rectCSV = "0,0," + (requiredWidth + cropWidth) + "," + (requiredHeight + cropHeight);
    Rectangle rect = ImageHelper.getCropRect(rectCSV, payload);
    layer.crop(rect);
    layer.rotate(180.00);
    rectCSV = "0,0," + requiredWidth + "," + requiredHeight;
    rect = ImageHelper.getCropRect(rectCSV, payload);
    layer.crop(rect);
    layer.rotate(180.00);
    LOGGER.debug("END OF cropImage METHOD");
    return layer;
  }

  private Resource getExactParent(Resource resource, int level) {
    for (int i = 0; i < level; i++) {
      resource = resource.getParent();
    }
    return resource;
  }
}
