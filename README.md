# `tui0`-java

a starting point for creating terminal apps handling non-canonical input and rendering their user interface via text.

this template implements some opinions about what basic functionality such an app should have:
- game loop with consistent time deltas
- full-screen addressable per-character read/write
- double-buffered screen redraw
- key event handling
- screen resize handling
- simple screen stack
- debug tooling
  - frame timing
  - logging
  - on-screen command input

many of these features are built upon functionality provided by the [lanterna] library.

## usage
> reference the [lanterna jar] when compiling and running

### compile

nix `javac -cp '.:lib/*' TUI0.java` <br>
win `javac -cp .;lib/* TUI0.java`

### run

nix `java -cp '.:lib/*' TUI0.java` <br>
win `java -cp .;lib/* TUI0.java`



[lanterna]: https://github.com/mabe02/lanterna "Java library for creating text-based GUIs"
[lanterna jar]: https://central.sonatype.com/artifact/com.googlecode.lanterna/lanterna "com.googlecode.lanterna at Sonatype's Maven repo"
