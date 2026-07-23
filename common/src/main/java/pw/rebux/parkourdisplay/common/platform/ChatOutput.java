package pw.rebux.parkourdisplay.common.platform;

/// Sends domain chat messages to the local client.
public interface ChatOutput {

  void display(Message message);
}
