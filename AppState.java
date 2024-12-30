import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

public class AppState {
  public FrameTimer frameTimer = null;
  public KeyStroke lastKey = null;
  public Screen screen = null;
  public TerminalSize terminalSize = null;
}