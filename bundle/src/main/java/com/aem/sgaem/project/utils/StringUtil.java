package com.aem.sgaem.project.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(Logger.class);

  public static List<String> splitWithNewLine(String input) {
    LOGGER.debug("START OF split METHOD");

    List<String> finalList = new ArrayList<String>();
    if (input != null && !input.equals("")) {
      String[] inputArray = input.split(Pattern.quote("\n"));
      finalList = Arrays.asList(inputArray);
    }

    LOGGER.debug("END OF split METHOD");
    return finalList;
  }
}
