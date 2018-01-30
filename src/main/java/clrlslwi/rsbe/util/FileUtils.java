package clrlslwi.rsbe.util;

import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides utilities relating to files.
 *
 * @author Demmonic
 */
@Log
@UtilityClass
public class FileUtils {

    /**
     * Retrieves a list of children within the provided {@link Path path}.
     *
     * @param p The path to retrieve the children of.
     * @return All children under the provided path.
     * @throws IOException If the listing fails.
     */
    public static List<Path> children(Path p) throws IOException {
        Stream<Path> childStream = Files.list(p);
        List<Path> children = childStream.collect(Collectors.toList());
        childStream.close();
        return children;
    }

    /**
     * Checks if a {@link Path path} has any children or not.
     *
     * @param p The path to check.
     * @return IF the provided path has any children.
     * @throws IOException If listing the children fails.
     */
    public static boolean isEmpty(Path p) throws IOException {
        return children(p).size() <= 0;
    }

    /**
     * Recursively deletes a folder, and all children.
     *
     * @param p The {@link Path folder} to delete.
     * @throws IOException If deleting fails.
     */
    public static void delete(Path p) throws IOException {
        if (Files.isDirectory(p)) {
            for (Path child : children(p)) {
                delete(child);
            }

            if (isEmpty(p)) {
                Files.delete(p);
            }
        } else {
            Files.delete(p);
        }

        if (Files.exists(p)) {
            throw new IOException("Failed to delete " + p + "!");
        }
    }

    /**
     * Copies all files from one {@link Path path} to another.
     *
     * @param fromRoot The path that we're copying from.
     * @param from     The current directory we're copying from (must be within fromRoot.)
     * @param toRoot   The path that we're copying to.
     * @throws IOException
     */
    public static void copy(Path fromRoot, Path from, Path toRoot) throws IOException {
        for (Path child : children(from)) {
            Path relative = fromRoot.toAbsolutePath().relativize(child.toAbsolutePath());
            Path toOffset = toRoot.resolve(relative);

            if (Files.isDirectory(child)) {
                if (!Files.exists(toOffset)) {
                    Files.createDirectories(toOffset);
                }

                copy(fromRoot, child, toRoot);
            } else {
                if (!Files.exists(toOffset.getParent())) {
                    Files.createDirectories(toOffset.getParent());
                }

                try (InputStream ins = new FileInputStream(child.toAbsolutePath().toFile())) {
                    try (OutputStream ons = new FileOutputStream(toOffset.toAbsolutePath().toFile())) {
                        IOUtils.copy(ins, ons);
                    }
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Failed to copy file", e);
                }
            }
        }
    }

}
