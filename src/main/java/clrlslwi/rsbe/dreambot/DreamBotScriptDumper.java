package clrlslwi.rsbe.dreambot;

import clrlslwi.rsbe.util.AsmUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.dreambot.LoginToken;
import org.dreambot.server.net.datatype.ScriptData;
import org.dreambot.server.net.datatype.ScriptMainClassResponse;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import static org.objectweb.asm.tree.AbstractInsnNode.METHOD_INSN;

/**
 * Dumps all classes of a script even when only the entry point is provided.
 * <p>
 * Interesting security mechanic, yet futile in the grand scheme of things.
 *
 * @author Demmonic
 */
@Log
@RequiredArgsConstructor
public class DreamBotScriptDumper implements Runnable {

    private final Path root;
    private final DreamBotKryoNet net;
    private final LoginToken token;
    private final ScriptData script;

    private String location;

    /**
     * Determines if a type should be dumped by its internal descriptor.
     *
     * @param internalType The internal type to check.
     * @return If the provided type should be dumped.
     */
    private boolean shouldTryDump(String internalType) {
        return !internalType.startsWith("org/dreambot")
                && !internalType.startsWith("java/")
                && !internalType.startsWith("javax/")
                && !internalType.equals("void")
                && !internalType.equals("boolean")
                && !internalType.equals("byte")
                && !internalType.equals("char")
                && !internalType.equals("short")
                && !internalType.equals("int")
                && !internalType.equals("long")
                && !internalType.equals("float")
                && !internalType.equals("double")
                ;
    }

    /**
     * Dumps a resource from the remote server.
     *
     * @param name The name of the resource to dump.
     * @throws IOException If dumping the resource fails.
     */
    private void dump(String name) throws IOException {
        name = name.replace("[]", "");
        name = name.replace("[", "");
        while (name.startsWith("L")) {
            name = name.substring(1);
        }
        while (name.endsWith(";")) {
            name = name.substring(0, name.length() - 1);
        }

        if (!shouldTryDump(name)) {
            log.log(Level.FINE, "Skipping " + name + "...");
            return;
        }

        Path path = root.resolve(script.getName().trim().replace(' ', '_') + "/" + name.replace('.', '/') + ".class");
        if (Files.exists(path)) {
            log.log(Level.FINE, "Contents of " + location + "/" + name + " already present!");
            return;
        }

        byte[] contents = DreamBotKryoNet.getResource(location, name);
        if (contents == null) {
            log.log(Level.WARNING, "Contents of " + location + "/" + name + " is null!");
            return;
        }

        Files.createDirectories(path.getParent());
        Files.write(path, contents);

        ClassNode cn = AsmUtils.fromBytes(contents);
        for (MethodNode mn : cn.methods) {
            dump(Type.getReturnType(mn.desc).getClassName().replace('.', '/'));
            for (Type arg : Type.getArgumentTypes(mn.desc)) {
                dump(arg.getClassName().replace('.', '/'));
            }

            for (AbstractInsnNode ain : mn.instructions.toArray()) {
                if (ain.getType() == METHOD_INSN) {
                    MethodInsnNode min = (MethodInsnNode) ain;
                    dump(min.owner);
                }
            }
        }
    }

    @Override
    public void run() {
        ScriptMainClassResponse main = net.getMainClass(token, script);
        location = main.getLocation();

        try {
             /* boom */
            dump(main.getClassName());
        } catch (IOException e) {
            log.log(Level.SEVERE, "Failed to dump", e);
        }
    }

}
