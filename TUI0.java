import java.io.IOException;
import java.util.Random;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;


public class TUI0 {
  private static final AppState state = new AppState();
  private static final Random random = new Random();


  private static Screen getTerminalScreen() throws IOException {
    DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
    Terminal terminal = defaultTerminalFactory.createTerminal();
    Screen screen = new TerminalScreen(terminal);
    screen.startScreen();
    screen.setCursorPosition(null); // null triggers call to terminal.setCursorVisible(false);
    return screen;
  }

  private static boolean userIsQuitting(KeyStroke keyStroke) {
    if(keyStroke == null) { return false; }
    else {
      if(keyStroke.getKeyType() == KeyType.Escape) { return true; }
      if(keyStroke.getKeyType() == KeyType.EOF) { return true; }
    }
    return false;
  }

  private static void confetti(Screen screen, TerminalSize size) {
    int c = random.nextInt(size.getColumns());
    int r = random.nextInt(size.getRows());
    TextCharacter[] tc = TextCharacter.fromCharacter(
      ' ',
      TextColor.ANSI.DEFAULT,
      TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().length)]
    );
    screen.setCharacter(c, r, tc[0]);
  }

  private static void updateState(AppState state) throws IOException {
    // screen dimensions
    TerminalSize newSize = state.screen.doResizeIfNecessary();
    if(newSize != null) { state.terminalSize = newSize; }
    // keyboard input
    KeyStroke keyStroke = state.screen.pollInput();
    if(keyStroke != null) { state.lastKey = keyStroke; }
    // frame rate
    state.frameTimer.nextFrame();
  }

  private static void renderState(AppState state) {
    // update background
    int i = 0, nfetti = 50;
    for (i = 0; i < nfetti; i++) { confetti(state.screen, state.terminalSize); }
    // redraw info box
    String[] info = {
      String.format("Terminal Size: %s", state.terminalSize),
      String.format("Last Key: %s", (state.lastKey == null) ? "<pending>" : state.lastKey.toString()),
      String.format("Frame Rate %dsec: %d", state.frameTimer.getFpsRefreshSec(), state.frameTimer.getFps()),
      String.format("Frame Sleep: %2dms", state.frameTimer.getSleepAvgMS()),
    };
    UI.infoBox(state.screen, info);
  }

  public static void main(String[] args) throws InterruptedException {
    boolean running = true;
    long frameNS = 0;
    try {
      state.screen = getTerminalScreen();
      state.terminalSize = state.screen.getTerminalSize();
      state.frameTimer = new FrameTimer(60, 3);
      state.frameTimer.start();
      while(running) {
        updateState(state); // refresh internal values
        renderState(state); // paint to back buffer
        state.screen.refresh(); // copy back buffer deltas to front
        if(userIsQuitting(state.lastKey)) { running = false; }
        Thread.sleep(state.frameTimer.getSleepingMS());
      }
    }
    catch(IOException e) { e.printStackTrace(); }
    finally {
      if(state.screen != null) {
        try { state.screen.close(); }
        catch(IOException e) { e.printStackTrace(); }
      }
    }
  }
}
