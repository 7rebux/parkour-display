package pw.rebux.parkourdisplay.integration.packets;

import java.util.Objects;
import net.labymod.serverapi.api.packet.Packet;
import net.labymod.serverapi.api.payload.io.PayloadReader;
import net.labymod.serverapi.api.payload.io.PayloadWriter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.api.SplitBoxTriggerMode;
import pw.rebux.parkourdisplay.integration.model.DoubleVec3;

@SuppressWarnings("unused")
public final class RunDataPacket implements Packet {

  private String name;
  private DoubleVec3 start;
  private DoubleVec3 minEnd, maxEnd;
  private SplitBoxTriggerMode triggerMode;
  private Long personalBest;

  public RunDataPacket(
      @NonNull String name,
      @NonNull DoubleVec3 start,
      @NonNull DoubleVec3 minEnd,
      @NonNull DoubleVec3 maxEnd,
      @NonNull SplitBoxTriggerMode triggerMode,
      @Nullable Long personalBest
  ) {
    Objects.requireNonNull(name, "Name cannot be null");
    Objects.requireNonNull(start, "Start cannot be null");
    Objects.requireNonNull(minEnd, "MinEnd cannot be null");
    Objects.requireNonNull(maxEnd, "MaxEnd cannot be null");
    Objects.requireNonNull(triggerMode, "TriggerMode cannot be null");

    this.name = name;
    this.start = start;
    this.minEnd = minEnd;
    this.maxEnd = maxEnd;
    this.triggerMode = triggerMode;
    this.personalBest = personalBest;
  }

  @Override
  public void read(@NonNull PayloadReader reader) {
    this.name = reader.readString();
    this.start = new DoubleVec3(
        reader.readDouble(),
        reader.readDouble(),
        reader.readDouble()
    );
    this.minEnd = new DoubleVec3(
        reader.readDouble(),
        reader.readDouble(),
        reader.readDouble()
    );
    this.maxEnd = new DoubleVec3(
        reader.readDouble(),
        reader.readDouble(),
        reader.readDouble()
    );
    this.triggerMode = SplitBoxTriggerMode.values()[reader.readVarInt()];
    this.personalBest = reader.readOptional(reader::readLong);
  }

  @Override
  public void write(@NonNull PayloadWriter writer) {
    writer.writeString(this.name);

    writer.writeDouble(this.start.x());
    writer.writeDouble(this.start.y());
    writer.writeDouble(this.start.z());

    writer.writeDouble(this.minEnd.x());
    writer.writeDouble(this.minEnd.y());
    writer.writeDouble(this.minEnd.z());

    writer.writeDouble(this.maxEnd.x());
    writer.writeDouble(this.maxEnd.y());
    writer.writeDouble(this.maxEnd.z());

    writer.writeVarInt(this.triggerMode.ordinal());

    writer.writeOptional(this.personalBest, writer::writeLong);
  }

  public String getName() {
    return name;
  }

  public DoubleVec3 getStart() {
    return this.start;
  }

  public DoubleVec3 getMinEnd() {
    return this.minEnd;
  }

  public DoubleVec3 getMaxEnd() {
    return this.maxEnd;
  }

  public SplitBoxTriggerMode getTriggerMode() {
    return triggerMode;
  }

  public @Nullable Long getPersonalBest() {
    return personalBest;
  }

  @Override
  public String toString() {
    return "LoadRunPacket{" +
        "name='" + this.name + '\'' +
        ", start=" + this.start +
        ", minEnd=" + this.minEnd +
        ", maxEnd=" + this.maxEnd +
        ", triggerMode=" + this.triggerMode +
        '}';
  }
}
