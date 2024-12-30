import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

public class UI {
  public static final String EMPTY_BORDER = "        ";
  public static final String SINGLE_BORDER = String.format("%s%s%s%s%s%s%s%s",
    Symbols.SINGLE_LINE_VERTICAL,
    Symbols.SINGLE_LINE_TOP_LEFT_CORNER,
    Symbols.SINGLE_LINE_HORIZONTAL,
    Symbols.SINGLE_LINE_TOP_RIGHT_CORNER,
    Symbols.SINGLE_LINE_VERTICAL,
    Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER,
    Symbols.SINGLE_LINE_HORIZONTAL,
    Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER
  );
  public static final String DOUBLE_BORDER = String.format("%s%s%s%s%s%s%s%s",
    Symbols.DOUBLE_LINE_VERTICAL,
    Symbols.DOUBLE_LINE_TOP_LEFT_CORNER,
    Symbols.DOUBLE_LINE_HORIZONTAL,
    Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER,
    Symbols.DOUBLE_LINE_VERTICAL,
    Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER,
    Symbols.DOUBLE_LINE_HORIZONTAL,
    Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER
  );

  public static void infoBox(Screen screen, String[] lines) {
    infoBox(screen, lines, EMPTY_BORDER);
  }

  public static void infoBox(Screen screen, String[] lines, String border) {
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
      border.charAt(0));
    // top border
    textGraphics.drawLine(
      boxTopLeft.withRelativeColumn(1),
      boxTopLeft.withRelativeColumn(boxSize.getColumns() - 2),
      border.charAt(2));
    // right border
    textGraphics.drawLine(
      boxTopRight.withRelativeRow(1),
      boxTopRight.withRelativeRow(boxSize.getRows() - 2),
      border.charAt(4));
    // bottom border
    textGraphics.drawLine(
      boxTopLeft.withRelativeRow(h + 1).withRelativeColumn(1),
      boxTopLeft.withRelativeRow(h + 1).withRelativeColumn(boxSize.getColumns() - 2),
      border.charAt(6));
    // border corners
    textGraphics.setCharacter(boxTopLeft, border.charAt(1));
    textGraphics.setCharacter(boxTopRight, border.charAt(3));
    textGraphics.setCharacter(boxTopRight.withRelativeRow(h + 1), border.charAt(5));
    textGraphics.setCharacter(boxTopLeft.withRelativeRow(h + 1), border.charAt(7));
    // message lines
    n = 0;
    for (String ln : lines) {
      textGraphics.putString(boxTopLeft.withRelative(1, n + 1), ln);
      n++;
    }
  }
}