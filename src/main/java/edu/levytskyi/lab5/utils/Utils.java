package edu.levytskyi.lab5.utils;
/* @author Sandoplay
 * @project lab5
 * @class Utils
 * @version 1.0.0
 * @since 18.05.2025 - 17.26
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
    public static String toJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }
}