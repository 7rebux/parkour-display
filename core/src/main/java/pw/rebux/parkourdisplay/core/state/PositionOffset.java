package pw.rebux.parkourdisplay.core.state;

import lombok.Builder;

@Builder
public record PositionOffset(
    double posX,
    double posY,
    double posZ,
    double offsetX,
    double offsetZ
) {
}
