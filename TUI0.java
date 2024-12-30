import java.io.IOException;
import java.util.Random;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class TUI0 {
  private static final int FPS = 60; // target frame rate
  private static final long NSPF = (long) (1e+9/FPS); // nanoseconds alotted per frame
  private static final Random random = new Random();
  private static final int fpsUpdateSec = 3;
  private static long startTime = 0;
  private static long lastFrameNS = 0;
  private static long sleepingMS = 0;
  private static long sleep = 0;
  private static long sleepSum = 0;
  private static long[] sleepAvg = new long[FPS*fpsUpdateSec]; // keep a moving average of sleep time
  private static int sleepSampleIndex = 0;
  private static TerminalSize terminalSize = null;
  private static KeyStroke lastKey = null;
  private static long fpsTimer = 0;
  private static int frames = 0;
  private static int fps = 0;

  private static Screen makeNewScreen() throws IOException {
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

  private static void confetti(Screen screen) {
    int c = random.nextInt(terminalSize.getColumns());
    int r = random.nextInt(terminalSize.getRows());
    TextCharacter[] tc = TextCharacter.fromCharacter(
      ' ',
      TextColor.ANSI.DEFAULT,
      TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().length)]
    );
    screen.setCharacter(c, r, tc[0]);
  }

  private static void infoBox(Screen screen, String[] lines) {
    int h = lines.length;
    int w = 0, n = 0;
    for (String ln : lines) { n = ln.length(); if (n > w) { w = n; } }

    TerminalSize boxSize = new TerminalSize(w + 2, h + 2);
    TerminalPosition boxTopLeft = new TerminalPosition(1, 1);
    TerminalPosition boxTopRight = boxTopLeft.withRelativeColumn(boxSize.getColumns() - 1);

    TextGraphics textGraphics = screen.newTextGraphics();
    // background fill
    textGraphics.fillRectangle(boxTopLeft, boxSize, ' ');
    // left border
    textGraphics.drawLine(
      boxTopLeft.withRelativeRow(1),
      boxTopLeft.withRelativeRow(boxSize.getRows() - 2),
      Symbols.DOUBLE_LINE_VERTICAL);
    // top border
    textGraphics.drawLine(
      boxTopLeft.withRelativeColumn(1),
      boxTopLeft.withRelativeColumn(boxSize.getColumns() - 2),
      Symbols.DOUBLE_LINE_HORIZONTAL);
    // right border
    textGraphics.drawLine(
      boxTopRight.withRelativeRow(1),
      boxTopRight.withRelativeRow(boxSize.getRows() - 2),
      Symbols.DOUBLE_LINE_VERTICAL);
    // bottom border
    textGraphics.drawLine(
      boxTopLeft.withRelativeRow(h + 1).withRelativeColumn(1),
      boxTopLeft.withRelativeRow(h + 1).withRelativeColumn(boxSize.getColumns() - 2),
      Symbols.DOUBLE_LINE_HORIZONTAL);
    // border corners
    textGraphics.setCharacter(boxTopLeft, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);
    textGraphics.setCharacter(boxTopRight, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
    textGraphics.setCharacter(boxTopRight.withRelativeRow(h + 1), Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER);
    textGraphics.setCharacter(boxTopLeft.withRelativeRow(h + 1), Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER);
    // message lines
    n = 0;
    for (String ln : lines) {
      textGraphics.putString(boxTopLeft.withRelative(1, n + 1), ln);
      n++;
    }
  }

  private static void updateState(Screen screen) throws IOException {
    // screen dimensions
    TerminalSize newSize = screen.doResizeIfNecessary();
    if(newSize != null) { terminalSize = newSize; }
    // keyboard input
    KeyStroke keyStroke = screen.pollInput();
    if(keyStroke != null) { lastKey = keyStroke; }
    // frame rate
    frames++;
    long now = System.nanoTime();
    // sleep average
    sleepSum -= sleepAvg[sleepSampleIndex];
    sleepSum += sleepingMS;
    sleepAvg[sleepSampleIndex] = sleepingMS;
    sleepSampleIndex = (sleepSampleIndex+1) % sleepAvg.length;

    if(fpsTimer + fpsUpdateSec*1e+9 < now) {
      fps = (int) (frames / ((now-fpsTimer)*1e-9) / fpsUpdateSec);
      fpsTimer = (long) (now + fpsUpdateSec*1e+9);
      frames = 0;
      sleep = (int) (sleepSum / sleepAvg.length);
    }
    // if (sleep == 0) { sleep = sleepingMS; }
    // else { sleep = (long) (sleep * 0.99 + sleepingMS * 0.01); }
  }

  private static void renderState(Screen screen) {
    // update background
    int i = 0, nfetti = 50;
    for (i = 0; i < nfetti; i++) { confetti(screen); }
    // redraw info box
    String[] info = {
      String.format("Terminal Size: %s", terminalSize),
      String.format("Last Key: %s", (lastKey == null) ? "<pending>" : lastKey.toString()),
      String.format("Frame Rate %dsec: %d", fpsUpdateSec, fps),
      String.format("Frame Sleep: %2dms", sleep),
    };
    infoBox(screen, info);
  }

  private static long elapsedFrameNS() {
    long now = System.nanoTime();
    long elapsed = now - lastFrameNS;
    lastFrameNS = now;
    return elapsed;
  }

  public static void main(String[] args) throws InterruptedException {
    Screen screen = null;
    boolean running = true;
    long frameNS = 0;
    try {
      screen = makeNewScreen();
      terminalSize = screen.getTerminalSize();
      startTime = System.nanoTime();
      lastFrameNS = startTime;
      while(running) {
        updateState(screen); // refresh internal values
        renderState(screen); // paint to back buffer
        screen.refresh(); // copy back buffer deltas to front
        if(userIsQuitting(lastKey)) { running = false; }
        frameNS = elapsedFrameNS();
        sleepingMS = (NSPF > frameNS) ? (long) ((NSPF - frameNS) * 1e-6) : 0;
        Thread.sleep(sleepingMS);
      }
    }
    catch(IOException e) { e.printStackTrace(); }
    finally {
      if(screen != null) {
        try { screen.close(); }
        catch(IOException e) { e.printStackTrace(); }
      }
    }
  }
}
