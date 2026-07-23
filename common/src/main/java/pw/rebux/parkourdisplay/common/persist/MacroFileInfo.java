package pw.rebux.parkourdisplay.common.persist;

import java.util.Date;

public record MacroFileInfo(
    String name,
    Date lastModified
) {
}
