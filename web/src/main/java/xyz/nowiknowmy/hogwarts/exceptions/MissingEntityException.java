package xyz.nowiknowmy.hogwarts.exceptions;

/**
 * Indicates an entity that was expected to be present is missing.
 *
 * <p>This should only be used in cases where it is expected that the entity
 * already exists. Cases where the entity may or may not have existed should use
 * a checked exception.
 */
public class MissingEntityException extends RuntimeException {

    public MissingEntityException(String message) {
        super(message);
    }

}
