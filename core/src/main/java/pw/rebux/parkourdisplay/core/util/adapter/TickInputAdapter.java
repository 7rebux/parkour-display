package pw.rebux.parkourdisplay.core.util.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import pw.rebux.parkourdisplay.core.state.TickInput;

public final class TickInputAdapter extends TypeAdapter<TickInput> {

  @Override
  public void write(JsonWriter out, TickInput value) throws IOException {
    out.beginArray();
    out.value(value.w());
    out.value(value.a());
    out.value(value.s());
    out.value(value.d());
    out.value(value.jump());
    out.value(value.sprint());
    out.value(value.sneak());
    out.value(value.yaw());
    out.value(value.pitch());
    out.endArray();
  }

  @Override
  public TickInput read(JsonReader in) throws IOException {
    in.beginArray();

    boolean w = in.nextBoolean();
    boolean a = in.nextBoolean();
    boolean s = in.nextBoolean();
    boolean d = in.nextBoolean();
    boolean jump = in.nextBoolean();
    boolean sprint = in.nextBoolean();
    boolean sneak = in.nextBoolean();
    float yaw = (float) in.nextDouble();
    float pitch = (float) in.nextDouble();

    in.endArray();

    return new TickInput(w, a, s, d, jump, sprint, sneak, yaw, pitch);
  }
}
