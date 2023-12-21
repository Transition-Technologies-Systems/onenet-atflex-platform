package pl.com.tt.flex.server.validator;


import pl.com.tt.flex.server.common.errors.ObjectValidationException;

public interface ObjectValidator<T, ID> {

    /**
     * Creating a new entity
     *
     * @param t object (mostly, if not always, a dto) to validate
     * @throws ObjectValidationException
     */
    default void checkValid(T t) throws ObjectValidationException {
    }

    /**
     * Modifying an entity (checkValid() still needed!! This method is only an "extension"
     * for whatever additional restrictions there might be on modifying)
     *
     * @param t object (mostly a dto) to check modifiability for
     * @throws ObjectValidationException
     */
    default void checkModifiable(T t) throws ObjectValidationException {

        checkValid(t);
    }

    /**
     * Deleting an entity that might possibly be protected (for cases such as "admin member")
     *
     * @param id the ID of the object to be deleted
     * @throws ObjectValidationException
     */
    default void checkDeletable(ID id) throws ObjectValidationException {
    }

}
