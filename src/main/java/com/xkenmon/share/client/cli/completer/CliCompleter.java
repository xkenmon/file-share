package com.xkenmon.share.client.cli.completer;

import com.xkenmon.share.client.cli.RemotePathHolder;
import java.util.ArrayList;
import java.util.List;
import org.jline.builtins.Completers.FileNameCompleter;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import picocli.AutoComplete;
import picocli.CommandLine.Model.CommandSpec;

public class CliCompleter implements Completer {

  private final CommandSpec spec;
  private final FileNameCompleter fileNameCompleter = new FileNameCompleter();

  public CliCompleter(CommandSpec spec) {
    this.spec = spec;
  }

  @Override
  public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
    String[] words = new String[line.words().size()];
    words = line.words().toArray(words);
    List<CharSequence> cs = new ArrayList<>();

    // picocli completer.
    AutoComplete.complete(spec,
        words,
        line.wordIndex(),
        0,
        line.cursor(),
        cs);

    if (line.wordIndex() == 1) {
      cs.addAll(RemotePathHolder.getRemotePaths());
    }

    if (line.wordIndex() == 2) {
      fileNameCompleter.complete(reader, line, candidates);
    }

    for (CharSequence c : cs) {
      candidates.add(new Candidate((String) c));
    }
  }
}
