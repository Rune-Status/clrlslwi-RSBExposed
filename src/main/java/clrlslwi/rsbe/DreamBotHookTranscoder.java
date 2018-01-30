package clrlslwi.rsbe;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * This class allows for easy transcoding of dreambot credentials.
 *
 * @author Demmonic
 */
@Log
@RequiredArgsConstructor
public class DreamBotHookTranscoder {

    private static final String LIVE_URL = "http://cdn.dreambot.org/hooks.txt";
    private static final String COMMENT_ID = "#";
    private static final String NEW_TYPE_ID = "@";

    private final String hooks;

    /**
     * Parses all {@link DreamBotType types} from the hooks file.
     *
     * @return The parsed types.
     */
    public List<DreamBotType> parseTypes() {
        List<DreamBotType> types = new LinkedList<>();

        DreamBotType currentType = null;
        for (String line : hooks.split("\n")) {
            /* comment */
            if (line.startsWith(COMMENT_ID))
                continue;

            /* retard filtering just in case */
            if (line.isEmpty() || line.trim().isEmpty())
                continue;

            if (line.startsWith(NEW_TYPE_ID)) {
                if (currentType != null) {
                    types.add(currentType);
                }

                String refactored = line.substring(1, line.indexOf(" "));
                String obfuscated = line.substring(line.indexOf(": ") + 2);
                currentType = new DreamBotType();
                currentType.setRefactored(refactored);
                currentType.setObfuscated(obfuscated);
                log.log(Level.FINE, "Loaded new type " + refactored + " -> " + obfuscated);
            } else {
                String[] segments = line.split(" ");
                if (!segments[0].contains(".") || (segments.length != 3 && segments.length != 4)) {
                    log.log(Level.WARNING, "Unable to deal with line " + Arrays.toString(segments));
                    continue;
                }

                String[] parentChild = segments[0].split("\\.");
                String child = parentChild[1];

                DreamBotField field = new DreamBotField();
                field.setRefactored(child);
                field.setObfuscated(segments[2]);
                if (segments.length == 4) {
                    String key = segments[3];

                    /* why is there an L at the end? nobody knows, probably how retards store type information.. */
                    key = key.replace("L", "");

                    field.setInverseKey(Long.parseLong(key));
                }

                log.log(Level.FINE, "     Loaded new field " + field.getRefactored() + " -> " + field.getObfuscated() + " ^ " + field.getInverseKey());
            }
        }

        types.add(currentType);
        return types;
    }

    /**
     * Parses hook from a remote source.
     *
     * @param url The URL to parse the hook file from.
     * @return The parsed hook file.
     * @throws IOException If parsing the file fails.
     */
    private static String parseRemote(String url) throws IOException {
        URL urlo = new URL(url);
        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(urlo.openStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rdr.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    /**
     * Creates a new {@link DreamBotHookTranscoder hook transcoder} that points to the live hook file.
     *
     * @return The newly created transcoder.
     * @throws Exception If retrieving the hooks fails.
     */
    public static DreamBotHookTranscoder live() throws Exception {
        return new DreamBotHookTranscoder(parseRemote(LIVE_URL));
    }

}
