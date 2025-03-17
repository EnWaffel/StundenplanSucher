package eu.projectp.webutils.sps.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

public record Result(String result, boolean success, String error) {

    public static Result getErrorFromException(Throwable e) {
        //😕
        String builder = "Irgendwas ist schiefgelaufen ):" +
                "\n\n" +
                ExceptionUtils.getStackTrace(e);

        return new Result(null, false, builder);
    }

}
