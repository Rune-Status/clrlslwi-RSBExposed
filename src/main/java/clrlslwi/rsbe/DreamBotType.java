package clrlslwi.rsbe;

import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a single type from the DB updater logs.
 *
 * @author Demmonic
 */
public class DreamBotType extends Symbol {

    @Getter
    private final List<DreamBotField> fields = new LinkedList<>();

    public void addField(DreamBotField field) {
        fields.add(field);
    }

}
