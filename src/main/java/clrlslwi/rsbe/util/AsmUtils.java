package clrlslwi.rsbe.util;

import lombok.experimental.UtilityClass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * @author Demmonic
 */
@UtilityClass
public class AsmUtils {

    /**
     * Converts a class file into a {@link ClassNode}.
     *
     * @param b The class file in a byte array.
     * @return The created node.
     */
    public static ClassNode fromBytes(byte[] b) {
        ClassReader cr = new ClassReader(b);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);
        return cn;
    }

}
